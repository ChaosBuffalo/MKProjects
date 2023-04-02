package com.chaosbuffalo.mknpc.entity;

import com.chaosbuffalo.mknpc.inventories.QuestGiverInventoryContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

public class EntityTradeContainer implements MenuProvider {
    private final MKEntity entity;

    public EntityTradeContainer(MKEntity entity){
        this.entity = entity;
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("mknpc.quest.trade_container", entity.getName());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player player) {
        return QuestGiverInventoryContainer.createGeneric9X1(menuId, playerInventory, entity);
    }
}
