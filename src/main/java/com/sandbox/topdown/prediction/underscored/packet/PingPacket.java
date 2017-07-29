package com.sandbox.topdown.prediction.underscored.packet;

/**
 *
 * @author Maarten
 */
public class PingPacket extends Packet {

    private long currentTime;

    public PingPacket() {
        super(Type.PING);
    }

    public PingPacket(long currentTime) {
        super(Type.PING);
        this.currentTime = currentTime;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

}
