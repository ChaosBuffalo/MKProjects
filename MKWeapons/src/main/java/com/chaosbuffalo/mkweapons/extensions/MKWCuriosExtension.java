package com.chaosbuffalo.mkweapons.extensions;

import com.chaosbuffalo.mkweapons.MKWeapons;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.InterModComms;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

public class MKWCuriosExtension {

    public static void sendExtension() {
        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE,
                () -> SlotTypePreset.RING.getMessageBuilder().size(2).build());
        InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE,
                () -> new SlotTypeMessage.Builder("earring").priority(220).icon(
                        new ResourceLocation(MKWeapons.MODID, "item/empty_earring_slot")).size(2).build());
    }
}
