package com.sandbox.topdown.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.sandbox.topdown.prediction.underscored.packet.Packet;
import com.sandbox.topdown.prediction.underscored.packet.InputPacket;
import com.sandbox.topdown.prediction.underscored.packet.PingPacket;
import com.sandbox.topdown.prediction.underscored.packet.ServerPacket;
import com.sandbox.topdown.prediction.underscored.packet.ServerUpdatePacket;

/**
 *
 * @author Maarten
 */
public class KryoHelper {

    public static KryoFactory createFactory() {
        return () -> {
            Kryo kryo = new Kryo();

            kryo.register(Packet.class);
            kryo.register(InputPacket.class);
            kryo.register(PingPacket.class);
            kryo.register(ServerPacket.class);
            kryo.register(ServerUpdatePacket.class);

            return kryo;
        };
    }
}
