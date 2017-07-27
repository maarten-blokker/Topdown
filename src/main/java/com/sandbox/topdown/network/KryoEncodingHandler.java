package com.sandbox.topdown.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.sandbox.topdown.network.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.ByteArrayOutputStream;

/**
 *
 * @author Maarten
 */
public class KryoEncodingHandler extends MessageToByteEncoder<Packet> {

    private static final KryoPool POOL = new KryoPool.Builder(KryoHelper.createFactory())
            .softReferences()
            .build();

    private final Output output;

    public KryoEncodingHandler(int bufferSize, int maxBufferSize) {
        this.output = new Output(bufferSize, maxBufferSize);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        output.setOutputStream(bout);

        int total = POOL.run((kryo) -> {
            kryo.writeClassAndObject(output, msg);
            output.flush();
            return bout.size();
        });

        out.writeBytes(bout.toByteArray(), 0, total);

    }
}
