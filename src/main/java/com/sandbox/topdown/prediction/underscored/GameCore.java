package com.sandbox.topdown.prediction.underscored;

import com.sandbox.topdown.prediction.underscored.packet.InputPacket;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.sandbox.topdown.Game;
import com.sandbox.topdown.prediction.underscored.packet.PingPacket;
import com.sandbox.topdown.prediction.underscored.packet.ServerUpdatePacket;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maarten
 */
public class GameCore {

    private static final Logger LOG = LoggerFactory.getLogger(GameCore.class);

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final float playerspeed = 120f;

    public Player self;
    public Player other;

    //Our ghost position on the server
    private Player server_pos_self;

    //The other players server position as we receive it
    private Player server_pos_other;

    //The other players ghost destination position (the lerp)
    private Player pos_other;

    private final GameInstance instance;
    public boolean naive_approach;
    public boolean client_predict;
    public boolean client_smoothing;
    public float client_smooth;

    private LinkedList<ServerUpdate> server_updates = new LinkedList<>();

    float _pdt = 0.0001F;               //The physics update delta time
    long _pdte = System.currentTimeMillis();  //The physics update last delta time

    //A local timer for precision on server and client
    float server_time;
    float local_time = 0.016F;          //The local timer
    long _dt = System.currentTimeMillis();    //The local timer delta
    long _dte = System.currentTimeMillis();   //The local timer last frame time
    private ServerUpdate laststate;
    private int input_seq;
    private ClientSocket socket;
    private float client_time;
    private float target_time;
    private int net_offset;
    private int buffer_size;
    private float oldest_tick;
    private long fake_lag;
    private long last_ping_time;
    private long net_ping;
    private long net_latency;
    private final boolean server;

    public GameCore(GameInstance instance) {
        this.instance = instance;
        this.server = (this.instance != null);

        initPlayers();

        //Start a physics loop, this is separate to the rendering
        //as this happens at a fixed frequency
        create_physics_simulation();

        //Start a fast paced timer for measuring time easier
        create_timer();

        //if client
        if (!this.server) {
            //Create the default configuration settings
            this.client_create_configuration();

            //Connect to the  server!
            this.client_connect_to_server();

            //We start pinging the server to determine latency
            this.client_create_ping_timer();

        } else { //if server

            this.server_time = 0;
            this.laststate = null;

        }
        this.executor.scheduleAtFixedRate(this::update, 0, 15, TimeUnit.MILLISECONDS);
    }

    private void initPlayers() {
        if (instance != null) {
            this.self = new Player(this, this.instance.player_host);
            this.other = new Player(this, this.instance.player_client);

            this.self.pos = new Vector2(20F, 20F);
        } else {
            this.self = new Player(this);
            this.other = new Player(this);

            //The other players ghost destination position (the lerp)
            this.pos_other = new Player(this);
            this.pos_other.state = Player.State.dest_pos;
            this.pos_other.color = Color.GRAY;
            this.pos_other.pos = new Vector2(500F, 200F);

            //Our ghost position on the server
            this.server_pos_self = new Player(this);
            this.server_pos_self.state = Player.State.server_pos;
            this.server_pos_self.color = Color.LIGHT_GRAY;
            this.server_pos_self.pos = new Vector2(20F, 20F);

            //The other players server position as we receive it
            this.server_pos_other = new Player(this);
            this.server_pos_other.state = Player.State.server_pos;
            this.server_pos_other.color = Color.LIGHT_GRAY;
            this.server_pos_other.pos = new Vector2(500F, 200F);
        }

    }

    public void update() {
        //Update the game specifics
        if (instance == null) {
            this.client_update();
        } else {
            this.server_update();
        }
    }

    public void client_update() {

        //Capture inputs from the player
        this.client_handle_input();

        //Network player just gets drawn normally, with interpolation from
        //the server updates, smoothing out the positions from the past.
        //Note that if we don't have prediction enabled - this will also
        //update the actual local client position on screen as well.
        if (!this.naive_approach) {
            this.client_process_net_updates();
        }

        //When we are doing client side prediction, we smooth out our position
        //across frames using local input states we have stored.
        this.client_update_local_position();

    }

    private void client_process_net_updates() {
        //No updates...
        if (this.server_updates.isEmpty()) {
            return;
        }

        //First : Find the position in the updates, on the timeline
        //We call this current_time, then we find the past_pos and the target_pos using this,
        //searching throught the server_updates array for current_time in between 2 other times.
        // Then :  other player position = lerp ( past_pos, target_pos, current_time );
        //Find the position in the timeline of updates we stored.
        float current_time = this.client_time;
        int count = this.server_updates.size() - 1;
        ServerUpdate target = null;
        ServerUpdate previous = null;

        //We look from the 'oldest' updates, since the newest ones
        //are at the end (list.length-1 for example). This will be expensive
        //only when our time is not found on the timeline, since it will run all
        //samples. Usually this iterates very little before breaking out with a target.
        for (int i = 0; i < count; ++i) {

            ServerUpdate point = this.server_updates.get(i);
            ServerUpdate next_point = this.server_updates.get(i + 1);

            //Compare our point in time with the server times we have
            if (current_time > point.time && current_time < next_point.time) {
                target = next_point;
                previous = point;
                break;
            }
        }

        //With no target we store the last known
        //server position and move to that instead
        if (target == null) {
            target = this.server_updates.get(0);
            previous = this.server_updates.get(0);
        }

        //Now that we have a target and a previous destination,
        //We can interpolate between then based on 'how far in between' we are.
        //This is simple percentage maths, value/target = [0,1] range of numbers.
        //lerp requires the 0,1 value to lerp to? thats the one.
        if (target != null && previous != null) {
            this.target_time = target.time;

            float difference = this.target_time - current_time;
            float max_difference = (target.time - previous.time);
            float time_point = (difference / max_difference);

            //Because we use the same target and previous in extreme cases
            //It is possible to get incorrect values due to division by 0 difference
            //and such. This is a safe guard and should probably not be here. lol.
            if (Float.isNaN(time_point)) {
                time_point = 0;
            }
            if (time_point == Float.NEGATIVE_INFINITY) {
                time_point = 0;
            }
            if (time_point == Float.POSITIVE_INFINITY) {
                time_point = 0;
            }

            //The most recent server update
            ServerUpdate latest_server_data = this.server_updates.get(this.server_updates.size() - 1);

            //These are the exact server positions from this tick, but only for the ghost
            Vector2 other_server_pos = this.self.host ? latest_server_data.clientPosition : latest_server_data.hostPosition;

            //The other players positions in this timeline, behind us and in front of us
            Vector2 other_target_pos = this.self.host ? target.clientPosition : target.hostPosition;
            Vector2 other_past_pos = this.self.host ? previous.clientPosition : previous.hostPosition;

            //update the dest block, this is a simple lerp
            //to the target from the previous point in the server_updates buffer
            this.server_pos_other.pos = other_server_pos;
            this.pos_other.pos = this.v_lerp(other_past_pos, other_target_pos, time_point);

            if (this.client_smoothing) {
                this.other.pos = this.v_lerp(this.other.pos, this.pos_other.pos, this._pdt * this.client_smooth);
            } else {
                this.other.pos = this.pos_other.pos;
            }

            //Now, if not predicting client movement , we will maintain the local player position
            //using the same method, smoothing the players information from the past.
            if (!this.client_predict && !this.naive_approach) {
                //These are the exact server positions from this tick, but only for the ghost
                Vector2 my_server_pos = this.self.host ? latest_server_data.hostPosition : latest_server_data.clientPosition;

                //The other players positions in this timeline, behind us and in front of us
                Vector2 my_target_pos = this.self.host ? target.hostPosition : target.clientPosition;
                Vector2 my_past_pos = this.self.host ? previous.hostPosition : previous.clientPosition;

                //Snap the ghost to the new server position
                this.server_pos_self.pos = my_server_pos;
                Vector2 local_target = this.v_lerp(my_past_pos, my_target_pos, time_point);

                //Smoothly follow the destination position
                if (this.client_smoothing) {
                    this.self.pos = this.v_lerp(this.self.pos, local_target, this._pdt * this.client_smooth);
                } else {
                    this.self.pos = local_target;
                }
            }
        }
    }

    private void client_update_local_position() {
        if (this.client_predict) {
            //Work out the time we have since we updated the state
            float t = (this.local_time - this.self.state_time) / this._pdt;

            //Then store the states for clarity,
            Vector2 old_state = this.self.old_state.pos;
            Vector2 current_state = this.self.cur_state.pos;

            //Make sure the visual position matches the states we have stored
            //this.players.self.pos = this.v_add( old_state, this.v_mul_scalar( this.v_sub(current_state,old_state), t )  );
            this.self.pos = current_state;

            //We handle collision on client if predicting.
            this.check_collision(this.self);
        }
    }

    public Vector2 client_handle_input() {

        //if(this.lit > this.local_time) return;
        //this.lit = this.local_time+0.5; //one second delay
        //This takes input from the client and keeps a record,
        //It also sends the input information to the server immediately
        //as it is pressed. It also tags each input with a sequence number.
        boolean left = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D);
        boolean up = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean down = Gdx.input.isKeyPressed(Input.Keys.S);

        if (!(left || right || up || down)) {
            return new Vector2();
        }

        this.input_seq += 1;
        GameInput input = new GameInput();
        input.left = left;
        input.right = right;
        input.up = up;
        input.down = down;
        input.time = this.local_time;
        input.seq = this.input_seq;

        this.self.inputs.add(input);

        this.socket.send(new InputPacket(this.input_seq, this.local_time, input));

        return getInputVector(input);

    }

    public void handle_server_input(PlayerClient client, GameInput input) {
        //Fetch which client this refers to out of the two
        Player player_client = (client.userid.equals(this.self.instance.userid)) ? this.self : this.other;

        //Store the input on the player instance for processing in the physics loop
        player_client.inputs.add(input);
    }

    private Vector2 process_input(Player player) {
        //It's possible to have recieved multiple inputs by now,
        //so we process each one
        Vector2 resulting_vector = null;
        int ic = player.inputs.size();
        if (ic > 0) {
            for (int j = 0; j < ic; ++j) {
                //don't process ones we already have simulated locally
                GameInput input = player.inputs.get(j);
                if (input.seq <= player.last_input_seq) {
                    continue;
                }

                resulting_vector = getInputVector(input);
            } //for each input command
        } //if we have inputs
        //we have a direction vector now, so apply the same physics as the client

        if (ic > 0) {
            //we can now clear the array since these have been processed
            player.last_input_time = player.inputs.get(ic - 1).time;
            player.last_input_seq = player.inputs.get(ic - 1).seq;
        }

        if (resulting_vector == null) {
            return new Vector2();
        }

        //give it back
        return resulting_vector;
    }

    private Vector2 v_lerp(Vector2 v, Vector2 tv, float t) {
        float x = lerp(v.x, tv.x, t);
        float y = lerp(v.y, tv.y, t);

        return new Vector2(x, y);
    }

    private float lerp(float a, float b, float f) {
        return (a + f * (b - a));
    }

    private Vector2 getInputVector(GameInput input) {
        if (input == null) {
            return new Vector2();
        }

        float x_dir = 0;
        float y_dir = 0;
        if (input.left) {
            x_dir -= 1;
        }
        if (input.right) {
            x_dir += 1;
        }
        if (input.up) {
            y_dir += 1;
        }
        if (input.down) {
            y_dir -= 1;
        }

        return physics_movement_vector_from_direction(x_dir, y_dir);
    }

    private Vector2 physics_movement_vector_from_direction(float x, float y) {
        return new Vector2(
                x * this.playerspeed * 0.015F,
                y * this.playerspeed * 0.015F
        );
    }

    private void server_update() {
        // Update the state of our local clock to match the timer
        this.server_time = this.local_time;

        // Make a snapshot of the current state, for updating the clients
        this.laststate = new ServerUpdate();
        this.laststate.hostPosition = this.self.pos;
        this.laststate.clientPosition = this.other.pos;
        this.laststate.hostInputSequence = this.self.last_input_seq;
        this.laststate.clientInputSequence = this.other.last_input_seq;
        this.laststate.serverTime = this.server_time;

        //Send the snapshot to the 'host' player
        if (this.self.instance != null) {
            this.self.instance.send(new ServerUpdatePacket(this.laststate));
        }

        //Send the snapshot to the 'client' player
        if (this.other.instance != null) {
            this.other.instance.send(new ServerUpdatePacket(this.laststate));
        }
    }

    private void create_physics_simulation() {
        executor.scheduleAtFixedRate(() -> {
            this._pdt = (float) ((System.currentTimeMillis() - this._pdte) / 1000F);
            this._pdte = System.currentTimeMillis();
            if (this.server) {
                this.server_update_physics();
            } else {
                this.client_update_physics();
            }

        }, 15, 15, TimeUnit.MILLISECONDS);
    }

    private void server_update_physics() {
        // Handle player one
        this.self.old_state.pos = this.self.pos;
        Vector2 new_dir = this.process_input(this.self);
        this.self.pos = this.v_add(this.self.old_state.pos, new_dir);

        // Handle player two
        this.other.old_state.pos = this.other.pos;
        Vector2 other_new_dir = this.process_input(this.other);
        this.other.pos = this.v_add(this.other.old_state.pos, other_new_dir);

        //Keep the physics position in the world
        this.check_collision(this.self);
        this.check_collision(this.other);

        this.self.inputs.clear(); //we have cleared the input buffer, so remove this
        this.other.inputs.clear(); //we have cleared the input buffer, so remove this
    }

    private void client_update_physics() {
        //Fetch the new direction from the input buffer,
        //and apply it to the state so we can smooth it in the visual state

        if (this.client_predict) {
            this.self.old_state.pos = this.self.cur_state.pos;
            Vector2 nd = this.process_input(this.self);
            this.self.cur_state.pos = this.v_add(this.self.old_state.pos, nd);
            this.self.state_time = this.local_time;
        }
    }

    public void client_onserverupdate_recieved(ServerUpdate data) {
        //Lets clarify the information we have locally. One of the players is 'hosting' and
        //the other is a joined in client, so we name these host and client for making sure
        //the positions we get from the server are mapped onto the correct local sprites
        Player player_host = this.self.host ? this.self : this.other;
        Player player_client = this.self.host ? this.other : this.self;
        Player this_player = this.self;

        //Store the server time (this is offset by the latency in the network, by the time we get it)
        this.server_time = data.time;
        //Update our local offset time from the last server update
        this.client_time = this.server_time - (this.net_offset / 1000);

        //One approach is to set the position directly as the server tells you.
        //This is a common mistake and causes somewhat playable results on a local LAN, for example,
        //but causes terrible lag when any ping/latency is introduced. The player can not deduce any
        //information to interpolate with so it misses positions, and packet loss destroys this approach
        //even more so. See 'the bouncing ball problem' on Wikipedia.
        if (this.naive_approach) {
            if (data.hostPosition != null) {
                player_host.pos = data.hostPosition;
            }

            if (data.clientPosition != null) {
                player_client.pos = data.clientPosition;
            }
        } else {
            //Cache the data from the server,
            //and then play the timeline
            //back to the player with a small delay (net_offset), allowing
            //interpolation between the points.
            this.server_updates.add(data);

            //we limit the buffer in seconds worth of updates
            //60fps*buffer seconds = number of samples
            if (this.server_updates.size() >= (60 * this.buffer_size)) {
                this.server_updates.removeFirst();
            }

            //We can see when the last tick we know of happened.
            //If client_time gets behind this due to latency, a snap occurs
            //to the last tick. Unavoidable, and a reallly bad connection here.
            //If that happens it might be best to drop the game after a period of time.
            this.oldest_tick = this.server_updates.get(0).time;

            //Handle the latest positions from the server
            //and make sure to correct our local predictions, making the server have final say.
            this.client_process_net_prediction_correction();

        } //non naive
    }

    public void client_onreadygame(float server_time) {
        Player player_host = this.self.host ? this.self : this.other;
        Player player_client = this.self.host ? this.other : this.self;

        this.local_time = server_time + this.net_latency;

        //Store their info colors for clarity. server is always blue
        player_host.color = Color.BLUE;
        player_client.color = Color.ORANGE;

        //Update their information
        player_host.state = Player.State.local_pos_host;
        player_client.state = Player.State.local_pos_client;
        this.self.state = Player.State.you;
    }

    public void client_onconnected(String clientId) {
        //The server responded that we are now in a game,
        //this lets us store the information about ourselves and set the colors
        //to show we are now ready to be playing.
        this.self.id = clientId;
        this.self.color = Color.RED;
        this.self.state = Player.State.connected;
        this.self.online = true;
    }

    public void client_ondisconnect() {
        //When we disconnect, we don't know if the other player is
        //connected or not, and since we aren't, everything goes to offline
        this.self.color = Color.GRAY;
        this.self.state = Player.State.not_connected;
        this.self.online = false;

        this.other.color = Color.GRAY;
        this.other.state = Player.State.not_connected;
    }

    private void client_connect_to_server() {
        this.socket = new ClientSocket(this);
        this.self.state = Player.State.connecting;
        this.socket.connect();
    }

    private void client_process_net_prediction_correction() {
        if (this.server_updates.isEmpty()) {
            //No updates...
            return;
        }

        //The most recent server update
        ServerUpdate latest_server_data = this.server_updates.get(this.server_updates.size() - 1);

        //Our latest server position
        Vector2 my_server_pos = this.self.host ? latest_server_data.hostPosition : latest_server_data.clientPosition;

        //Update the debug server position block
        this.server_pos_self.pos = my_server_pos;

        //here we handle our local input prediction ,
        //by correcting it with the server and reconciling its differences
        int my_last_input_on_server = this.self.host ? latest_server_data.hostInputSequence : latest_server_data.clientInputSequence;
        if (my_last_input_on_server > 0) {
            //The last input sequence index in my local input list
            int lastinputseq_index = -1;
            //Find this input in the list, and store the index
            for (int i = 0; i < this.self.inputs.size(); ++i) {
                if (this.self.inputs.get(i).seq == my_last_input_on_server) {
                    lastinputseq_index = i;
                    break;
                }
            }

            //Now we can crop the list of any updates we have already processed
            if (lastinputseq_index != -1) {
                //so we have now gotten an acknowledgement from the server that our inputs here have been accepted
                //and that we can predict from this known position instead

                //remove the rest of the inputs we have confirmed on the server
                int number_to_clear = Math.abs(lastinputseq_index - (-1));
                for (int i = 0; i < number_to_clear; i++) {
                    this.self.inputs.removeFirst();
                }

                //The player is now located at the new server position, authoritive server
                this.self.cur_state.pos = my_server_pos;
                this.self.last_input_seq = lastinputseq_index;
                //Now we reapply all the inputs that we have locally that
                //the server hasn't yet confirmed. This will 'keep' our position the same,
                //but also confirm the server position at the same time.
                this.client_update_physics();
                this.client_update_local_position();

            }
        }
    }

    private void check_collision(Player item) {
        //Left wall.
        if (item.pos.x <= 0) {
            item.pos.x = 0;
        }

        //Right wall
        if (item.pos.x >= Game.VIEW_WIDTH) {
            item.pos.x = Game.VIEW_WIDTH;
        }

        //Roof wall.
        if (item.pos.y <= 0) {
            item.pos.y = 0;
        }

        //Floor wall
        if (item.pos.y >= Game.VIEW_HEIGHT) {
            item.pos.y = Game.VIEW_HEIGHT;
        }
    }

    private void create_timer() {
        this.executor.scheduleAtFixedRate(() -> {
            this._dt = System.currentTimeMillis() - this._dte;
            this._dte = System.currentTimeMillis();
            this.local_time += this._dt / 1000.0;
        }, 4, 4, TimeUnit.MILLISECONDS);
    }

    private void client_create_ping_timer() {
        this.executor.scheduleAtFixedRate(() -> {
            this.last_ping_time = System.currentTimeMillis() - this.fake_lag;
            this.socket.send(new PingPacket(this.last_ping_time));
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    public void client_onping(long pingTime) {
        this.net_ping = System.currentTimeMillis() - pingTime;
        this.net_latency = this.net_ping / 2;
    }

    private Vector2 v_add(Vector2 a, Vector2 b) {
        return a.add(b);
    }

    public void client_onhostgame(float server_time) {
        //The server sends the time when asking us to host, but it should be a new game.
        //so the value will be really small anyway (15 or 16ms)

        //Get an estimate of the current time on the server
        this.local_time = server_time + this.net_latency;

        //Set the flag that we are hosting, this helps us position respawns correctly
        this.self.host = true;

        //Update debugging information to display state
        this.self.state = Player.State.host_waiting;
        this.self.color = Color.ORANGE;

        //Make sure we start in the correct place as the host.
        this.client_reset_positions();
    }

    public void client_onjoingame(float time) {
        //We are not the host
        this.self.host = false;

        //Update the local state
        this.self.state = Player.State.client_waiting;
        this.self.color = Color.PURPLE;

        //Make sure the positions match servers and other clients
        this.client_reset_positions();
    }

    private void client_reset_positions() {
        Player player_host = this.self.host ? this.self : this.other;
        Player player_client = this.self.host ? this.other : this.self;

        //Host always spawns at the top left.
        player_host.pos = new Vector2(20, 20);
        player_client.pos = new Vector2(500, 200);

        //Make sure the local player physics is updated
        this.self.old_state.pos = this.self.pos;
        this.self.pos = this.self.pos;
        this.self.cur_state.pos = this.self.pos;

        //Position all debug view items to their owners position
        this.server_pos_self.pos = this.self.pos;

        this.server_pos_other.pos = this.other.pos;
        this.pos_other.pos = this.other.pos;
    }

    public void stop_update() {
        this.executor.shutdownNow();
    }

    private void client_create_configuration() {
        this.naive_approach = false;        //Whether or not to use the naive approach

        this.client_predict = true;         //Whether or not the client is predicting input
        this.input_seq = 0;                 //When predicting client inputs, we store the last input as a sequence number
        this.client_smoothing = true;       //Whether or not the client side prediction tries to smooth things out
        this.client_smooth = 25;            //amount of smoothing to apply to client update dest

        this.net_latency = 0L;           //the latency between the client and the server (ping/2)
        this.net_ping = 0L;              //The round trip time from here to the server,and back
        this.last_ping_time = 0L;        //The time we last sent a ping
        this.fake_lag = 0L;                //If we are simulating lag, this applies only to the input client (not others)

        this.net_offset = 100;              //100 ms latency between server and client interpolation for other clients
        this.buffer_size = 2;               //The size of the server history to keep for rewinding/interpolating.
        this.target_time = 0.01F;            //the time where we want to be in the server timeline
        this.oldest_tick = 0.01F;            //the last time tick we have available in the buffer

        this.client_time = 0.01F;            //Our local 'clock' based on server time - client interpolation(net_offset).
        this.server_time = 0.01F;            //The time the server reported it was at, last we heard from it
    }

}
