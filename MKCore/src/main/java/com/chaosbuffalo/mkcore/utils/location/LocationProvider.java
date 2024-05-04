package com.chaosbuffalo.mkcore.utils.location;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public abstract class LocationProvider {
    protected final int max;
    public static final Codec<LocationProvider> CODEC = ExtraCodecs.lazyInitializedCodec(() ->
            MKCoreRegistry.LOC_PROVIDER_TYPES.getCodec().dispatch(LocationProvider::getType, LocationProviderType::codec));

    public LocationProvider(int max) {
        this.max = max;
    }
    public record WorldLocationResult(Vec3 worldPosition, Vec2 rotation, boolean isValid) {
        public WorldLocationResult(Vec3 worldPosition, Vec2 rotation, boolean isValid) {
            this.worldPosition = worldPosition;
            this.rotation = rotation;
            this.isValid = isValid;
        }

        public WorldLocationResult(Vec3 worldPosition, Vec2 rotation) {
            this(worldPosition, rotation, true);
        }

        public WorldLocationResult() {
            this(Vec3.ZERO, Vec2.ZERO, false);
        }
    }

    public abstract LocationProviderType<? extends LocationProvider> getType();

    public abstract WorldLocationResult getPosition(LivingEntity entity, Vec2 rotation, int index);
}
