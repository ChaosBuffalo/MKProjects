package com.chaosbuffalo.mkcore.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class CodecUtils {

    public static DataResult<double[]> validateDoubleStreamSize(DoubleStream stream, int size) {
        double[] aint = stream.limit(size + 1).toArray();
        if (aint.length != size) {
            String s = "Input is not a list of " + size + " doubles";
            return aint.length >= size ? DataResult.error(s, Arrays.copyOf(aint, size)) : DataResult.error(s);
        } else {
            return DataResult.success(aint);
        }
    }

    public static final PrimitiveCodec<DoubleStream> DOUBLE_STREAM = new PrimitiveCodec<DoubleStream>() {
        @Override
        public <T> DataResult<DoubleStream> read(final DynamicOps<T> ops, final T input) {
            return ops.getStream(input).flatMap(stream -> {
                final List<T> list = stream.collect(Collectors.toList());
                if (list.stream().allMatch(element -> ops.getNumberValue(element).result().isPresent())) {
                    return DataResult.success(list.stream().mapToDouble(element ->
                            ops.getNumberValue(element).result().get().doubleValue()));
                }
                return DataResult.error("Some elements are not doubles: " + input);
            });
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final DoubleStream value) {
            return ops.createList(value.mapToObj(ops::createDouble));
        }

        @Override
        public String toString() {
            return "DoubleStream";
        }
    };

    public static final Codec<Vec3> VECTOR_3D_CODEC = CodecUtils.DOUBLE_STREAM.comapFlatMap(
            (stream) -> CodecUtils.validateDoubleStreamSize(stream, 3).map(
                    (componentArray) -> new Vec3(componentArray[0], componentArray[1], componentArray[2])),
            (vector) -> DoubleStream.of(vector.x(), vector.y(), vector.z()));
}
