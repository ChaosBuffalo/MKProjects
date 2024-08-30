package com.chaosbuffalo.mkweapons.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.network.EntityCastPacket;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.types.IMeleeWeaponType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncWeaponTypesPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncWeaponTypesPacket> TYPE = new CustomPacketPayload.Type<>(MKWeapons.id("weapon_type_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncWeaponTypesPacket> STREAM_CODEC = StreamCodec.ofMember(
            SyncWeaponTypesPacket::toBytes, SyncWeaponTypesPacket::new
    );


    public final Map<ResourceLocation, CompoundTag> data;


    public SyncWeaponTypesPacket(Collection<IMeleeWeaponType> meleeTypes) {

        data = new HashMap<>();
        for (IMeleeWeaponType meleeType : meleeTypes) {
            Tag dyn = meleeType.serialize(NbtOps.INSTANCE);
            if (dyn instanceof CompoundTag) {
                data.put(meleeType.getName(), (CompoundTag) dyn);
            } else {
                throw new RuntimeException(String.format("Melee Weapon Type %s did not serialize to a CompoundNBT!",
                        meleeType.getName()));
            }
        }
    }

    public void toBytes(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(data.size());
        for (Map.Entry<ResourceLocation, CompoundTag> meleeData : data.entrySet()) {
            buffer.writeResourceLocation(meleeData.getKey());
            buffer.writeNbt(meleeData.getValue());
        }
    }

    public SyncWeaponTypesPacket(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readInt();
        data = new HashMap<>();
        for (int i = 0; i < count; i++) {
            ResourceLocation typeName = buffer.readResourceLocation();
            CompoundTag typeData = buffer.readNbt();
            data.put(typeName, typeData);
        }
    }

    public static void handle(final SyncWeaponTypesPacket packet, IPayloadContext context) {
        MKCore.LOGGER.debug("Handling player abilities update packet");
        context.enqueueWork(() -> {
            ClientHandlerWeaponPacket.handlePacket(packet);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return null;
    }
}
