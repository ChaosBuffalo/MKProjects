package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingEntry;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingRequirement;
import com.chaosbuffalo.mkcore.abilities.training.IAbilityTrainer;
import com.chaosbuffalo.mkcore.abilities.training.IAbilityTrainingEntity;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbilityTrainingOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "ability_trainings");
    public static final Codec<AbilityTrainingOption> CODEC = AbilityTrainingOptionEntry.CODEC.listOf().xmap(AbilityTrainingOption::new, AbilityTrainingOption::getValue);

    public static class AbilityTrainingOptionEntry {
        public static final Codec<AbilityTrainingOptionEntry> CODEC = ExtraCodecs.lazyInitializedCodec(() ->
                RecordCodecBuilder.<AbilityTrainingOptionEntry>mapCodec(builder -> {
                    return builder.group(
                            MKCoreRegistry.ABILITIES.getCodec().fieldOf("ability").forGetter(i -> i.ability),
                            AbilityTrainingRequirement.CODEC.listOf().fieldOf("requirements").forGetter(i -> i.requirements)
                    ).apply(builder, AbilityTrainingOptionEntry::new);
                }).codec());

        private final MKAbility ability;
        private final List<AbilityTrainingRequirement> requirements = new ArrayList<>();

        public AbilityTrainingOptionEntry(MKAbility ability, List<AbilityTrainingRequirement> requirements) {
            this.ability = ability;
            this.requirements.addAll(requirements);
        }
    }

    private final List<AbilityTrainingOptionEntry> options;

    public AbilityTrainingOption(List<AbilityTrainingOptionEntry> options) {
        super(NAME, ApplyOrder.MIDDLE);
        this.options = ImmutableList.copyOf(options);
    }

    public AbilityTrainingOption() {
        super(NAME, ApplyOrder.MIDDLE);
        this.options = new ArrayList<>();
    }

    public List<AbilityTrainingOptionEntry> getValue() {
        return options;
    }

    public AbilityTrainingOption withTrainingOption(MKAbility ability, AbilityTrainingRequirement... reqs) {
        getValue().add(new AbilityTrainingOptionEntry(ability, Arrays.asList(reqs)));
        return this;
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        if (entity instanceof IAbilityTrainingEntity trainingEntity) {
            IAbilityTrainer trainer = trainingEntity.getAbilityTrainer();
            for (AbilityTrainingOptionEntry entry : options) {
                if (entry.ability != null) {
                    AbilityTrainingEntry trainingEntry = trainer.addTrainedAbility(entry.ability);
                    for (AbilityTrainingRequirement req : entry.requirements) {
                        trainingEntry.addRequirement(req);
                    }
                }
            }
        }
    }
}
