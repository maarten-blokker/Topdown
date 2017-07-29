package com.sandbox.topdown.prediction.underscored.packet;

import com.sandbox.topdown.prediction.underscored.PlayerClient;

/**
 *
 * @author Maarten
 */
public class Packet {

    public enum Type {
        SERVER, INPUT, UPDATE, PING;
    }

    public PlayerClient client;
    private final Type type;

    public Packet(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

}
