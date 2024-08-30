package com.chaosbuffalo.mknpc.capabilities;

import com.chaosbuffalo.mknpc.init.MKNpcAttachments;
import com.chaosbuffalo.mknpc.world.gen.IStructurePlaced;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public interface IChestNpcData extends INBTSerializable<CompoundTag>, IStructurePlaced, MenuProvider {

    SimpleContainer getQuestInventoryForPlayer(Player player);

    boolean hasQuestInventoryForPlayer(Player player);

    @Nullable
    UUID getChestId();

    @Nullable
    String getChestLabel();

    ChestBlockEntity getTileEntity();

    void generateChestId(String chestLabel);

    void onLoad();

    static Optional<IChestNpcData> get(BlockEntity blockEntity) {
        if (blockEntity instanceof ChestBlockEntity) {
            return Optional.of(blockEntity.getData(MKNpcAttachments.CHEST_NPC_DATA));
        }
        return Optional.empty();
    }

}
