package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkcore.fx.particles.effect_instances.ParticleEffectInstance;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class ParticleEffectsOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "particle_effects");
    public static final Codec<ParticleEffectsOption> CODEC = ParticleEffectInstance.CODEC.listOf().xmap(ParticleEffectsOption::new, ParticleEffectsOption::getValue);

    private final List<ParticleEffectInstance> instances;

    public ParticleEffectsOption(List<ParticleEffectInstance> effects) {
        super(NAME, ApplyOrder.MIDDLE);
        instances = ImmutableList.copyOf(effects);
    }

    public List<ParticleEffectInstance> getValue() {
        return instances;
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        if (entity instanceof MKEntity mkEntity) {
            for (ParticleEffectInstance inst : instances) {
                mkEntity.getParticleEffectTracker().addParticleInstance(inst);
            }
        }
    }

    @Override
    public boolean canBeBossStage() {
        return true;
    }
}
