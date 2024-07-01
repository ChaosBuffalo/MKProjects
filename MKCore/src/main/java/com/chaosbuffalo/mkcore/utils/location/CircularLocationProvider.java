package com.chaosbuffalo.mkcore.utils.location;

import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CircularLocationProvider extends LocationProvider{
    protected final Vec3 offset;
    protected final float percentEyeHeight;
    protected final float distance;
    protected final float minDegrees;
    protected final float maxDegrees;

    public static final Codec<CircularLocationProvider> CODEC = RecordCodecBuilder.<CircularLocationProvider>mapCodec(builder -> builder.group(
            Vec3.CODEC.fieldOf("offset").forGetter(i -> i.offset),
            Codec.FLOAT.fieldOf("percentEyeHeight").forGetter(i -> i.percentEyeHeight),
            Codec.INT.fieldOf("count").forGetter(i -> i.count),
            Codec.FLOAT.fieldOf("distance").forGetter(i -> i.distance),
            Codec.FLOAT.fieldOf("minDegrees").forGetter(i -> i.minDegrees),
            Codec.FLOAT.fieldOf("maxDegrees").forGetter(i -> i.maxDegrees)
    ).apply(builder, CircularLocationProvider::new)).codec();

    public CircularLocationProvider(Vec3 offset, float percentEyeHeight, int count, float distance) {
        this(offset, percentEyeHeight, count, distance, 0f, -360f);
    }

    public CircularLocationProvider(Vec3 offset, float percentEyeHeight, int count, float distance, float minDegrees, float maxDegrees) {
        super(count);
        this.offset = offset;
        this.percentEyeHeight = percentEyeHeight;
        this.distance = distance;
        this.minDegrees = minDegrees;
        this.maxDegrees = maxDegrees;
    }

    @Override
    public LocationProviderType<? extends LocationProvider> getType() {
        return LocationProviderTypes.CIRCULAR_LOCATION.get();
    }

    @Override
    public WorldLocationResult getPosition(Entity entity, int index) {
        Vec3 startPos = entity.position().add(new Vec3(0, entity.getEyeHeight() * percentEyeHeight, 0));
        Vec3 offsetPos = startPos.add(Vec3.directionFromRotation(new Vec2(0.0f, entity.getYRot())).multiply(offset));
        float degrees = MathUtils.lerp(minDegrees, maxDegrees, (float)(index) / (count - 1));
        float degreeOffset = entity.getYRot() - degrees;
        Vec3 offset = new Vec3(0.0, 0.0, 1.0).yRot(-degreeOffset * ((float) Math.PI / 180f)).scale(distance);
        return new WorldLocationResult(offsetPos.add(offset), new Vec2(0 , degreeOffset));
    }
}
