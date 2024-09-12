package com.chaosbuffalo.mkfaction.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketHandler {

    private static final String VERSION = "1.0";

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(VERSION);

        registrar.playToClient(
                MobFactionAssignmentPacket.TYPE,
                MobFactionAssignmentPacket.STREAM_CODEC,
                MobFactionAssignmentPacket::handle
        );
    }
}
