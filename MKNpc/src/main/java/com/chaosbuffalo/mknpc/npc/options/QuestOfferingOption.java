package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import com.chaosbuffalo.mknpc.npc.option_entries.QuestOptionsEntry;
import com.chaosbuffalo.mknpc.quest.QuestDefinitionManager;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class QuestOfferingOption extends WorldPermanentOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "offer_quests");
    private final List<ResourceLocation> questOfferings;


    public QuestOfferingOption(ResourceLocation... quests) {
        this();
        questOfferings.addAll(Arrays.asList(quests));
    }

    public QuestOfferingOption() {
        super(NAME, ApplyOrder.LATE);
        questOfferings = new ArrayList<>();
    }

    @Override
    protected INpcOptionEntry makeOptionEntry(NpcDefinition definition, Random random) {
        return new QuestOptionsEntry(questOfferings);
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        List<ResourceLocation> offerings = dynamic.get("offerings").asList(x -> new ResourceLocation(x.asString(QuestDefinitionManager.INVALID_QUEST.toString())));
        questOfferings.clear();
        for (ResourceLocation offering : offerings) {
            if (!offering.equals(QuestDefinitionManager.INVALID_QUEST)) {
                questOfferings.add(offering);
            }
        }
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("offerings"),
                ops.createList(questOfferings.stream().map(x -> ops.createString(x.toString()))));
    }
}
