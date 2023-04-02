package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.entries.LootOptionEntry;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ExtraLootOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "extra_loot");
    private final List<LootOptionEntry> lootOptions;
    private double noLootChance;
    private int dropChances;
    private double noLootIncrease;


    public ExtraLootOption() {
        super(NAME, ApplyOrder.MIDDLE);
        lootOptions = new ArrayList<>();
        noLootChance = 0.0;
        dropChances = 1;
        noLootIncrease = 0.0;
    }

    public ExtraLootOption(double noLootChance, int dropChances, double noLootIncrease) {
        this();
        this.noLootChance = noLootChance;
        this.dropChances = dropChances;
        this.noLootIncrease = noLootIncrease;
    }

    public ExtraLootOption withLootOptions(LootOptionEntry... entries) {
        lootOptions.addAll(Arrays.asList(entries));
        return this;
    }

    public ExtraLootOption withDropChances(int chances) {
        dropChances = chances;
        return this;
    }

    public ExtraLootOption withNoLootIncrease(double chanceIncrease) {
        noLootIncrease = chanceIncrease;
        return this;
    }

    public ExtraLootOption withNoLootChance(double chance) {
        noLootChance = chance;
        return this;
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("noLootChance"), ops.createDouble(noLootChance));
        builder.put(ops.createString("dropChances"), ops.createInt(dropChances));
        builder.put(ops.createString("noLootIncrease"), ops.createDouble(noLootIncrease));
        builder.put(ops.createString("lootOptions"), ops.createList(lootOptions.stream().map(x -> x.serialize(ops))));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        noLootChance = dynamic.get("noLootChance").asDouble(0);
        dropChances = dynamic.get("dropChances").asInt(1);
        noLootIncrease = dynamic.get("noLootIncrease").asDouble(0.0);
        List<Optional<LootOptionEntry>> lootOpts = dynamic.get("lootOptions").asList(x -> {
            LootOptionEntry newEntry = new LootOptionEntry();
            newEntry.deserialize(x);
            if (newEntry.isValidConfiguration()) {
                return Optional.of(newEntry);
            } else {
                return Optional.empty();
            }
        });
        lootOptions.clear();
        for (Optional<LootOptionEntry> entry : lootOpts) {
            entry.ifPresent(lootOptions::add);
        }
    }

    @Override
    public void applyToEntity(NpcDefinition definition, Entity entity, double difficultyLevel) {
        MKNpc.getNpcData(entity).ifPresent(x -> {
            x.setChanceNoLoot(noLootChance);
            x.setDropChances(dropChances);
            for (LootOptionEntry entry : lootOptions) {
                x.addLootOption(entry);
                x.setNoLootChanceIncrease(noLootChance);
            }
        });
    }
}
