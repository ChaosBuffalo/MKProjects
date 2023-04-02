package com.chaosbuffalo.mknpc.inventories;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

public class PsuedoChestContainer extends ChestMenu {
    private final Container psuedoChest;

    public static PsuedoChestContainer createGeneric9X3(int id, Inventory player, Container inventory, Container realblockEntity) {
        return new PsuedoChestContainer(MenuType.GENERIC_9x3, id, player, inventory, 3, realblockEntity);
    }

    public PsuedoChestContainer(MenuType<?> type, int id, Inventory playerInventoryIn,
                                Container p_i50092_4_, int rows, Container otherChest) {
        super(type, id, playerInventoryIn, p_i50092_4_, rows);
        psuedoChest = otherChest;
    }

    @Override
    public Container getContainer() {
        return psuedoChest;
    }
}
