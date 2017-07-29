package com.sandbox.topdown.prediction.underscored.packet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maarten
 */
public class ServerPacket extends Packet {

    private static final Logger LOG = LoggerFactory.getLogger(ServerPacket.class);

    public enum MessageType {
        CONNECTED, HOSTING, JOINED, END, START;
    }

    private float time;
    private MessageType messageType;
    private String payload;

    public ServerPacket() {
        super(Type.SERVER);
    }

    public ServerPacket(float time, MessageType type) {
        super(Type.SERVER);
        this.messageType = type;
        this.time = time;
    }

    public ServerPacket(float time, MessageType type, String payload) {
        super(Type.SERVER);
        this.time = time;
        this.messageType = type;
        this.payload = payload;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public MessageType getMesssageType() {
        return messageType;
    }

    public void setMesssageType(MessageType type) {
        this.messageType = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

}
