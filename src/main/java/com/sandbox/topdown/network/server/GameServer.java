package com.sandbox.topdown.network.server;

import com.sandbox.topdown.network.KryoDecodingHandler;
import com.sandbox.topdown.network.KryoEncodingHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.util.List;

/**
 *
 * @author Maarten
 */
public class GameServer {

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final GameServerHandler handler;
    private ChannelFuture serverChannel;

    public GameServer() {
        this.handler = new GameServerHandler(channels);
    }

    public void listen(int port) throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(createInitializer())
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        // Bind and start to accept incoming connections.
        this.serverChannel = bootstrap.bind(port).sync();

    }

    public void stop() {
        try {
            // Wait until the server socket is closed.
            this.serverChannel.channel().closeFuture();
        } finally {
            this.workerGroup.shutdownGracefully();
            this.bossGroup.shutdownGracefully();
        }
    }

    public List<PlayerSession> getSessions() {
        return this.handler.getSessions();
    }

    private ChannelInitializer<SocketChannel> createInitializer() {
        return new ChannelInitializer<SocketChannel>() { // (4)
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
                ch.pipeline().addLast("logic", handler);
            }
        };
    }

    public static void main(String[] args) throws Exception {
        GameServer server = new GameServer();
        server.listen(9000);

        System.in.read();
    }

}
