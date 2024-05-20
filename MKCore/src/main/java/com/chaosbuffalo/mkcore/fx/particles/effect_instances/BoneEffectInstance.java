package com.chaosbuffalo.mkcore.fx.particles.effect_instances;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCBone;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCSkeleton;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class BoneEffectInstance extends ParticleEffectInstance {
    public static final ResourceLocation TYPE = MKCore.makeRL("effect_instance.bone");
    public static final Codec<BoneEffectInstance> CODEC = RecordCodecBuilder.<BoneEffectInstance>mapCodec(builder -> {
        return builder.group(
                UUIDUtil.STRING_CODEC.fieldOf("instanceUUID").forGetter(ParticleEffectInstance::getInstanceUUID),
                ResourceLocation.CODEC.fieldOf("particleAnimName").forGetter(ParticleEffectInstance::getParticleAnimName),
                Codec.STRING.optionalFieldOf("boneName", BipedSkeleton.ROOT_BONE_NAME).forGetter(i -> i.boneName)
        ).apply(builder, BoneEffectInstance::new);
    }).codec();

    private final String boneName;

    public BoneEffectInstance(UUID instanceUUID, ResourceLocation particleName, String boneName) {
        super(TYPE, instanceUUID);
        this.particleAnimName = particleName;
        this.boneName = boneName;
    }

    @Override
    public void update(Entity entity, MCSkeleton skeleton, float partialTicks, Vec3 offset) {
        if (entity instanceof LivingEntity living) {
            MCBone.getPositionOfBoneInWorld(living, skeleton, partialTicks, offset, boneName).ifPresent(x ->
                    getAnimation().ifPresent(anim -> anim.spawn(entity.getLevel(), x, new Vec3(1., 1., 1.), null)));
        }
    }
}
