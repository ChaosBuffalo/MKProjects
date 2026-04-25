package com.chaosbuffalo.mknpc.init;

import com.chaosbuffalo.mkcore.core.MKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.MKServerPlayerData;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.*;
import com.chaosbuffalo.mknpc.world.gen.workspace.capability.MKStructureWorkspaceDataHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MKNpcAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, MKNpc.MODID);


    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EntityNpcDataHandler>> NPC_DATA = ATTACHMENT_TYPES.register(
            "npc_data", () -> AttachmentType.serializable(holder -> {
                if (holder instanceof Player) {
                    throw new IllegalArgumentException("Cannot construct npc data attachment for player entity " + holder);
                } else if (holder instanceof LivingEntity living) {
                    return new EntityNpcDataHandler(living);
                }
                throw new IllegalArgumentException("Cannot construct npc data attachment for non-living entity " +  holder);
            }).build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerQuestingDataHandler>> PLAYER_QUEST_DATA = ATTACHMENT_TYPES.register(
            "player_quest_data", () -> AttachmentType.serializable(holder -> {
                if (holder instanceof Player player) {
                    return new PlayerQuestingDataHandler(player);
                }
                throw new IllegalArgumentException("Cannot construct player quest data attachment for non-player " +  holder);
            }).build()
    );


    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WorldNpcDataHandler>> WORLD_NPC_DATA = ATTACHMENT_TYPES.register(
            "world_npc_data", () -> AttachmentType.serializable(holder -> {
                if (holder instanceof Level level) {
                    return new WorldNpcDataHandler(level);
                }
                throw new IllegalArgumentException("Cannot attach world npc data to non level holder " + holder);
            }).build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MKStructureWorkspaceDataHandler>> STRUCTURE_WORKSPACE_DATA = ATTACHMENT_TYPES.register(
            "structure_workspace_data", () -> AttachmentType.serializable(holder -> {
                if (holder instanceof Level level) {
                    return new MKStructureWorkspaceDataHandler(level);
                }
                throw new IllegalArgumentException("Cannot attach structure workspace data to non level holder " + holder);
            }).build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ChunkNpcDataHandler>> CHUNK_NPC_DATA = ATTACHMENT_TYPES.register(
            "chunk_npc_data", () -> AttachmentType.serializable(holder -> {
                if (holder instanceof LevelChunk chunk) {
                    return new ChunkNpcDataHandler(chunk);
                }
                throw new IllegalArgumentException("Cannot attach chunk npc data to non level chunk holder " + holder);
            }).build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ChestNpcDataHandler>> CHEST_NPC_DATA = ATTACHMENT_TYPES.register(
            "chest_npc_data", () -> AttachmentType.serializable(holder -> {
                if (holder instanceof ChestBlockEntity chest) {
                    return new ChestNpcDataHandler(chest);
                }
                throw new IllegalArgumentException("Cannot attach chest npc data to non chest block entity holder " + holder);
            }).build()
    );

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
