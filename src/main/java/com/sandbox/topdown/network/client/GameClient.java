package com.sandbox.topdown.network.client;

import com.sandbox.topdown.network.KryoDecodingHandler;
import com.sandbox.topdown.network.KryoEncodingHandler;
import com.sandbox.topdown.network.packet.Packet;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Maarten
 */
public class GameClient {

    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private final List<PacketListener> listeners = new CopyOnWriteArrayList<>();
    private ChannelFuture channelFuture;

    public GameClient() {

    }

    public void connect(SocketAddress address) {
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast("prepender", new LengthFieldPrepender(2));
                    ch.pipeline().addLast("framer", new LengthFieldBasedFrameDecoder(
                            10000, //frame length
                            0, // lengthFieldOffset,
                            2, // lengthFieldLength,
                            0, // lengthAdjustment,
                            2 // initialBytesToStrip
                    ));
                    ch.pipeline().addLast("encoder", new KryoEncodingHandler(4 * 1024, 16 * 1024));
                    ch.pipeline().addLast("decoder", new KryoDecodingHandler());
                    ch.pipeline().addLast("logic", new GameClientHandler(GameClient.this));
                }
            });

            // Start the client.
            this.channelFuture = b.connect(address).sync();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public void disconnect() {
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public void addPacketListener(PacketListener listener) {
        listeners.add(listener);
    }

    public void removePacketListener(PacketListener listener) {
        listeners.remove(listener);
    }

    public void firePacketReceived(Packet packet) {
        listeners.forEach((l) -> l.onPacket(packet));
    }

    public void send(Packet packet) {
        try {
            channelFuture.channel().writeAndFlush(packet);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
