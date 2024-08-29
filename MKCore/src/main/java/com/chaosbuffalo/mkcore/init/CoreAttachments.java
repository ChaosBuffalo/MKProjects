package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.MKServerPlayerData;
import net.minecraft.server.level.ServerPlayer;
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
            "player_data", () -> AttachmentType.serializable(holder -> {
                if (holder instanceof ServerPlayer player) {
                    return new MKServerPlayerData(player);
                } else if (holder instanceof Player player) {
                    return new MKPlayerData(player);
                }
                throw new IllegalArgumentException("Cannot construct player attachment for non-player entity " + holder);
            }).build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MKEntityData>> ENTITY_DATA_ATTACHMENT = ATTACHMENT_TYPES.register(
            "entity_data", () -> AttachmentType.serializable(holder -> {
                if (holder instanceof LivingEntity entity && !(entity instanceof Player)) {
                    return new MKEntityData(entity);
                }
                throw new IllegalArgumentException("Cannot construct entity_data attachment for non-living entity " + holder);
            }).build()
    );

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
