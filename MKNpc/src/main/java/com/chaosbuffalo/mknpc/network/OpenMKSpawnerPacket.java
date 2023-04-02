package com.chaosbuffalo.mknpc.network;

import com.chaosbuffalo.mknpc.client.gui.screens.MKSpawnerScreen;
import com.chaosbuffalo.mknpc.tile_entities.MKSpawnerTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenMKSpawnerPacket extends SetSpawnListPacket {

    public OpenMKSpawnerPacket(MKSpawnerTileEntity entity) {
        super(entity);
    }

    public OpenMKSpawnerPacket(FriendlyByteBuf buffer){
        super(buffer);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleInternal(){
        if (Minecraft.getInstance().player != null) {
            Level world = Minecraft.getInstance().player.getCommandSenderWorld();
            BlockEntity tileEntity = world.getBlockEntity(tileEntityLoc);
            if (tileEntity instanceof MKSpawnerTileEntity){
                MKSpawnerTileEntity spawner = (MKSpawnerTileEntity) tileEntity;
                setSpawnerFromPacket(spawner);
                Minecraft.getInstance().setScreen(new MKSpawnerScreen(spawner));
            }
        }
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            handleInternal();
        });
        ctx.setPacketHandled(true);
    }
}
