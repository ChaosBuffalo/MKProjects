package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.entries.LootOptionEntry;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExtraLootOption extends NpcDefinitionOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "extra_loot");
    public static final Codec<ExtraLootOption> CODEC = RecordCodecBuilder.<ExtraLootOption>mapCodec(builder -> {
        return builder.group(
                LootOptionEntry.CODEC.listOf().fieldOf("lootOptions").forGetter(i -> i.lootOptions),
                Codec.DOUBLE.optionalFieldOf("noLootChance", 0.0).forGetter(i -> i.noLootChance),
                Codec.INT.optionalFieldOf("dropChances", 1).forGetter(i -> i.dropChances),
                Codec.DOUBLE.optionalFieldOf("noLootIncrease", 0.0).forGetter(i -> i.noLootIncrease)
        ).apply(builder, ExtraLootOption::new);
    }).codec();

    private final List<LootOptionEntry> lootOptions;
    private double noLootChance;
    private int dropChances;
    private double noLootIncrease;

    private ExtraLootOption(List<LootOptionEntry> optionEntries, double noLootChance, int dropChances, double noLootIncrease) {
        super(NAME, ApplyOrder.MIDDLE);
        lootOptions = ImmutableList.copyOf(optionEntries);
        this.noLootChance = noLootChance;
        this.dropChances = dropChances;
        this.noLootIncrease = noLootIncrease;
    }

    public ExtraLootOption() {
        super(NAME, ApplyOrder.MIDDLE);
        lootOptions = new ArrayList<>();
        noLootChance = 0.0;
        dropChances = 1;
        noLootIncrease = 0.0;
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
