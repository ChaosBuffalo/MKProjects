package com.chaosbuffalo.mkcore.fx.particles.effect_instances;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCSkeleton;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.serialization.IDynamicMapTypedSerializer;
import com.chaosbuffalo.mkcore.serialization.ISerializableAttributeContainer;
import com.chaosbuffalo.mkcore.serialization.attributes.ISerializableAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public abstract class ParticleEffectInstance implements ISerializableAttributeContainer, IDynamicMapTypedSerializer {
    private static final String TYPE_NAME_FIELD = "type";
    public final static ResourceLocation INVALID_OPTION = new ResourceLocation(MKCore.MOD_ID,
            "particle_effect_instance.invalid");

    protected final List<ISerializableAttribute<?>> attributes;
    protected final ResourceLocationAttribute particleAnimName = new ResourceLocationAttribute("particleAnimName",
            INVALID_OPTION);
    private UUID instanceUUID;
    private ParticleAnimation animation;
    private final ResourceLocation instanceType;

    public ParticleEffectInstance(ResourceLocation instanceType) {
        this(instanceType, UUID.randomUUID());
    }

    public ParticleEffectInstance(ResourceLocation instanceType, UUID instanceUUID) {
        this.attributes = new ArrayList<>();
        addAttribute(particleAnimName);
        this.instanceType = instanceType;
        this.instanceUUID = instanceUUID;
    }


    public void setInstanceUUID(UUID instanceUUID) {
        this.instanceUUID = instanceUUID;
    }

    public abstract void update(Entity entity, MCSkeleton skeleton, float partialTicks, Vec3 offset);

    public ResourceLocation getParticleAnimName() {
        return particleAnimName.getValue();
    }

    public UUID getInstanceUUID() {
        return instanceUUID;
    }

    public Optional<ParticleAnimation> getAnimation() {
        if (animation == null) {
            this.animation = ParticleAnimationManager.getAnimation(getParticleAnimName());
        }
        if (animation != null) {
            return Optional.of(animation);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ISerializableAttribute<?>> getAttributes() {
        return attributes;
    }

    @Override
    public void addAttribute(ISerializableAttribute<?> attribute) {
        attributes.add(attribute);
    }

    @Override
    public void addAttributes(ISerializableAttribute<?>... attributes) {
        this.attributes.addAll(Arrays.asList(attributes));
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        builder.put(ops.createString("instanceUUID"), ops.createString(instanceUUID.toString()));
        builder.put(ops.createString("attributes"), serializeAttributeMap(ops));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        this.instanceUUID = UUID.fromString(dynamic.get("instanceUUID").asString(UUID.randomUUID().toString()));
        deserializeAttributeMap(dynamic, "attributes");
    }

    @Override
    public ResourceLocation getTypeName() {
        return instanceType;
    }

    @Override
    public String getTypeEntryName() {
        return TYPE_NAME_FIELD;
    }

    public static <D> ResourceLocation getType(Dynamic<D> dynamic) {
        return IDynamicMapTypedSerializer.getType(dynamic, TYPE_NAME_FIELD).orElse(INVALID_OPTION);
    }
}
