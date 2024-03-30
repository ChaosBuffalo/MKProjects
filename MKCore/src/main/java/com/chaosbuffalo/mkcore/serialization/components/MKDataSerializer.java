package com.chaosbuffalo.mkcore.serialization.components;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;

import java.nio.ByteBuffer;

public class MKDataSerializer<T> {

    private final WireSerializer<T> wireSerializer;

    public MKDataSerializer(WireSerializer<T> wireSerializer) {
        this.wireSerializer = wireSerializer;
    }

    public MKDataSerializer(EntityDataSerializer<T> serializer) {
        this.wireSerializer = WireSerializer.wrap(serializer);
    }

    public void write(FriendlyByteBuf buf, T value) {
        wireSerializer.write(buf, value);
    }

    public T read(FriendlyByteBuf buf) {
        return wireSerializer.read(buf);
    }

    public <D> D serialize(DynamicOps<D> ops, T value) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        wireSerializer.write(buf, value);
        return ops.createByteList(buf.nioBuffer());
    }

    public <D> T deserialize(Dynamic<D> dynamic, T defaultValue) {
        ByteBuffer bb = dynamic.asByteBuffer();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bb));
        return wireSerializer.read(buf);
    }

    public interface WireSerializer<T> {
        void write(FriendlyByteBuf buf, T value);

        T read(FriendlyByteBuf buf);

        static <T> WireSerializer<T> wrap(EntityDataSerializer<T> serializer) {
            return new WireSerializer<>() {
                @Override
                public void write(FriendlyByteBuf buf, T value) {
                    serializer.write(buf, value);
                }

                @Override
                public T read(FriendlyByteBuf buf) {
                    return serializer.read(buf);
                }
            };
        }
    }
}
