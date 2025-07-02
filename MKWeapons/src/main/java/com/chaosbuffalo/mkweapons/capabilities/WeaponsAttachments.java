package com.chaosbuffalo.mkweapons.capabilities;

import com.chaosbuffalo.mkweapons.MKWeapons;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class WeaponsAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, MKWeapons.MODID);


    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
