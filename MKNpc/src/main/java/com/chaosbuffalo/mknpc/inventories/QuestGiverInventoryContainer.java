package com.chaosbuffalo.mknpc.inventories;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IPlayerQuestingData;
import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.quest.Quest;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestChainInstance;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import com.chaosbuffalo.mknpc.quest.objectives.ITradeObjectiveHandler;
import com.chaosbuffalo.mknpc.quest.objectives.QuestObjective;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class QuestGiverInventoryContainer extends ChestMenu {
    private final MKEntity entity;

    public QuestGiverInventoryContainer(MenuType<?> type, int id, Inventory playerInventoryIn,
                                        Container p_i50092_4_, int rows, MKEntity entity) {
        super(type, id, playerInventoryIn, p_i50092_4_, rows);
        this.entity = entity;
    }

    public static QuestGiverInventoryContainer createGeneric9X1(int id, Inventory player, MKEntity entity) {
        return new QuestGiverInventoryContainer(MenuType.GENERIC_9x1, id, player, new SimpleContainer(9), 1, entity);
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        Container inventory = getContainer();
        List<ItemStack> nonEmpty = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                nonEmpty.add(stack);
            }
            inventory.setItem(i, ItemStack.EMPTY);
        }
        if (nonEmpty.isEmpty()) {
            return;
        }
        Optional<? extends IPlayerQuestingData> playerQuestOpt = MKNpc.getPlayerQuestData(playerIn).resolve();
        if (!playerIn.getLevel().isClientSide() && playerQuestOpt.isPresent()) {
            IPlayerQuestingData playerQuest = playerQuestOpt.get();
            Collection<PlayerQuestChainInstance> chains = playerQuest.getQuestChains();
            for (PlayerQuestChainInstance chain : chains) {
                QuestChainInstance questChain = ContentDB.getQuestInstance(chain.getQuestId());
                if (questChain == null) {
                    continue;
                }
                for (String questName : chain.getCurrentQuests()) {
                    Quest currentQuest = questChain.getDefinition().getQuest(questName);
                    if (currentQuest != null) {
                        for (QuestObjective<?> obj : currentQuest.getObjectives()) {
                            PlayerQuestData playerData = chain.getQuestData(currentQuest.getQuestName());
                            PlayerQuestObjectiveData playerObj = playerData.getObjective(obj.getObjectiveName());
                            QuestData questData = questChain.getQuestData(currentQuest);
                            if (obj instanceof ITradeObjectiveHandler tradeObj) {
                                if (tradeObj.canTradeWith(entity, playerIn, playerObj,
                                        questData, chain)) {
                                    int[] matches = tradeObj.findMatches(nonEmpty);
                                    if (matches == null) {
                                        continue;
                                    } else {
                                        tradeObj.onPlayerTradeSuccess(playerIn,
                                                playerObj, questData, chain, entity);
                                        questChain.signalQuestProgress(ContentDB.getQuestDB(), playerQuest, currentQuest, chain, false);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for (ItemStack is : nonEmpty) {
            Component name = Component.literal(String.format("<%s>", entity.getDisplayName().getString()));
            playerIn.sendSystemMessage(Component.translatable("mknpc.quest.trade.dont_need", name,
                    playerIn.getName(), is.getCount(), is.getHoverName()));
            playerIn.getInventory().placeItemBackInInventory(is, true);
        }
    }

}
