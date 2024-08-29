package com.chaosbuffalo.mkfaction.init;

import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.capabilities.MobFactionHandler;
import com.chaosbuffalo.mkfaction.capabilities.PlayerFactionHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class FactionAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, MKFactionMod.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerFactionHandler>> PLAYER_DATA_ATTACHMENT = ATTACHMENT_TYPES.register(
            "player_faction_data", () -> AttachmentType.serializable(holder -> {
                if (holder instanceof Player player) {
                    return new PlayerFactionHandler(player);
                }
                throw new IllegalArgumentException("Cannot construct player_faction_data attachment for non-player entity " + holder);
            }).build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MobFactionHandler>> ENTITY_DATA_ATTACHMENT = ATTACHMENT_TYPES.register(
            "mob_faction_data", () -> AttachmentType.serializable(holder -> {
                if (holder instanceof LivingEntity entity && !(entity instanceof Player)) {
                    return new MobFactionHandler(entity);
                }
                throw new IllegalArgumentException("Cannot construct mob_faction_data attachment for non-living entity " + holder);
            }).build()
    );

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
