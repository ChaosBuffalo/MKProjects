package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcAbilityEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.options.binding.AbilitiesOptionBoundValue;
import com.chaosbuffalo.mknpc.npc.options.binding.IBoundNpcOptionValue;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public class AbilitiesOption extends BindingNpcOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "abilities");
    public static final Codec<AbilitiesOption> CODEC = Codec.list(NpcAbilityEntry.CODEC).xmap(AbilitiesOption::new, i -> i.abilities);

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
        abilities.add(new NpcAbilityEntry(ability.getAbilityId(), priority, chance));
        return this;
    }

    @Override
    protected IBoundNpcOptionValue generateBoundValue(NpcDefinition definition, RandomSource random) {
        List<NpcAbilityEntry> finalChoices = new ArrayList<>();
        for (NpcAbilityEntry entry : abilities) {
            if (random.nextDouble() <= entry.getChance()) {
                finalChoices.add(entry);
            }
        }
        return new AbilitiesOptionBoundValue(finalChoices);
    }
}
