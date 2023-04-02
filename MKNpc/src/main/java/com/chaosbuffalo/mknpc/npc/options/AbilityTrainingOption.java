package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingEntry;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingRequirement;
import com.chaosbuffalo.mkcore.abilities.training.IAbilityTrainer;
import com.chaosbuffalo.mkcore.abilities.training.IAbilityTrainingEntity;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class AbilityTrainingOption extends SimpleOption<List<AbilityTrainingOption.AbilityTrainingOptionEntry>> {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "ability_trainings");

    public static class AbilityTrainingOptionEntry {
        private MKAbility ability;
        protected final List<AbilityTrainingRequirement> requirements = new ArrayList<>();

        public AbilityTrainingOptionEntry(MKAbility ability, List<AbilityTrainingRequirement> requirements) {
            this.ability = ability;
            this.requirements.addAll(requirements);
        }

        public <D> AbilityTrainingOptionEntry(Dynamic<D> dynamic) {
            deserialize(dynamic);
        }

        public <D> D serialize(DynamicOps<D> ops) {
            ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
            builder.put(ops.createString("ability"), ops.createString(ability.getAbilityId().toString()));
            builder.put(ops.createString("reqs"), ops.createList(requirements.stream().map(x -> x.serialize(ops))));
            return ops.createMap(builder.build());
        }

        public <D> void deserialize(Dynamic<D> dynamic) {
            ResourceLocation abilityId = dynamic.get("ability").asString()
                    .resultOrPartial(MKNpc.LOGGER::error)
                    .map(ResourceLocation::new)
                    .orElseThrow(() -> new IllegalArgumentException("Failed to parse field 'ability' from " + dynamic));

            ability = MKCoreRegistry.getAbility(abilityId);
            if (ability == null) {
                throw new NoSuchElementException(String.format("Ability '%s' does not exist", abilityId));
            }

            requirements.clear();
            requirements.addAll(dynamic.get("reqs").asList(x -> AbilityTrainingRequirement.fromDynamic(x)
                    .resultOrPartial(error -> {
                        throw new IllegalArgumentException(String.format("Failed to parse training requirement for " +
                                "ability '%s': %s", ability.getAbilityId(), error));
                    }).orElseThrow(IllegalStateException::new)));
        }
    }

    public AbilityTrainingOption() {
        super(NAME);
        setValue(new ArrayList<>());
    }

    public AbilityTrainingOption withTrainingOption(MKAbility ability, AbilityTrainingRequirement... reqs) {
        getValue().add(new AbilityTrainingOptionEntry(ability, Arrays.asList(reqs)));
        return this;
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        List<AbilityTrainingOptionEntry> entries = dynamic.get("value").asList(AbilityTrainingOptionEntry::new);
        setValue(entries);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("value"), ops.createList(getValue().stream().map(x -> x.serialize(ops))));
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, List<AbilityTrainingOptionEntry> value) {
        if (entity instanceof IAbilityTrainingEntity) {
            IAbilityTrainer trainer = ((IAbilityTrainingEntity) entity).getAbilityTrainer();
            for (AbilityTrainingOptionEntry entry : value) {
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
