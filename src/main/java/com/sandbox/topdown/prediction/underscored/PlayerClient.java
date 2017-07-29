package com.sandbox.topdown.prediction.underscored;

import com.sandbox.topdown.prediction.underscored.packet.Packet;
import io.netty.channel.Channel;
import java.util.UUID;

/**
 *
 * @author Maarten
 */
public class PlayerClient {

    public String userid = UUID.randomUUID().toString();
    public boolean hosting;
    public GameInstance game;

    private final Channel channel;

    public PlayerClient(Channel channel) {
        this.channel = channel;
    }

    public void send(Packet serverPacket) {
        this.channel.writeAndFlush(serverPacket);
    }

}
