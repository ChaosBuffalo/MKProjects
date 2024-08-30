package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkcore.fx.particles.effect_instances.ParticleEffectInstance;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcOptionTypes;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class ParticleEffectsOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = MKNpc.id("particle_effects");
    public static final Codec<ParticleEffectsOption> CODEC = ParticleEffectInstance.CODEC.listOf().xmap(ParticleEffectsOption::new, ParticleEffectsOption::getValue);
    public static final MapCodec<ParticleEffectsOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ParticleEffectInstance.CODEC.listOf().fieldOf("instances").forGetter(i -> i.instances)
    ).apply(builder, ParticleEffectsOption::new));

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

    @Override
    public NpcOptionType<? extends NpcDefinitionOption> getType() {
        return NpcOptionTypes.PARTICLE_EFFECTS.get();
    }
}
