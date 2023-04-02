package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

public class PlayerAbilitiesSyncPacket {
    private final Map<ResourceLocation, CompoundTag> data;


    public PlayerAbilitiesSyncPacket(Collection<MKAbility> abilities) {
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

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        MKCore.LOGGER.debug("Handling player abilities update packet");
        ctx.enqueueWork(() -> {
            for (Entry<ResourceLocation, CompoundTag> abilityData : data.entrySet()) {
                MKAbility ability = MKCoreRegistry.ABILITIES.getValue(abilityData.getKey());
                if (ability != null) {
                    MKCore.LOGGER.debug("Updating ability with server data: {}", abilityData.getKey());
                    ability.deserializeDynamic(new Dynamic<>(NbtOps.INSTANCE, abilityData.getValue()));
                } else {
                    MKCore.LOGGER.warn("Skipping ability update for {}", abilityData.getKey());
                }

            }
        });
        ctx.setPacketHandled(true);
    }
}
