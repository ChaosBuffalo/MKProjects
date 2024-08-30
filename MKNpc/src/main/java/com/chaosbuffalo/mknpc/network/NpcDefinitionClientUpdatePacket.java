package com.chaosbuffalo.mknpc.network;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionClient;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.Collection;

public class NpcDefinitionClientUpdatePacket implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<NpcDefinitionClientUpdatePacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MKNpc.MODID, "npc_definition_client_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, NpcDefinitionClientUpdatePacket> STREAM_CODEC = StreamCodec.ofMember(
            NpcDefinitionClientUpdatePacket::toBytes, NpcDefinitionClientUpdatePacket::new
    );

    private final ArrayList<NpcDefinitionClient> clientDefs;

    public NpcDefinitionClientUpdatePacket(Collection<NpcDefinitionClient> clientDefinitions) {
        this.clientDefs = new ArrayList<>();
        this.clientDefs.addAll(clientDefinitions);
    }

    public NpcDefinitionClientUpdatePacket(FriendlyByteBuf buffer) {
        clientDefs = new ArrayList<>();
        int count = buffer.readInt();
        for (int i = 0; i < count; i++) {
            NpcDefinitionClient def = NpcDefinitionClient.fromBuffer(buffer);
            clientDefs.add(def);
        }
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(clientDefs.size());
        for (NpcDefinitionClient def : clientDefs) {
            def.toBuffer(buffer);
        }
    }

    public static void handle(final NpcDefinitionClientUpdatePacket packet, IPayloadContext context) {
        MKNpc.LOGGER.info("Handling client npc definition data sync");
        NpcDefinitionManager.CLIENT_DEFINITIONS.clear();
        for (NpcDefinitionClient client : packet.clientDefs) {
            NpcDefinitionManager.CLIENT_DEFINITIONS.put(client.getDefinitionName(), client);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

