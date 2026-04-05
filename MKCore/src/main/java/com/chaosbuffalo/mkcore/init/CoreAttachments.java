package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.training.EntityAbilityTrainer;
import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.MKServerPlayerData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;


public class CoreAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, MKCore.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MKPlayerData>> PLAYER_DATA_ATTACHMENT = ATTACHMENT_TYPES.register(
            "player_data", () -> AttachmentType
                    .serializable(CoreAttachments::createFreshPlayerData)
                    .build()
    );

    private static final IAttachmentSerializer<CompoundTag, MKEntityData> ENTITY_SERIALIZER = new IAttachmentSerializer<>() {
        @Override
        public MKEntityData read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
            if (holder instanceof LivingEntity entity && !(entity instanceof Player)) {
                // In case any entities installed a built-in cap in their constructor, check for an existing cap
                // instance and deserialize into that.
                return entity.getExistingData(ENTITY_DATA_ATTACHMENT).map(existing -> {
                    existing.deserializeNBT(provider, tag);
                    return existing;
                }).orElseGet(() -> new MKEntityData(entity));
            }
            throw new IllegalArgumentException("Cannot construct entity_data attachment for non-living entity " + holder);
        }

        @Nullable
        @Override
        public CompoundTag write(MKEntityData attachment, HolderLookup.Provider provider) {
            return attachment.serializeNBT(provider);
        }
    };

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MKEntityData>> ENTITY_DATA_ATTACHMENT = ATTACHMENT_TYPES.register(
            "entity_data", () -> AttachmentType
                    .builder(CoreAttachments::createFreshEntityData)
                    .serialize(ENTITY_SERIALIZER)
                    .build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EntityAbilityTrainer>> ABILITY_TRAINER = ATTACHMENT_TYPES.register(
            "ability_trainer", () -> AttachmentType.builder(holder -> {
                if (holder instanceof Entity entity && !(entity instanceof Player)) {
                    return new EntityAbilityTrainer(entity);
                }
                throw new IllegalArgumentException("Cannot construct ability_trainer attachment for non-entity " + holder);
            }).build()
    );


    static MKPlayerData createFreshPlayerData(IAttachmentHolder holder) {
        if (holder instanceof ServerPlayer player) {
            return new MKServerPlayerData(player);
        } else if (holder instanceof Player player) {
            return new MKPlayerData(player);
        }
        throw new IllegalArgumentException("Cannot construct player attachment for non-player entity " + holder);
    }

    static MKEntityData createFreshEntityData(IAttachmentHolder holder) {
        if (holder instanceof LivingEntity entity && !(entity instanceof Player)) {
            return new MKEntityData(entity);
        }
        throw new IllegalArgumentException("Cannot construct entity_data attachment for non-living entity " + holder);
    }

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
