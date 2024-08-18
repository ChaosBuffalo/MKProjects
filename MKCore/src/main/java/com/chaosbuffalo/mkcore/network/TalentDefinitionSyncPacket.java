package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TalentDefinitionSyncPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TalentDefinitionSyncPacket> TYPE = new CustomPacketPayload.Type<>(
            MKCore.id("talent_definition_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TalentDefinitionSyncPacket> STREAM_CODEC = StreamCodec.ofMember(
            TalentDefinitionSyncPacket::toBytes, TalentDefinitionSyncPacket::new
    );

    private final Map<ResourceLocation, CompoundTag> data = new HashMap<>();

    public TalentDefinitionSyncPacket(Collection<TalentTreeDefinition> definitions) {
        for (TalentTreeDefinition treeDefinition : definitions) {
            Tag serialized = treeDefinition.serialize(NbtOps.INSTANCE);
            if (serialized instanceof CompoundTag) {
                data.put(treeDefinition.getTreeId(), (CompoundTag) serialized);
            } else {
                throw new IllegalArgumentException("TalentTreeDefinition did not serialize to a CompoundNBT!");
            }
        }
    }

    public TalentDefinitionSyncPacket(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readInt();
        for (int i = 0; i < count; i++) {
            ResourceLocation abilityName = buffer.readResourceLocation();
            CompoundTag abilityData = buffer.readNbt();
            data.put(abilityName, abilityData);
        }
    }

    public static void handle(TalentDefinitionSyncPacket packet, IPayloadContext context) {
        MKCore.LOGGER.debug("Handling player talent definition update packet");

        for (Map.Entry<ResourceLocation, CompoundTag> abilityData : packet.data.entrySet()) {
            var ops = context.player().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            TalentTreeDefinition definition = TalentTreeDefinition.deserialize(abilityData.getKey(), new Dynamic<>(ops, abilityData.getValue()));
            MKCore.getTalentManager().registerTalentTree(definition);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(data.size());
        for (Map.Entry<ResourceLocation, CompoundTag> abilityData : data.entrySet()) {
            buffer.writeResourceLocation(abilityData.getKey());
            buffer.writeNbt(abilityData.getValue());
        }
    }
}
