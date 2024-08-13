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
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NpcDefinitionManager extends SimpleJsonResourceReloadListener {
    public static final String DEFINITION_FOLDER = "mknpcs";

    public static final ResourceLocation INVALID_NPC_DEF = new ResourceLocation(MKNpc.MODID, "npc_def.invalid");
    public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
//    public static final Map<ResourceLocation, NpcDefinition> DEFINITIONS = new HashMap<>();
    public static final Map<ResourceLocation, NpcDefinitionClient> CLIENT_DEFINITIONS = new HashMap<>();
    private static final Map<ResourceLocation, Codec<? extends NpcDefinitionOption>> OPTION_CODEC_MAP = new HashMap<>();
//    private static final Map<ResourceLocation, Codec<? extends INpcOptionEntry>> ENTRY_CODEC_MAP = new HashMap<>();

    public NpcDefinitionManager() {
        super(GSON, DEFINITION_FOLDER);
        MinecraftForge.EVENT_BUS.addListener(this::addReloadListener);
        MinecraftForge.EVENT_BUS.addListener(this::onDataPackSync);
    }

    public void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(this);
    }

    public static void setupDeserializers() {
    }

    public static final Codec<NpcDefinitionOption> NPC_OPTION_CODEC = CommonCodecs.createMapBackedDispatch(
            ResourceLocation.CODEC, OPTION_CODEC_MAP, NpcDefinitionOption::getName);
//    public static final Codec<INpcOptionEntry> ENTRY_CODEC = CommonCodecs.createMapBackedDispatch(
//            ResourceLocation.CODEC, ENTRY_CODEC_MAP, INpcOptionEntry::getOptionId);

//    public static void putOptionDeserializer(ResourceLocation optionName,
//                                             Codec<? extends NpcDefinitionOption> optionCodec) {
//        OPTION_CODEC_MAP.put(optionName, optionCodec);
//    }
//
//    public static void putOptionEntryDeserializer(ResourceLocation entryName,
//                                                  Codec<? extends INpcOptionEntry> codec) {
//        ENTRY_CODEC_MAP.put(entryName, codec);
//    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn,
                         ProfilerFiller profilerIn) {
//        CLIENT_DEFINITIONS.clear();
//
//        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
//            ResourceLocation resourcelocation = entry.getKey();
//            MKNpc.LOGGER.info("Found Npc Definition file: {}", resourcelocation);
//            NpcDefinition def = NpcDefinition.deserializeDefinitionFromDynamic(entry.getKey(),
//                    new Dynamic<>(JsonOps.INSTANCE, entry.getValue()));
//        }
//        resolveDefinitions();
    }

    public static void resolveDefinitions(MinecraftServer server) {
        Registry<NpcDefinition> registry = server.registryAccess().registryOrThrow(NpcRegistries.NPC_DEFINITIONS);
        registry.stream().forEach(def -> {
            boolean resolved = def.resolveParents(registry);
            if (!resolved) {
                MKNpc.LOGGER.error("Failed to resolve parents for {}",
                        def.getDefinitionName());
            }
        });
        registry.stream().forEach(def -> {
            def.resolveEntityType();
        });
    }

    public void onDataPackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            resolveDefinitions(event.getPlayerList().getServer());
        }
        NpcDefinitionClientUpdatePacket updatePacket = new NpcDefinitionClientUpdatePacket(event.getPlayerList().getServer().registryAccess().registryOrThrow(NpcRegistries.NPC_DEFINITIONS)
                .stream().map(NpcDefinitionClient::new).collect(Collectors.toList()));
        var vanilla = PacketHandler.getNetworkChannel().toVanillaPacket(updatePacket, NetworkDirection.PLAY_TO_CLIENT);

        ServerPlayer player = event.getPlayer();
        MKNpc.LOGGER.debug("Sending Npc Client Definitions to {}: {} npcs", player != null ? player : "all", CLIENT_DEFINITIONS.size());

        if (player != null) {
            PacketDistributor.PLAYER.with(() -> player).send(vanilla);
        } else {
            event.getPlayerList().broadcastAll(vanilla);
        }
    }

    public static NpcDefinition getDefinition(ResourceLocation name) {
        return null;
//        return DEFINITIONS.get(name);
    }
}
