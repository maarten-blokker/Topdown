package com.sandbox.topdown.prediction.underscored;

import com.sandbox.topdown.framework.event.EventBus;
import com.sandbox.topdown.network.KryoDecodingHandler;
import com.sandbox.topdown.network.KryoEncodingHandler;
import com.sandbox.topdown.network.client.GameClient;
import com.sandbox.topdown.network.client.GameClientHandler;
import com.sandbox.topdown.prediction.underscored.packet.Packet;
import com.sandbox.topdown.prediction.underscored.packet.PingPacket;
import com.sandbox.topdown.prediction.underscored.packet.ServerPacket;
import com.sandbox.topdown.prediction.underscored.packet.ServerUpdatePacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maarten
 */
public class ClientSocket {

    private static final Logger LOG = LoggerFactory.getLogger(ClientSocket.class);

    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final GameCore core;
    private ChannelFuture channelFuture;

    public ClientSocket(GameCore core) {
        this.core = core;
    }

    public void send(Packet packet) {
        channelFuture.channel().writeAndFlush(packet);
    }

    public void connect() {
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
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
                    ch.pipeline().addLast("logic", new Handler());
                }
            });

            // Start the client.
            this.channelFuture = b.connect(new InetSocketAddress(4004)).sync();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    class Handler extends ChannelInboundHandlerAdapter {

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            Packet packet = (Packet) msg;
            switch (packet.getType()) {
                case SERVER:
                    ServerPacket serverPacket = (ServerPacket) packet;
                    switch (serverPacket.getMesssageType()) {
                        case CONNECTED:
                            core.client_onconnected(serverPacket.getPayload());
                            break;
                        case HOSTING:
                            core.client_onhostgame(serverPacket.getTime());
                            break;
                        case JOINED:
                            core.client_onjoingame(serverPacket.getTime());
                            break;
                        case START:
                            core.client_onreadygame(serverPacket.getTime());
                            break;
                        case END:
                            core.client_ondisconnect();
                            break;
                    }
                    break;
                case UPDATE:
                    core.client_onserverupdate_recieved(((ServerUpdatePacket) packet).getUpdate());
                    break;
                case PING:
                    PingPacket pingPacket = (PingPacket) packet;
                    core.client_onping(pingPacket.getCurrentTime());
                    break;
                default:
                    break;
            }
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            core.client_ondisconnect();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOG.error("Error in client handler", cause);
            ctx.close();
        }

    }

}
