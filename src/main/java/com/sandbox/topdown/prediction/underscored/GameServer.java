package com.sandbox.topdown.prediction.underscored;

import com.sandbox.topdown.prediction.underscored.packet.InputPacket;
import com.sandbox.topdown.prediction.underscored.packet.Packet;
import com.sandbox.topdown.prediction.underscored.packet.ServerPacket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maarten
 */
public class GameServer extends GameInstance {

    private static final Logger LOG = LoggerFactory.getLogger(GameServer.class);

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private long fake_latency = 0;
    private float local_time = 0;

    private long _dt = System.currentTimeMillis();
    private long _dte = System.currentTimeMillis();
    private Map<String, GameInstance> games = new HashMap<>();
    private LinkedList<PacketHolder> messages = new LinkedList<>();

    public GameServer() {
        executor.scheduleAtFixedRate(() -> {
            this._dt = System.currentTimeMillis() - _dte;
            this._dte = System.currentTimeMillis();
            this.local_time += _dt / 1000.0;
        }, 4, 4, TimeUnit.MILLISECONDS);
    }

    private void createGame(PlayerClient player) {
        GameInstance thegame = new GameInstance(
                UUID.randomUUID().toString(),
                player,
                null,
                1
        );

        games.put(thegame.id, thegame);

        //Create a new game core instance, this actually runs the
        //game code like collisions and such.
        thegame.gamecore = new GameCore(thegame);

        //Start updating the game loop on the server
        thegame.gamecore.update();

        //tell the player that they are now the host
        player.send(new ServerPacket(thegame.gamecore.local_time, ServerPacket.MessageType.HOSTING));
        player.game = thegame;
        player.hosting = true;
    }

    public void findGame(PlayerClient player) {
        LOG.info("[{}] Looking for a game. We have: {}", player.userid, this.games.size());

        //so there are games active,
        //lets see if one needs another player
        AtomicBoolean joined_a_game = new AtomicBoolean();
        if (!this.games.isEmpty()) {

            //Check the list of games for an open game
            this.games.forEach((gameid, game_instance) -> {
                //If the game is a player short
                if (game_instance.player_count < 2) {
                    //someone wants us to join!
                    joined_a_game.set(true);

                    //increase the player count and store
                    //the player as the client of this game
                    game_instance.player_client = player;
                    game_instance.gamecore.other.instance = player;
                    game_instance.player_count++;

                    //start running the game on the server,
                    //which will tell them to respawn/start
                    this.startGame(game_instance);
                }
            });

        }

        if (!joined_a_game.get()) {
            this.createGame(player);
        }
    }

    private void startGame(GameInstance game) {
        //right so a game has 2 players and wants to begin
        //the host already knows they are hosting,
        //tell the other client they are joining a game

        game.player_client.send(new ServerPacket(
                game.gamecore.local_time,
                ServerPacket.MessageType.JOINED,
                game.player_host.userid
        ));

        //now we tell both that the game is ready to start
        //clients will reset their positions in this case.
        ServerPacket start = new ServerPacket(
                game.gamecore.local_time,
                ServerPacket.MessageType.START
        );
        game.player_client.send(start);
        game.player_host.send(start);
    }

    public void endGame(String gameid, String userid) {
        GameInstance thegame = this.games.get(gameid);
        if (thegame == null) {
            LOG.debug("Trying to end non existing game: {}", gameid);
        }

        //stop the game updates immediate
        thegame.gamecore.stop_update();

        //if the game has two players, the one is leaving
        if (thegame.player_count > 1) {
            //send the players the message the game is ending
            if (userid.equals(thegame.player_host.userid)) {
                //the host left, oh snap. Lets try join another game
                if (thegame.player_client != null) {
                    //tell them the game is over
                    thegame.player_client.send(new ServerPacket(local_time, ServerPacket.MessageType.END));
                    //now look for/create a new game.
                    this.findGame(thegame.player_client);
                }
            } else {
                //the other player left, we were hosting
                if (thegame.player_host != null) {
                    //tell the client the game is ended
                    thegame.player_host.send(new ServerPacket(local_time, ServerPacket.MessageType.END));
                    //i am no longer hosting, this game is going down
                    thegame.player_host.hosting = false;
                    //now look for/create a new game.
                    findGame(thegame.player_host);
                }
            }
        }

        this.games.remove(gameid);

        LOG.info("game removed. there are now {} games", this.games.size());

    }

    public void onMessage(PlayerClient client, Packet message) {
        if (this.fake_latency > 0 && message.getType() == Packet.Type.INPUT) {
            //store all input message
            messages.add(new PacketHolder(message, client));

            this.executor.schedule(() -> {
                PacketHolder holder = messages.removeFirst();
                _onMessage(holder.client, holder.packet);
            }, fake_latency, TimeUnit.MILLISECONDS);
        } else {
            _onMessage(client, message);
        }
    }

    private void _onMessage(PlayerClient client, Packet packet) {
        if (packet.getType() == Packet.Type.INPUT) {
            //Input handler will forward this
            this.onInput(client, (InputPacket) packet);
        } else if (packet.getType() == Packet.Type.PING) {
            client.send(packet);
        }
    }

    private void onInput(PlayerClient client, InputPacket packet) {
        //the client should be in a game, so
        //we can tell that game to handle the input
        if (client != null && client.game != null && client.game.gamecore != null) {
            client.game.gamecore.handle_server_input(client, packet.getInput());
        }
    }

    static class PacketHolder {

        public final Packet packet;
        public final PlayerClient client;

        public PacketHolder(Packet packet, PlayerClient client) {
            this.packet = packet;
            this.client = client;
        }

    }

}
