package com.chaosbuffalo.mknpc.content.databases;

import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import com.chaosbuffalo.mknpc.quest.generation.QuestChainBuildResult;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public interface IQuestDatabase {
    @Nullable
    QuestChainInstance getQuest(UUID questId);

    Optional<QuestChainBuildResult> buildQuest(QuestDefinition definition, BlockPos pos);
}
