package com.sandbox.topdown.network.packet.event;

import com.sandbox.topdown.network.packet.Packet;

/**
 *
 * @author Maarten
 */
public class ServerEvent extends Packet {

    public static final int TYPE_CLOSING = 1;

    private int type;

    public ServerEvent() {
        super(ID_EVENT_SERVER);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
