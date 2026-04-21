package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

public class NpcAbilityEntry {
    public static final Codec<NpcAbilityEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.mapEither(
                    MKCoreRegistry.ABILITIES.byNameCodec().fieldOf("ability"),
                    ResourceLocation.CODEC.fieldOf("abilityId")
            ).forGetter(i -> i.getAbility() != null ? Either.left(i.getAbility()) : Either.right(i.getAbilityId())),
            Codec.STRING.optionalFieldOf("activationId").forGetter(i -> Optional.ofNullable(i.getActivationId())),
            Codec.INT.fieldOf("priority").forGetter(NpcAbilityEntry::getPriority),
            Codec.DOUBLE.fieldOf("chance").forGetter(NpcAbilityEntry::getChance)
    ).apply(builder, (e, activationId, p, c) ->
            new NpcAbilityEntry(e.map(MKAbility::getAbilityId, id -> id), activationId.orElse(null), p, c)));

    private final ResourceLocation abilityId;
    @Nullable
    private final MKAbility ability;
    @Nullable
    private final String activationId;
    private final int priority;
    private final double chance;

    public NpcAbilityEntry(MKAbility ability, int priority, double chance) {
        this(ability.getAbilityId(), null, priority, chance);
    }

    public NpcAbilityEntry(MKAbility ability, @Nullable String activationId, int priority, double chance) {
        this(ability.getAbilityId(), activationId, priority, chance);
    }

    public NpcAbilityEntry(ResourceLocation abilityId, @Nullable String activationId, int priority, double chance) {
        this.priority = priority;
        this.abilityId = Objects.requireNonNull(abilityId, "abilityId");
        this.ability = MKCoreRegistry.getAbility(abilityId);
        this.activationId = activationId;
        this.chance = chance;
    }

    public ResourceLocation getAbilityId() {
        return abilityId;
    }

    @Nullable
    public MKAbility getAbility() {
        return ability;
    }

    public @Nullable String getActivationId() {
        return activationId;
    }

    public int getPriority() {
        return priority;
    }

    public double getChance() {
        return chance;
    }
}
