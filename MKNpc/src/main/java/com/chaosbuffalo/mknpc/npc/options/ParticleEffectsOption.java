package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.ParticleEffectInstance;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ParticleEffectsOption extends SimpleOption<List<ParticleEffectInstance>> {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "particle_effects");

    public ParticleEffectsOption() {
        super(NAME);
    }

    public ParticleEffectsOption withEffects(List<ParticleEffectInstance> effectInstances) {
        setValue(effectInstances);
        return this;
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, List<ParticleEffectInstance> value) {
        if (entity instanceof MKEntity) {
            for (ParticleEffectInstance inst : value) {
                ((MKEntity) entity).getParticleEffectTracker().addParticleInstance(inst);
            }
        }
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("value"), ops.createList(getValue().stream().map(
                x -> x.serialize(ops))));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        List<ParticleEffectInstance> val = new ArrayList<>();
        List<DataResult<ParticleEffectInstance>> decoded = dynamic.get("value").asList(x -> {
            ResourceLocation type = ParticleEffectInstance.getType(x);
            ParticleEffectInstance inst = ParticleAnimationManager.getEffectInstance(type);
            if (inst != null) {
                inst.deserialize(x);
                return DataResult.success(inst);
            }
            return DataResult.error(String.format("Failed to decode effect type %s", type.toString()));
        });
        for (DataResult<ParticleEffectInstance> data : decoded) {
            data.result().ifPresent(val::add);
        }
        setValue(val);
    }

    @Override
    public boolean canBeBossStage() {
        return true;
    }
}
