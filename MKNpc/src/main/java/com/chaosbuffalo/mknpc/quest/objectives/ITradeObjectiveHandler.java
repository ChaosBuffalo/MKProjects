package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestChainInstance;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public interface ITradeObjectiveHandler {

    void onPlayerTradeSuccess(Player player, PlayerQuestObjectiveData objectiveData,
                              QuestData questData, PlayerQuestChainInstance playerChain, LivingEntity trader);

    boolean canTradeWith(LivingEntity trader, Player player, PlayerQuestObjectiveData objectiveData,
                         QuestData questData, PlayerQuestChainInstance chainInstance);

    @Nullable
    int[] findMatches(List<ItemStack> nonEmptyInventoryContents);
}
