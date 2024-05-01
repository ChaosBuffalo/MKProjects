package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import com.chaosbuffalo.mknpc.npc.option_entries.QuestOptionsEntry;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.List;


public class QuestOfferingOption extends WorldPermanentOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "offer_quests");
    public static final Codec<QuestOfferingOption> CODEC = ResourceLocation.CODEC.listOf().xmap(QuestOfferingOption::new, i -> i.questOfferings);

    private final List<ResourceLocation> questOfferings;

    public QuestOfferingOption(ResourceLocation quest) {
        this(List.of(quest));
    }

    public QuestOfferingOption(List<ResourceLocation> quests) {
        super(NAME, ApplyOrder.LATE);
        questOfferings = ImmutableList.copyOf(quests);
    }

    @Override
    protected INpcOptionEntry makeOptionEntry(NpcDefinition definition, RandomSource random) {
        return new QuestOptionsEntry(questOfferings);
    }
}
