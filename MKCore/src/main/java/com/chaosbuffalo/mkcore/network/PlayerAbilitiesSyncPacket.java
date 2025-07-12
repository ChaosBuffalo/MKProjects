package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PlayerAbilitiesSyncPacket implements CustomPacketPayload{
    private final Map<ResourceLocation, CompoundTag> data;

    public static final CustomPacketPayload.Type<PlayerAbilitiesSyncPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "player_abilities_sync"));

    public static final StreamCodec<FriendlyByteBuf, PlayerAbilitiesSyncPacket> STREAM_CODEC = StreamCodec.ofMember(
            PlayerAbilitiesSyncPacket::toBytes, PlayerAbilitiesSyncPacket::new
    );


    public PlayerAbilitiesSyncPacket(Registry<MKAbility> abilities) {
        data = new HashMap<>();
        for (MKAbility ability : abilities) {
            Tag dyn = ability.serializeDynamic(NbtOps.INSTANCE);
            if (dyn instanceof CompoundTag) {
                data.put(ability.getAbilityId(), (CompoundTag) dyn);
            } else {
                throw new RuntimeException(String.format("Ability %s did not serialize to a CompoundNBT!", ability.getAbilityId()));
            }
        }
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(data.size());
        for (Entry<ResourceLocation, CompoundTag> abilityData : data.entrySet()) {
            buffer.writeResourceLocation(abilityData.getKey());
            buffer.writeNbt(abilityData.getValue());
        }
    }

    public PlayerAbilitiesSyncPacket(FriendlyByteBuf buffer) {
        int count = buffer.readInt();
        data = new HashMap<>();
        for (int i = 0; i < count; i++) {
            ResourceLocation abilityName = buffer.readResourceLocation();
            CompoundTag abilityData = buffer.readNbt();
            data.put(abilityName, abilityData);
        }
    }

    public static void handle(final PlayerAbilitiesSyncPacket packet, IPayloadContext context) {
        for (Entry<ResourceLocation, CompoundTag> abilityData : packet.data.entrySet()) {
            MKAbility ability = MKCoreRegistry.ABILITIES.get(abilityData.getKey());
            if (ability != null) {
                if (MKCore.DEV_LOGGING) {
                    MKCore.LOGGER.debug("Updating ability with server data: {}", abilityData.getKey());
                }
                ability.deserializeDynamic(new Dynamic<>(NbtOps.INSTANCE, abilityData.getValue()));
            } else {
                MKCore.LOGGER.warn("Skipping ability update for {}", abilityData.getKey());
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
