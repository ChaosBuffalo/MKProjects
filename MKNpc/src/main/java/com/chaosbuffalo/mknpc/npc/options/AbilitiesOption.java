package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcAbilityEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.option_entries.AbilitiesOptionEntry;
import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public class AbilitiesOption extends WorldPermanentOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "abilities");
    private final List<NpcAbilityEntry> abilities;

    public AbilitiesOption() {
        super(NAME);
        abilities = new ArrayList<>();
    }

    protected void addAbilityEntry(NpcAbilityEntry entry) {
        abilities.add(entry);
    }

    public AbilitiesOption withAbilityOption(MKAbility ability, int priority, double chance) {
        return withAbilityOption(ability.getDefaultInstance(), priority, chance);
    }

    public AbilitiesOption withAbilityOption(MKAbilityInfo abilityInfo, int priority, double chance) {
        addAbilityEntry(new NpcAbilityEntry(abilityInfo, priority, chance));
        return this;
    }

    @Override
    protected INpcOptionEntry makeOptionEntry(NpcDefinition definition, RandomSource random) {
        List<NpcAbilityEntry> finalChoices = new ArrayList<>();
        for (NpcAbilityEntry entry : abilities) {
            if (random.nextDouble() <= entry.getChance()) {
                finalChoices.add(entry);
            }
        }
        return new AbilitiesOptionEntry(finalChoices);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("options"), ops.createList(abilities.stream().map(x -> x.serialize(ops))));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        List<NpcAbilityEntry> entries = dynamic.get("options").asList(d -> {
            NpcAbilityEntry entry = new NpcAbilityEntry();
            entry.deserialize(d);
            return entry;
        });
        abilities.clear();
        abilities.addAll(entries);
    }
}
