package com.sandbox.topdown.prediction.underscored;

import com.sandbox.topdown.prediction.underscored.packet.ServerPacket;
import com.badlogic.gdx.graphics.Color;
import com.sandbox.topdown.framework.GameEntity;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maarten
 */
public class Player extends GameEntity {

    private static final Logger LOG = LoggerFactory.getLogger(Player.class);

    public enum State {
        server_pos,
        dest_pos,
        not_connected,
        connecting,
        connected,
        host_waiting,
        client_waiting,
        local_pos_host,
        local_pos_client,
        you;
    }

    public String id;
    public LinkedList<GameInput> inputs = new LinkedList<>();
    public float width;
    public float height;
    public Color color;
    public State state;

    public float state_time;
    public PlayerState cur_state = new PlayerState();
    public PlayerState old_state = new PlayerState();
    public float last_input_time;
    public int last_input_seq;
    public boolean online;

    public boolean host;

    private final GameCore core;
    public PlayerClient instance;

    public Player(GameCore core) {
        this.core = core;
        this.instance = null;
    }

    public Player(GameCore core, PlayerClient player_instance) {
        this.core = core;
        this.instance = player_instance;
    }

    public void send(ServerPacket serverPacket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
