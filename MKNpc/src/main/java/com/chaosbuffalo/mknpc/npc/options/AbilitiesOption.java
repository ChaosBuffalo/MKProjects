package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcAbilityEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcOptionTypes;
import com.chaosbuffalo.mknpc.npc.option_entries.AbilitiesOptionEntry;
import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class AbilitiesOption extends WorldPermanentOption {
    public static final ResourceLocation NAME = MKNpc.id("abilities");
    public static final Codec<AbilitiesOption> CODEC = Codec.list(NpcAbilityEntry.CODEC).xmap(AbilitiesOption::new, i -> i.abilities);
    public static final MapCodec<AbilitiesOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            NpcAbilityEntry.CODEC.listOf().fieldOf("abilities").forGetter(i -> i.abilities)
    ).apply(builder, AbilitiesOption::new));

    private final List<NpcAbilityEntry> abilities;

    private AbilitiesOption(List<NpcAbilityEntry> abilities) {
        super(NAME);
        this.abilities = ImmutableList.copyOf(abilities);
    }

    public AbilitiesOption() {
        super(NAME);
        abilities = new ArrayList<>();
    }

    public AbilitiesOption withAbilityOption(MKAbility ability, int priority, double chance) {
        abilities.add(new NpcAbilityEntry(ability, priority, chance));
        return this;
    }

    public AbilitiesOption withAbilityDefinitionOption(ResourceLocation abilityId,
                                                       @org.jetbrains.annotations.Nullable String activationId,
                                                       int priority,
                                                       double chance) {
        abilities.add(new NpcAbilityEntry(abilityId, activationId, priority, chance));
        return this;
    }

    @Override
    protected INpcOptionEntry makeOptionEntry(NpcDefinition definition, Level level, RandomSource random) {
        List<NpcAbilityEntry> finalChoices = new ArrayList<>();
        for (NpcAbilityEntry entry : abilities) {
            if (random.nextDouble() <= entry.getChance()) {
                finalChoices.add(entry);
            }
        }
        return new AbilitiesOptionEntry(finalChoices);
    }

    @Override
    public NpcOptionType<? extends NpcDefinitionOption> getType() {
        return NpcOptionTypes.ABILITIES.get();
    }
}
