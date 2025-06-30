package com.chaosbuffalo.mknpc.components;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.spawn.SpawnList;
import com.chaosbuffalo.mknpc.spawn.SpawnOption;
import com.chaosbuffalo.mkweapons.components.WeaponsComponents;
import com.chaosbuffalo.mkweapons.items.effects.ranged.IRangedWeaponEffect;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;



public record SpawnerDataComponent(SpawnList spawns, int spawnTime, MKEntity.NonCombatMoveType moveType) {
    public static final SpawnerDataComponent EMPTY = new SpawnerDataComponent(new SpawnList(), GameConstants.TICKS_PER_SECOND * 300, MKEntity.NonCombatMoveType.STATIONARY);

    public static final Codec<SpawnerDataComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            SpawnList.CODEC.fieldOf("spawns").forGetter(i -> i.spawns),
            Codec.INT.fieldOf("spawnTime").forGetter(i -> i.spawnTime),
            MKEntity.NonCombatMoveType.CODEC.fieldOf("moveType").forGetter(i -> i.moveType)
    ).apply(builder, SpawnerDataComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpawnerDataComponent> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);


    public SpawnerDataComponent withAddedOption(SpawnOption newOption) {
        ImmutableList.Builder<SpawnOption> builder = ImmutableList.builderWithExpectedSize(this.spawns.getOptions().size() + 1);

        for (SpawnOption old : this.spawns().getOptions()) {
            builder.add(old);
        }
        builder.add(newOption);
        return new SpawnerDataComponent(new SpawnList(builder.build()), spawnTime, moveType);
    }
//
    public static void addOption(ItemStack itemStack, SpawnOption newOption) {
        itemStack.update(NpcComponents.SPAWNER_DATA, EMPTY, existing -> existing.withAddedOption(newOption));
    }
}