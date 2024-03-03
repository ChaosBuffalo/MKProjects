package com.chaosbuffalo.mknpc.inventories;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

public class PseudoChestContainer extends ChestMenu {
    private final Container pseudoChest;

    public static PseudoChestContainer createGeneric9X3(int id, Inventory player, Container inventory, Container realblockEntity) {
        return new PseudoChestContainer(MenuType.GENERIC_9x3, id, player, inventory, 3, realblockEntity);
    }

    public PseudoChestContainer(MenuType<?> type, int id, Inventory playerInventoryIn,
                                Container p_i50092_4_, int rows, Container otherChest) {
        super(type, id, playerInventoryIn, p_i50092_4_, rows);
        pseudoChest = otherChest;
    }

    @Override
    public Container getContainer() {
        return pseudoChest;
    }
}
