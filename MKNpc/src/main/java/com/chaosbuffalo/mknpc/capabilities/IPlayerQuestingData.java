package com.chaosbuffalo.mknpc.capabilities;

import com.chaosbuffalo.mknpc.quest.Quest;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestChainInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPlayerQuestingData extends INBTSerializable<CompoundTag> {

    Player getPlayer();

    Collection<PlayerQuestChainInstance> getQuestChains();

    Optional<PlayerQuestChainInstance> getQuestChain(UUID questId);

    void advanceQuestChain(IWorldNpcData worldHandler, QuestChainInstance questChainInstance, Quest currentQuest);

    void questProgression(IWorldNpcData worldHandler, QuestChainInstance questChainInstance);

    void startQuest(IWorldNpcData worldHandler, UUID questId);

    PlayerQuestingDataHandler.QuestStatus getQuestStatus(UUID questId);

    Optional<List<String>> getCurrentQuestSteps(UUID questId);
}
