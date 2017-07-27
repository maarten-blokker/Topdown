package com.sandbox.topdown.network.packet.event;

import com.sandbox.topdown.network.packet.Packet;

/**
 *
 * @author Maarten
 */
public class PositionEvent extends Packet {

    private String sessionId;
    private float x;
    private float y;
    private float direction;

    public PositionEvent() {
        super(ID_EVENT_POSITION);
    }

    public PositionEvent(String sessionId, float x, float y, float direction) {
        super(ID_EVENT_POSITION);
        this.sessionId = sessionId;
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getDirection() {
        return direction;
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }

}
