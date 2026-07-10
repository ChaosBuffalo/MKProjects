package com.chaosbuffalo.mknpc.network;


import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.network.packets.FinalizeMKSpawnerPacket;
import com.chaosbuffalo.mknpc.network.packets.NpcDefinitionClientUpdatePacket;
import com.chaosbuffalo.mknpc.network.packets.OpenMKSpawnerPacket;
import com.chaosbuffalo.mknpc.network.packets.SetSpawnListPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MKNpc.MODID)
public class PacketHandler {

    private static final String VERSION = "1.0";


    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToServer(
                SetSpawnListPacket.TYPE,
                SetSpawnListPacket.STREAM_CODEC,
                SetSpawnListPacket::handle
        );
        registrar.playToClient(
                OpenMKSpawnerPacket.TYPE,
                OpenMKSpawnerPacket.STREAM_CODEC,
                OpenMKSpawnerPacket::handle
        );
        registrar.playToClient(
                NpcDefinitionClientUpdatePacket.TYPE,
                NpcDefinitionClientUpdatePacket.STREAM_CODEC,
                NpcDefinitionClientUpdatePacket::handle
        );
        registrar.playToServer(
                FinalizeMKSpawnerPacket.TYPE,
                FinalizeMKSpawnerPacket.STREAM_CODEC,
                FinalizeMKSpawnerPacket::handle
        );
    }
}
