package com.chaosbuffalo.mkcore.utils.location;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class SingleLocationProvider extends LocationProvider {
    protected final Vec3 offset;
    protected final float percentEyeHeight;

    public static final Codec<SingleLocationProvider> CODEC = RecordCodecBuilder.<SingleLocationProvider>mapCodec(builder -> builder.group(
            Vec3.CODEC.fieldOf("offset").forGetter(i -> i.offset),
            Codec.FLOAT.fieldOf("percentEyeHeight").forGetter(i -> i.percentEyeHeight)
    ).apply(builder, SingleLocationProvider::new)).codec();

    public SingleLocationProvider(Vec3 offset, float percentEyeHeight) {
        super(1);
        this.offset = offset;
        this.percentEyeHeight = percentEyeHeight;
    }

    @Override
    public LocationProviderType<? extends LocationProvider> getType() {
        return LocationProviderTypes.SINGLE_LOCATION.get();
    }

    @Override
    public WorldLocationResult getPosition(Entity entity, int index) {
        Vec3 startPos = entity.position().add(new Vec3(0, entity.getEyeHeight() * percentEyeHeight, 0));
        startPos = startPos.add(Vec3.directionFromRotation(entity.getRotationVector()).multiply(offset));
        return new WorldLocationResult(startPos, entity.getRotationVector());
    }
}
