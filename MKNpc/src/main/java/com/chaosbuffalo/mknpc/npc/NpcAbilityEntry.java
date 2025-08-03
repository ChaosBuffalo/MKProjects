package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class NpcAbilityEntry {
    public static final Codec<NpcAbilityEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.mapEither(
                    MKCoreRegistry.ABILITIES.byNameCodec().fieldOf("ability"),
                    MKCoreRegistry.ABILITIES.byNameCodec().fieldOf("abilityId")
            ).forGetter(i -> Either.left(i.getAbility())),
            Codec.INT.fieldOf("priority").forGetter(NpcAbilityEntry::getPriority),
            Codec.DOUBLE.fieldOf("chance").forGetter(NpcAbilityEntry::getChance)
    ).apply(builder, (e, p, c) -> {
        return new NpcAbilityEntry(e.left().or(e::right).orElseThrow(), p, c);
    }));

    private final MKAbility ability;
    private final int priority;
    private final double chance;

    public NpcAbilityEntry(MKAbility ability, int priority, double chance) {
        this.priority = priority;
        this.ability = ability;
        this.chance = chance;
    }

    public MKAbility getAbility() {
        return ability;
    }

    public int getPriority() {
        return priority;
    }

    public double getChance() {
        return chance;
    }
}
