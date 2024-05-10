package com.chaosbuffalo.mkcore.utils.location;

import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class PerpendicularLineLocationProvider extends LocationProvider{
    protected final Vec3 offset;
    protected final float percentEyeHeight;
    protected final float distance;

    public static final Codec<PerpendicularLineLocationProvider> CODEC = RecordCodecBuilder.<PerpendicularLineLocationProvider>mapCodec(builder -> builder.group(
            Vec3.CODEC.fieldOf("offset").forGetter(i -> i.offset),
            Codec.FLOAT.fieldOf("percentEyeHeight").forGetter(i -> i.percentEyeHeight),
            Codec.INT.fieldOf("count").forGetter(i -> i.max),
            Codec.FLOAT.fieldOf("distance").forGetter(i -> i.distance)
    ).apply(builder, PerpendicularLineLocationProvider::new)).codec();

    public PerpendicularLineLocationProvider(Vec3 offset, float percentEyeHeight, int count, float distance) {
        super(count);
        this.offset = offset;
        this.percentEyeHeight = percentEyeHeight;
        this.distance = distance;
    }

    @Override
    public LocationProviderType<? extends LocationProvider> getType() {
        return LocationProviderTypes.PERPENDICULAR_LINE_LOCATION.get();
    }

    @Override
    public WorldLocationResult getPosition(Entity entity, int index) {
        Vec3 startPos = entity.position().add(new Vec3(0, entity.getEyeHeight() * percentEyeHeight, 0));
        Vec3 offsetPos = startPos.add(Vec3.directionFromRotation(entity.getRotationVector()).multiply(offset));
        Vec3 entityCenter = new Vec3(entity.getX(), offsetPos.y, entity.getZ());
        Vec3 normal = offsetPos.subtract(entityCenter).normalize();
        Vec3 up = new Vec3(0.0, 1.0, 0.0);
        Vec3 cross = normal.cross(up).normalize();
        float lerpV = ((float)index) / (max - 1);
        float distValue = MathUtils.lerp(-distance / 2.0f, distance / 2.0f, lerpV);
        return new WorldLocationResult(offsetPos.add(cross.scale(distValue)), entity.getRotationVector());
    }
}
