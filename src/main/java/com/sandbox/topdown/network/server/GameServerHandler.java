package com.sandbox.topdown.network.server;

import com.sandbox.topdown.framework.GameEntity;
import com.sandbox.topdown.network.packet.Packet;
import com.sandbox.topdown.network.packet.UpdatePositionCommand;
import com.sandbox.topdown.network.packet.UpdateSessionCommand;
import com.sandbox.topdown.network.packet.WelcomePacket;
import com.sandbox.topdown.network.packet.event.PositionEvent;
import com.sandbox.topdown.network.packet.event.SessionEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatchers;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Maarten
 */
@Sharable
public class GameServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(GameServerHandler.class);

    private static final AttributeKey<PlayerSession> SESSION_KEY = AttributeKey.newInstance("session");
    private static final AttributeKey<GameEntity> ENTITY_KEY = AttributeKey.newInstance("entity");

    private final ChannelGroup channels;

    public GameServerHandler(ChannelGroup channels) {
        this.channels = channels;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        LOG.info("New client added: {}", channel.remoteAddress());

        this.channels.add(channel);

        channel.write(new WelcomePacket("Welcome, there are currently " + this.channels.size() + " players online"));
        getSessions().forEach((session) -> {
            channel.write(new SessionEvent(SessionEvent.TYPE_CREATED, session));
        });
        channel.flush();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        PlayerSession session = getSession(channel);
        this.channels.remove(channel);
        if (session != null) {
            this.channels.writeAndFlush(new SessionEvent(SessionEvent.TYPE_DELETED, session));
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Packet packet = (Packet) msg;
        Channel channel = ctx.channel();

        if (packet.getId() == Packet.ID_UPDATE_SESSION) {
            updateSession(channel, (UpdateSessionCommand) packet);
        } else if (packet.getId() == Packet.ID_UPDATE_POSITION) {
            updatePosition(channel, (UpdatePositionCommand) packet);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        LOG.error("Failed to handle event", cause);

        ctx.close();
    }

    private void updateSession(Channel channel, UpdateSessionCommand command) {
        Attribute<PlayerSession> attr = channel.attr(SESSION_KEY);
        PlayerSession session = attr.get();
        SessionEvent event;
        if (session == null) {
            session = new PlayerSession(UUID.randomUUID().toString());
            event = new SessionEvent(SessionEvent.TYPE_CREATED, session);
            attr.set(session);
        } else {
            event = new SessionEvent(SessionEvent.TYPE_UPDATED, session);
        }

        session.setName(command.getName());

        this.channels.write(event, ChannelMatchers.isNot(channel));
        this.channels.flush();
    }

    private void updatePosition(Channel channel, UpdatePositionCommand command) {
        Attribute<GameEntity> attr = channel.attr(ENTITY_KEY);
        GameEntity entity = attr.get();
        if (entity == null) {
            attr.set(entity = new GameEntity());
        }

        entity.x = command.x;
        entity.y = command.y;
        entity.direction = command.direction;

        PlayerSession session = getSession(channel);
        if (session == null) {
            throw new IllegalStateException("Player does not have a session");
        }

        PositionEvent event = new PositionEvent(session.getId(), command.x, command.y, command.direction);
        this.channels.write(event, ChannelMatchers.isNot(channel));
        this.channels.flush();
    }

    public PlayerSession getSession(Channel channel) {
        Attribute<PlayerSession> attr = channel.attr(SESSION_KEY);
        PlayerSession session = attr.get();

        return session;
    }

    public List<PlayerSession> getSessions() {
        List<PlayerSession> sessions = new ArrayList<>();
        this.channels.forEach((channel) -> {
            PlayerSession session = getSession(channel);
            if (session != null) {
                sessions.add(session);
            }
        });

        return sessions;
    }

}
