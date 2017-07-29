package com.sandbox.topdown.prediction.underscored;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maarten
 */
public class GameInstance {

    private static final Logger LOG = LoggerFactory.getLogger(GameInstance.class);

    public String id;
    public PlayerClient player_host;
    public PlayerClient player_client;
    public int player_count;
    public GameCore gamecore;

    public GameInstance() {
    }

    public GameInstance(String id, PlayerClient player_host, PlayerClient player_client, int player_count) {
        this.id = id;
        this.player_host = player_host;
        this.player_client = player_client;
        this.player_count = player_count;
    }

    public void emit(String key, Object value) {

    }
}
