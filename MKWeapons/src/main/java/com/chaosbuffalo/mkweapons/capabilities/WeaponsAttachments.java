package com.chaosbuffalo.mkweapons.capabilities;

import com.chaosbuffalo.mkweapons.MKWeapons;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class WeaponsAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, MKWeapons.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ArrowDataHandler>> ARROW_DATA_ATTACHMENT = ATTACHMENT_TYPES.register(
            "arrow_data", () -> AttachmentType.serializable(holder -> {
                if (holder instanceof AbstractArrow entity) {
                    return new ArrowDataHandler(entity);
                }
                throw new IllegalArgumentException("Cannot construct entity_data attachment for non-living entity " + holder);
            }).build()
    );


    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
