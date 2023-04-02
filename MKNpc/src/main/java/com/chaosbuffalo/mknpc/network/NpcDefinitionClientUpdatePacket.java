package com.chaosbuffalo.mknpc.network;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionClient;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

public class NpcDefinitionClientUpdatePacket {

    private final ArrayList<NpcDefinitionClient> clientDefs;

    public NpcDefinitionClientUpdatePacket(Collection<NpcDefinitionClient> clientDefinitions){
        this.clientDefs = new ArrayList<>();
        this.clientDefs.addAll(clientDefinitions);
    }

    public NpcDefinitionClientUpdatePacket(FriendlyByteBuf buffer){
        clientDefs = new ArrayList<>();
        int count = buffer.readInt();
        for (int i=0; i < count; i++){
            NpcDefinitionClient def = NpcDefinitionClient.fromBuffer(buffer);
            clientDefs.add(def);
        }
    }

    public void toBytes(FriendlyByteBuf buffer){
        buffer.writeInt(clientDefs.size());
        for (NpcDefinitionClient def : clientDefs){
            def.toBuffer(buffer);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier){
        NetworkEvent.Context ctx = supplier.get();
        MKNpc.LOGGER.info("Handling client npc definition data sync");
        ctx.enqueueWork(() -> {
            NpcDefinitionManager.CLIENT_DEFINITIONS.clear();
            for (NpcDefinitionClient client : clientDefs){
                NpcDefinitionManager.CLIENT_DEFINITIONS.put(client.getDefinitionName(), client);
            }
        });
        ctx.setPacketHandled(true);
    }
}

