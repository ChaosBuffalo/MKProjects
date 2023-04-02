package com.chaosbuffalo.mknpc.inventories;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

public class QuestChestInventory extends SimpleContainer {
    private final ChestBlockEntity replacementChest;

    public QuestChestInventory(ChestBlockEntity replacementChest){
        super(replacementChest.getContainerSize());
        this.replacementChest = replacementChest;
    }

    @Override
    public void startOpen(Player player) {
        replacementChest.startOpen(player);
    }

    @Override
    public void stopOpen(Player player) {
        replacementChest.stopOpen(player);
    }
}
