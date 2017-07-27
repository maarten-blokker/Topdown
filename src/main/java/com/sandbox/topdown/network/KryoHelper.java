package com.sandbox.topdown.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.sandbox.topdown.network.packet.Packet;
import com.sandbox.topdown.network.packet.UpdatePositionCommand;
import com.sandbox.topdown.network.packet.UpdateSessionCommand;
import com.sandbox.topdown.network.packet.WelcomePacket;
import com.sandbox.topdown.network.packet.event.PositionEvent;
import com.sandbox.topdown.network.packet.event.ServerEvent;
import com.sandbox.topdown.network.packet.event.SessionEvent;

/**
 *
 * @author Maarten
 */
public class KryoHelper {

    public static KryoFactory createFactory() {
        return () -> {
            Kryo kryo = new Kryo();

            kryo.register(Packet.class);
            kryo.register(UpdatePositionCommand.class);
            kryo.register(UpdateSessionCommand.class);
            kryo.register(WelcomePacket.class);

            kryo.register(PositionEvent.class);
            kryo.register(ServerEvent.class);
            kryo.register(SessionEvent.class);

            return kryo;
        };
    }
}
