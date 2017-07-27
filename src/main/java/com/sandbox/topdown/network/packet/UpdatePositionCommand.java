package com.sandbox.topdown.network.packet;

/**
 *
 * @author Maarten
 */
public class UpdatePositionCommand extends Packet {

    public float x;
    public float y;
    public float direction;

    public UpdatePositionCommand() {
        super(ID_UPDATE_POSITION);
    }

    public UpdatePositionCommand(float x, float y, float direction) {
        this();
        this.x = x;
        this.y = y;
        this.direction = direction;
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
