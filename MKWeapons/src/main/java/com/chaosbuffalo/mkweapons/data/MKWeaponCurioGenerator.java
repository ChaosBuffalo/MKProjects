package com.chaosbuffalo.mkweapons.data;

import com.chaosbuffalo.mkweapons.MKWeapons;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosDataProvider;

import java.util.concurrent.CompletableFuture;

public class MKWeaponCurioGenerator extends CuriosDataProvider {
    public MKWeaponCurioGenerator(String modId, PackOutput output, ExistingFileHelper fileHelper, CompletableFuture<HolderLookup.Provider> registries) {
        super(modId, output, fileHelper, registries);
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
        createSlot("ring")
                .size(2);
        createSlot("earring")
                .icon(MKWeapons.id("slot/empty_earring_slot"))
                .size(2);
        createSlot("hands")
                .size(1);

//        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE,
//                () -> SlotTypePreset.RING.getMessageBuilder().size(2).build());
//        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE,
//                () -> new SlotTypeMessage.Builder("earring").priority(220).icon(
//                        new ResourceLocation(MKWeapons.MODID, "slot/empty_earring_slot")).size(2).build());
//        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE,
//                () -> SlotTypePreset.HANDS.getMessageBuilder().size(1).build());
    }
}
