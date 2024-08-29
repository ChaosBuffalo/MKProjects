package com.chaosbuffalo.mkfaction.network;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class MKFactionDefinitionUpdatePacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MKFactionDefinitionUpdatePacket> TYPE = new CustomPacketPayload.Type<>(
            MKFactionMod.id("faction_definition_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MKFactionDefinitionUpdatePacket> STREAM_CODEC = StreamCodec.ofMember(
            MKFactionDefinitionUpdatePacket::toBytes, MKFactionDefinitionUpdatePacket::new
    );


    private final List<MKFactionData> factionData;

    private static class MKFactionData {
        public final MKFaction faction;
        public Tag encoded;

        public MKFactionData(MKFaction faction) {
            this.faction = faction;
        }
    }

    public MKFactionDefinitionUpdatePacket(Registry<MKFaction> factions) {
        this.factionData = new ArrayList<>();
        for (MKFaction faction : factions) {
            factionData.add(new MKFactionData(faction));
        }
    }

    public MKFactionDefinitionUpdatePacket(RegistryFriendlyByteBuf buffer) {
        factionData = new ArrayList<>();
        int count = buffer.readInt();
        Registry<MKFaction> registry = buffer.registryAccess().registryOrThrow(MKFactionRegistry.FACTION_REGISTRY_KEY);
        for (int i = 0; i < count; i++) {
            int regId = buffer.readVarInt();
            MKFaction faction = registry.byId(regId);
            if (faction != null) {
                MKFactionData data = new MKFactionData(faction);
                data.encoded = buffer.readNbt();
                factionData.add(data);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        Registry<MKFaction> registry = buffer.registryAccess().registryOrThrow(MKFactionRegistry.FACTION_REGISTRY_KEY);
        buffer.writeInt(factionData.size());
        for (MKFactionData data : factionData) {
            buffer.writeVarInt(registry.getId(data.faction));
            buffer.writeNbt((CompoundTag) data.faction.serialize(NbtOps.INSTANCE));
        }
    }

    public static void handle(final MKFactionDefinitionUpdatePacket packet, IPayloadContext context) {

        MKFactionMod.LOGGER.debug("Handling faction update packet");

        for (MKFactionData data : packet.factionData) {
            MKFaction faction = data.faction;
            MKFactionMod.LOGGER.debug("Parsing faction data: {}", faction.getId());

            faction.deserialize(new Dynamic<>(NbtOps.INSTANCE, data.encoded));
            MKFactionMod.LOGGER.info("Updated Faction: {} new score: {}",
                    faction.getId(), faction.getDefaultPlayerScore());
        }
    }
}
