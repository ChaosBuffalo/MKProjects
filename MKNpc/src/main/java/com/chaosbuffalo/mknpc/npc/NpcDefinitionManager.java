package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.network.NpcDefinitionClientUpdatePacket;
import com.chaosbuffalo.mknpc.network.PacketHandler;
import com.chaosbuffalo.mknpc.npc.option_entries.*;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NpcDefinitionManager extends SimpleJsonResourceReloadListener {
    public static final String DEFINITION_FOLDER = "mknpcs";
    private MinecraftServer server;
    private boolean serverStarted = false;

    public static final ResourceLocation INVALID_NPC_DEF = new ResourceLocation(MKNpc.MODID, "npc_def.invalid");
    public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static final Map<ResourceLocation, NpcDefinition> DEFINITIONS = new HashMap<>();
    public static final Map<ResourceLocation, NpcDefinitionClient> CLIENT_DEFINITIONS = new HashMap<>();
    private static final Map<ResourceLocation, Codec<? extends NpcDefinitionOption>> OPTION_CODEC_MAP = new HashMap<>();
    private static final Map<ResourceLocation, Codec<? extends INpcOptionEntry>> ENTRY_CODEC_MAP = new HashMap<>();

    public NpcDefinitionManager() {
        super(GSON, DEFINITION_FOLDER);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void serverStop(ServerStoppingEvent event) {
        serverStarted = false;
        server = null;
    }

    @SubscribeEvent
    public void subscribeEvent(AddReloadListenerEvent event) {
        event.addListener(this);
    }

    @SubscribeEvent
    public void serverStart(ServerAboutToStartEvent event) {
        server = event.getServer();
        serverStarted = true;
    }

    public static void setupDeserializers() {
        putOptionEntryDeserializer(AbilitiesOption.NAME, AbilitiesOptionEntry.CODEC);
        putOptionEntryDeserializer(EquipmentOption.NAME, EquipmentOptionEntry.CODEC);
        putOptionDeserializer(EquipmentOption.NAME, EquipmentOption.CODEC);
        putOptionDeserializer(AbilitiesOption.NAME, AbilitiesOption.CODEC);
        putOptionDeserializer(AttributesOption.NAME, AttributesOption.CODEC);
        putOptionDeserializer(NameOption.NAME, NameOption.CODEC);
        putOptionDeserializer(ExperienceOption.NAME, ExperienceOption.CODEC);
        putOptionDeserializer(FactionOption.NAME, FactionOption.CODEC);
        putOptionDeserializer(DialogueOption.NAME, DialogueOption.CODEC);
        putOptionDeserializer(FactionNameOption.NAME, FactionNameOption.CODEC);
        putOptionEntryDeserializer(FactionNameOption.NAME, FactionNameOptionEntry.CODEC);
        putOptionDeserializer(NotableOption.NAME, NotableOption.CODEC);
        putOptionDeserializer(RenderGroupOption.NAME, RenderGroupOption.CODEC);
        putOptionDeserializer(MKSizeOption.NAME, MKSizeOption.CODEC);
        putOptionDeserializer(MKComboSettingsOption.NAME, MKComboSettingsOption.CODEC);
        putOptionDeserializer(LungeSpeedOption.NAME, LungeSpeedOption.CODEC);
        putOptionDeserializer(AbilityTrainingOption.NAME, AbilityTrainingOption.CODEC);
        putOptionDeserializer(ParticleEffectsOption.NAME, ParticleEffectsOption.CODEC);
        putOptionDeserializer(ExtraLootOption.NAME, ExtraLootOption.CODEC);
        putOptionDeserializer(QuestOfferingOption.NAME, QuestOfferingOption.CODEC);
        putOptionEntryDeserializer(QuestOfferingOption.NAME, QuestOptionsEntry.CODEC);
        putOptionDeserializer(BossStageOption.NAME, BossStageOption.CODEC);
        putOptionDeserializer(TempAbilitiesOption.NAME, TempAbilitiesOption.CODEC);
        putOptionDeserializer(GhostOption.NAME, GhostOption.CODEC);
        putOptionDeserializer(SkillOption.NAME, SkillOption.CODEC);
        putOptionDeserializer(FactionBattlecryOption.NAME, FactionBattlecryOption.CODEC);
        putOptionEntryDeserializer(FactionBattlecryOption.NAME, FactionBattlecryOptionEntry.CODEC);
    }

    public static final Codec<NpcDefinitionOption> NPC_OPTION_CODEC = CommonCodecs.createMapBackedDispatch(
            ResourceLocation.CODEC, OPTION_CODEC_MAP, NpcDefinitionOption::getName);
    public static final Codec<INpcOptionEntry> ENTRY_CODEC = CommonCodecs.createMapBackedDispatch(
            ResourceLocation.CODEC, ENTRY_CODEC_MAP, INpcOptionEntry::getOptionId);

    public static void putOptionDeserializer(ResourceLocation optionName,
                                             Codec<? extends NpcDefinitionOption> optionCodec) {
        OPTION_CODEC_MAP.put(optionName, optionCodec);
    }

    public static void putOptionEntryDeserializer(ResourceLocation entryName,
                                                  Codec<? extends INpcOptionEntry> codec) {
        ENTRY_CODEC_MAP.put(entryName, codec);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn,
                         ProfilerFiller profilerIn) {
        DEFINITIONS.clear();
        CLIENT_DEFINITIONS.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            MKNpc.LOGGER.info("Found Npc Definition file: {}", resourcelocation);
            if (resourcelocation.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.
            NpcDefinition def = NpcDefinition.deserializeDefinitionFromDynamic(entry.getKey(),
                    new Dynamic<>(JsonOps.INSTANCE, entry.getValue()));
            DEFINITIONS.put(def.getDefinitionName(), def);
        }
        resolveDefinitions();
        if (serverStarted) {
            syncToPlayers();
        }
    }

    public static void resolveDefinitions() {
        List<ResourceLocation> toRemove = new ArrayList<>();
        for (NpcDefinition def : DEFINITIONS.values()) {
            boolean resolved = def.resolveParents();
            if (!resolved) {
                MKNpc.LOGGER.info("Failed to resolve parents for {}, removing definition",
                        def.getDefinitionName());
                toRemove.add(def.getDefinitionName());
            }
        }
        for (ResourceLocation loc : toRemove) {
            DEFINITIONS.remove(loc);
        }
        for (NpcDefinition def : DEFINITIONS.values()) {
            def.resolveEntityType();
            CLIENT_DEFINITIONS.put(def.getDefinitionName(), new NpcDefinitionClient(def));
        }
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void playerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            NpcDefinitionClientUpdatePacket updatePacket = new NpcDefinitionClientUpdatePacket(
                    CLIENT_DEFINITIONS.values());
            MKNpc.LOGGER.info("Sending {} update packet", event.getEntity());
            serverPlayer.connection.send(
                    PacketHandler.getNetworkChannel().toVanillaPacket(
                            updatePacket, NetworkDirection.PLAY_TO_CLIENT));
        }
    }

    public void syncToPlayers() {
        NpcDefinitionClientUpdatePacket updatePacket = new NpcDefinitionClientUpdatePacket(CLIENT_DEFINITIONS.values());
        if (server != null) {
            server.getPlayerList().broadcastAll(PacketHandler.getNetworkChannel().toVanillaPacket(
                    updatePacket, NetworkDirection.PLAY_TO_CLIENT));
        }
    }

    public static NpcDefinition getDefinition(ResourceLocation name) {
        return DEFINITIONS.get(name);
    }

}
