package com.chaosbuffalo.mkcore.effects.utility;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.init.CoreEffects;
import com.chaosbuffalo.mkcore.network.MKParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.utils.MKNBTUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class MKParticleEffect extends MKEffect {

    public MKParticleEffect() {
        super(MobEffectCategory.NEUTRAL);
    }

    public static MKEffectBuilder<?> from(LivingEntity source, ResourceLocation animName, boolean includeSelf, Vec3 location) {
        return CoreEffects.PARTICLE.get().builder(source)
                .state(s -> s.setup(animName, includeSelf, location));
    }

    @Override
    public State makeState() {
        return new State();
    }

    @Override
    public MKEffectBuilder<State> builder(UUID sourceId) {
        return new MKEffectBuilder<>(this, sourceId, this::makeState);
    }

    @Override
    public MKEffectBuilder<State> builder(LivingEntity sourceEntity) {
        return new MKEffectBuilder<>(this, sourceEntity, this::makeState);
    }

    public static class State extends MKEffectState {
        public ResourceLocation animName;
        public Vec3 location;
        public boolean includeSelf;

        public void setup(ResourceLocation animName, boolean includeSelf, Vec3 location) {
            this.animName = animName;
            this.includeSelf = includeSelf;
            this.location = location;
        }

        @Override
        public boolean validateOnApply(IMKEntityData targetData, MKActiveEffect activeEffect) {
            // Don't apply if we're the caster and the caller didn't want us included
            return includeSelf || !isEffectSource(targetData.getEntity(), activeEffect);
        }

        @Override
        public boolean performEffect(IMKEntityData targetData, MKActiveEffect instance) {
            PacketHandler.sendToTrackingAndSelf(createPacket(targetData.getEntity()), targetData.getEntity());
            return true;
        }

        private MKParticleEffectSpawnPacket createPacket(Entity target) {
            return new MKParticleEffectSpawnPacket(location, animName, target.getId());
        }

        @Override
        public void serializeStorage(CompoundTag stateTag) {
            super.serializeStorage(stateTag);
            stateTag.putBoolean("includeSelf", includeSelf);
            MKNBTUtil.writeVector3d(stateTag, "location", location);
            MKNBTUtil.writeResourceLocation(stateTag, "animName", animName);
        }

        @Override
        public void deserializeStorage(CompoundTag tag) {
            super.deserializeStorage(tag);
            includeSelf = tag.getBoolean("includeSelf");
            location = MKNBTUtil.readVector3(tag, "location");
            animName = MKNBTUtil.readResourceLocation(tag, "animName");
        }
    }
}
