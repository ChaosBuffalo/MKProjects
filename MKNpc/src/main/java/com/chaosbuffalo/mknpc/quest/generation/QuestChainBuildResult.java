package com.chaosbuffalo.mknpc.quest.generation;

import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.chaosbuffalo.mknpc.quest.QuestStructureLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public class QuestChainBuildResult {
    public QuestChainInstance instance;
    public Map<QuestStructureLocation, MKStructureEntry> questStructures;

    public QuestChainBuildResult(QuestChainInstance instance,
                                 Map<QuestStructureLocation, MKStructureEntry> structuresIn) {
        this.instance = instance;
        questStructures = structuresIn;
    }

}
