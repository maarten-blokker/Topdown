package com.sandbox.topdown.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.pool.KryoPool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

/**
 *
 * @author Maarten
 */
public class KryoDecodingHandler extends ByteToMessageDecoder {

    private final Input input = new Input();

    private static final KryoPool POOL = new KryoPool.Builder(KryoHelper.createFactory())
            .softReferences()
            .build();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
        int index = buffer.readerIndex();
        int total = buffer.readableBytes();

        byte[] data = new byte[total];
        buffer.readBytes(data, index, total);
        input.setBuffer(data);

        out.add(POOL.run((Kryo kryo) -> kryo.readClassAndObject(input)));
    }

}
