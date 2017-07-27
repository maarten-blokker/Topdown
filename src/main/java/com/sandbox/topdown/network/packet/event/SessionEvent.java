package com.sandbox.topdown.network.packet.event;

import com.sandbox.topdown.network.packet.Packet;
import com.sandbox.topdown.network.server.PlayerSession;

/**
 *
 * @author Maarten
 */
public class SessionEvent extends Packet {

    public static final int TYPE_CREATED = 1;
    public static final int TYPE_UPDATED = 2;
    public static final int TYPE_DELETED = 3;

    private int type;
    private PlayerSession session;

    public SessionEvent() {
        super(ID_EVENT_SESSION);
    }

    public SessionEvent(int type, PlayerSession session) {
        super(ID_EVENT_SESSION);
        this.type = type;
        this.session = session;
    }

    public PlayerSession getSession() {
        return session;
    }

    public void setSession(PlayerSession session) {
        this.session = session;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
