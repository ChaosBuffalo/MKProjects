package com.chaosbuffalo.mkweapons.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkweapons.items.weapon.types.IMeleeWeaponType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncWeaponTypesPacket {
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

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(data.size());
        for (Map.Entry<ResourceLocation, CompoundTag> meleeData : data.entrySet()) {
            buffer.writeResourceLocation(meleeData.getKey());
            buffer.writeNbt(meleeData.getValue());
        }
    }

    public SyncWeaponTypesPacket(FriendlyByteBuf buffer) {
        int count = buffer.readInt();
        data = new HashMap<>();
        for (int i = 0; i < count; i++) {
            ResourceLocation typeName = buffer.readResourceLocation();
            CompoundTag typeData = buffer.readNbt();
            data.put(typeName, typeData);
        }
    }


    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        MKCore.LOGGER.debug("Handling player abilities update packet");
        ctx.enqueueWork(() -> {
            ClientHandlerWeaponPacket.handlePacket(this);
        });
        ctx.setPacketHandled(true);
    }
}
