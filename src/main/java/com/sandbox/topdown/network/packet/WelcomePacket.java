package com.sandbox.topdown.network.packet;

/**
 *
 * @author Maarten
 */
public class WelcomePacket extends Packet {

    private String message;

    public WelcomePacket() {
        super(Packet.ID_JOIN);
    }

    public WelcomePacket(String message) {
        super(Packet.ID_JOIN);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
