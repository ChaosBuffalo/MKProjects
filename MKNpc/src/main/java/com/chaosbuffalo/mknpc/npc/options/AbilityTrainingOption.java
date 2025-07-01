package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingRequirement;
import com.chaosbuffalo.mkcore.abilities.training.EntityAbilityTrainer;
import com.chaosbuffalo.mkcore.init.CoreAttachments;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcOptionTypes;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbilityTrainingOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = MKNpc.id("ability_trainings");
    public static final Codec<AbilityTrainingOption> CODEC = AbilityTrainingOptionEntry.CODEC.listOf()
            .xmap(AbilityTrainingOption::new, AbilityTrainingOption::getValue);
    public static final MapCodec<AbilityTrainingOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            AbilityTrainingOptionEntry.CODEC.listOf().fieldOf("abilities").forGetter(i -> i.options)
    ).apply(builder, AbilityTrainingOption::new));

    public record AbilityTrainingOptionEntry(Holder<MKAbility> ability, List<AbilityTrainingRequirement> requirements) {
        public static final Codec<AbilityTrainingOptionEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                MKCoreRegistry.ABILITIES.holderByNameCodec().fieldOf("ability").forGetter(AbilityTrainingOptionEntry::ability),
                AbilityTrainingRequirement.CODEC.listOf().fieldOf("requirements").forGetter(AbilityTrainingOptionEntry::requirements)
        ).apply(builder, AbilityTrainingOptionEntry::new));
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

    public AbilityTrainingOption withTrainingOption(Holder<MKAbility> ability, AbilityTrainingRequirement... reqs) {
        getValue().add(new AbilityTrainingOptionEntry(ability, Arrays.asList(reqs)));
        return this;
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        EntityAbilityTrainer trainer = entity.getData(CoreAttachments.ABILITY_TRAINER);
        for (AbilityTrainingOptionEntry entry : options) {
            if (entry.ability.isBound()) {
                trainer.addTrainedAbility(entry.ability.value(), entry.requirements);
            }
        }
    }

    @Override
    public NpcOptionType<? extends NpcDefinitionOption> getType() {
        return NpcOptionTypes.ABILITY_TRAINING.get();
    }
}
