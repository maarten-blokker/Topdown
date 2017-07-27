package com.sandbox.topdown.network.packet;

/**
 *
 * @author Maarten
 */
public abstract class Packet {

    public static final int ID_JOIN = 1;

    public static final int ID_UPDATE_SESSION = 2;
    public static final int ID_UPDATE_POSITION = 3;

    public static final int ID_EVENT_SERVER = 4;
    public static final int ID_EVENT_SESSION = 5;
    public static final int ID_EVENT_POSITION = 6;

    private int id;

    public Packet(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
