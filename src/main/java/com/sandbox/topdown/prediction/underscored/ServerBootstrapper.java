package com.sandbox.topdown.prediction.underscored;

import com.sandbox.topdown.network.KryoDecodingHandler;
import com.sandbox.topdown.network.KryoEncodingHandler;
import com.sandbox.topdown.prediction.underscored.packet.Packet;
import com.sandbox.topdown.prediction.underscored.packet.ServerPacket;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
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
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maarten
 */
public class ServerBootstrapper {

    private static final Logger LOG = LoggerFactory.getLogger(ServerBootstrapper.class);

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final GameServer server;
    private ChannelFuture serverChannel;

    public ServerBootstrapper(GameServer server) {
        this.server = server;
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
                ch.pipeline().addLast("logic", new Handler());
            }
        };
    }
    private static final AttributeKey<PlayerClient> CLIENT_KEY = AttributeKey.newInstance("client");

    class Handler extends ChannelInboundHandlerAdapter {

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            PlayerClient client = new PlayerClient(ctx.channel());
            ctx.channel().attr(CLIENT_KEY).set(client);
            ctx.channel().writeAndFlush(new ServerPacket(0, ServerPacket.MessageType.CONNECTED, client.userid));

            server.findGame(client);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
            PlayerClient client = ctx.channel().attr(CLIENT_KEY).get();
            server.onMessage(client, (Packet) message);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            PlayerClient client = ctx.channel().attr(CLIENT_KEY).get();
            if (client.game != null && client.game.id != null) {
                server.endGame(client.game.id, client.userid);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            PlayerClient client = ctx.channel().attr(CLIENT_KEY).get();
            LOG.error("[" + client.userid + "] Caused an expection, closing connection", cause);
            ctx.channel().close();
        }

    }

}
