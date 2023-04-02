package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
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
import java.util.function.Supplier;

public class TalentDefinitionSyncPacket {
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

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(data.size());
        for (Map.Entry<ResourceLocation, CompoundTag> abilityData : data.entrySet()) {
            buffer.writeResourceLocation(abilityData.getKey());
            buffer.writeNbt(abilityData.getValue());
        }
    }

    public TalentDefinitionSyncPacket(FriendlyByteBuf buffer) {
        int count = buffer.readInt();
        for (int i = 0; i < count; i++) {
            ResourceLocation abilityName = buffer.readResourceLocation();
            CompoundTag abilityData = buffer.readNbt();
            data.put(abilityName, abilityData);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        MKCore.LOGGER.debug("Handling player talent definition update packet");
        ctx.enqueueWork(() -> {
            for (Map.Entry<ResourceLocation, CompoundTag> abilityData : data.entrySet()) {
                TalentTreeDefinition definition = TalentTreeDefinition.deserialize(abilityData.getKey(), new Dynamic<>(NbtOps.INSTANCE, abilityData.getValue()));
                MKCore.getTalentManager().registerTalentTree(definition);
            }
        });
        ctx.setPacketHandled(true);
    }
}
