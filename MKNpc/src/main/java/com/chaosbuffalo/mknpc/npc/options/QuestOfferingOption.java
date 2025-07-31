package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcOptionTypes;
import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import com.chaosbuffalo.mknpc.npc.option_entries.QuestOptionEntry;
import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.List;


public class QuestOfferingOption extends WorldPermanentOption {
    public static final ResourceLocation NAME = MKNpc.id("offer_quests");
    public static final MapCodec<QuestOfferingOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            QuestDefinition.KEY_CODEC.listOf().fieldOf("questIds").forGetter(i -> i.questOfferings)
    ).apply(builder, QuestOfferingOption::new));

    private final List<ResourceKey<QuestDefinition>> questOfferings;

    public QuestOfferingOption(List<ResourceKey<QuestDefinition>> quests) {
        super(NAME, ApplyOrder.LATE);
        questOfferings = ImmutableList.copyOf(quests);
    }

    @Override
    protected INpcOptionEntry makeOptionEntry(NpcDefinition definition, Level level, RandomSource random) {
        return new QuestOptionEntry(questOfferings);
    }

    @Override
    public NpcOptionType<? extends NpcDefinitionOption> getType() {
        return NpcOptionTypes.QUEST_OFFERING.get();
    }
}
