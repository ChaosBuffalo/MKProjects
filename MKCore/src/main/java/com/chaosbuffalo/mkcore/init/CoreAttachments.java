package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;


public class CoreAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, MKCore.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MKPlayerData>> PLAYER_DATA_ATTACHMENT = ATTACHMENT_TYPES.register(
            "player_data", () -> AttachmentType.serializable((attachee) -> {
                if (attachee instanceof Player player) {
                    return new MKPlayerData(player);
                }
                return null;
            }).build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MKEntityData>> ENTITY_DATA_ATTACHMENT = ATTACHMENT_TYPES.register(
            "entity_data", () -> AttachmentType.serializable((attachee) -> {
                if (attachee instanceof LivingEntity entity && !(attachee instanceof Player)) {
                    return new MKEntityData(entity);
                }
                return null;
            }).build()
    );

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
