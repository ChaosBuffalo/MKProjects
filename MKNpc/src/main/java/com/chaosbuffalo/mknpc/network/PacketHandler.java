package com.chaosbuffalo.mknpc.network;

import com.chaosbuffalo.mknpc.MKNpc;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {

    private static SimpleChannel networkChannel;
    private static final String VERSION = "1.0";

    public static void setupHandler() {
        networkChannel = NetworkRegistry.newSimpleChannel(new ResourceLocation(MKNpc.MODID, "packet_handler"),
                () -> VERSION,
                s -> s.equals(VERSION),
                s -> s.equals(VERSION));
        registerMessages();
    }

    public static SimpleChannel getNetworkChannel() {
        return networkChannel;
    }

    public static void registerMessages() {
        int id = 1;
        networkChannel.registerMessage(id++, NpcDefinitionClientUpdatePacket.class,
                NpcDefinitionClientUpdatePacket::toBytes,
                NpcDefinitionClientUpdatePacket::new, NpcDefinitionClientUpdatePacket::handle);
        networkChannel.registerMessage(id++, SetSpawnListPacket.class,
                SetSpawnListPacket::toBytes,
                SetSpawnListPacket::new, SetSpawnListPacket::handle);
        networkChannel.registerMessage(id++, OpenMKSpawnerPacket.class,
                OpenMKSpawnerPacket::toBytes,
                OpenMKSpawnerPacket::new, OpenMKSpawnerPacket::handle);
        networkChannel.registerMessage(id++, FinalizeMKSpawnerPacket.class,
                FinalizeMKSpawnerPacket::toBytes,
                FinalizeMKSpawnerPacket::new, FinalizeMKSpawnerPacket::handle);
    }
}
