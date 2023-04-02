package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.network.NpcDefinitionClientUpdatePacket;
import com.chaosbuffalo.mknpc.network.PacketHandler;
import com.chaosbuffalo.mknpc.npc.option_entries.*;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class NpcDefinitionManager extends SimpleJsonResourceReloadListener {
    private MinecraftServer server;
    private boolean serverStarted = false;

    public static final ResourceLocation INVALID_NPC_DEF = new ResourceLocation(MKNpc.MODID, "npc_def.invalid");
    public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static final Map<ResourceLocation, NpcDefinition> DEFINITIONS = new HashMap<>();
    public static final Map<ResourceLocation, NpcDefinitionClient> CLIENT_DEFINITIONS = new HashMap<>();
    public static final Map<ResourceLocation, Supplier<INpcOptionEntry>> ENTRY_DESERIALIZERS = new HashMap<>();
    public static final Map<ResourceLocation, Supplier<NpcDefinitionOption>> OPTION_DESERIALIZERS = new HashMap<>();

    public NpcDefinitionManager() {
        super(GSON, "mknpcs");
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void serverStop(ServerStoppingEvent event) {
        serverStarted = false;
        server = null;
    }

    @SubscribeEvent
    public void subscribeEvent(AddReloadListenerEvent event){
        event.addListener(this);
    }

    @SubscribeEvent
    public void serverStart(ServerAboutToStartEvent event) {
        server = event.getServer();
        serverStarted = true;
    }

    public static void setupDeserializers(){
        putOptionEntryDeserializer(AbilitiesOption.NAME, AbilitiesOptionEntry::new);
        putOptionEntryDeserializer(EquipmentOption.NAME, EquipmentOptionEntry::new);
        putOptionDeserializer(EquipmentOption.NAME, EquipmentOption::new);
        putOptionDeserializer(AbilitiesOption.NAME, AbilitiesOption::new);
        putOptionDeserializer(AttributesOption.NAME, AttributesOption::new);
        putOptionDeserializer(NameOption.NAME, NameOption::new);
        putOptionDeserializer(ExperienceOption.NAME, ExperienceOption::new);
        putOptionDeserializer(FactionOption.NAME, FactionOption::new);
        putOptionDeserializer(DialogueOption.NAME, DialogueOption::new);
        putOptionDeserializer(FactionNameOption.NAME, FactionNameOption::new);
        putOptionEntryDeserializer(FactionNameOption.NAME, FactionNameOptionEntry::new);
        putOptionDeserializer(NotableOption.NAME, NotableOption::new);
        putOptionDeserializer(RenderGroupOption.NAME, RenderGroupOption::new);
        putOptionDeserializer(MKSizeOption.NAME, MKSizeOption::new);
        putOptionDeserializer(MKComboSettingsOption.NAME, MKComboSettingsOption::new);
        putOptionDeserializer(LungeSpeedOption.NAME, LungeSpeedOption::new);
        putOptionDeserializer(AbilityTrainingOption.NAME, AbilityTrainingOption::new);
        putOptionDeserializer(ParticleEffectsOption.NAME, ParticleEffectsOption::new);
        putOptionDeserializer(ExtraLootOption.NAME, ExtraLootOption::new);
        putOptionDeserializer(QuestOfferingOption.NAME, QuestOfferingOption::new);
        putOptionEntryDeserializer(QuestOfferingOption.NAME, QuestOptionsEntry::new);
        putOptionDeserializer(BossStageOption.NAME, BossStageOption::new);
        putOptionDeserializer(TempAbilitiesOption.NAME, TempAbilitiesOption::new);
        putOptionDeserializer(GhostOption.NAME, GhostOption::new);
        putOptionDeserializer(SkillOption.NAME, SkillOption::new);
    }

    public static void putOptionDeserializer(ResourceLocation optionName,
                                             Supplier<NpcDefinitionOption> optionFunction){
        OPTION_DESERIALIZERS.put(optionName, optionFunction);
    }

    public static void putOptionEntryDeserializer(ResourceLocation entryName,
                                                  Supplier<INpcOptionEntry> entryFunction){
        ENTRY_DESERIALIZERS.put(entryName, entryFunction);
    }

    @Nullable
    public static INpcOptionEntry getNpcOptionEntry(ResourceLocation entryName){
        if (!ENTRY_DESERIALIZERS.containsKey(entryName)){
            MKNpc.LOGGER.error("Failed to deserialize option entry {}", entryName);
            return null;
        }
        return ENTRY_DESERIALIZERS.get(entryName).get();
    }

    @Nullable
    public static NpcDefinitionOption getNpcOption(ResourceLocation optionName){

        if (!OPTION_DESERIALIZERS.containsKey(optionName)){
            MKNpc.LOGGER.error("Failed to deserialize option {}", optionName);
            return null;
        }
        return OPTION_DESERIALIZERS.get(optionName).get();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn,
                         ProfilerFiller profilerIn) {
        DEFINITIONS.clear();
        CLIENT_DEFINITIONS.clear();
        for(Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            MKNpc.LOGGER.info("Found Npc Definition file: {}", resourcelocation);
            if (resourcelocation.getPath().startsWith("_")) continue; //Forge: filter anything beginning with "_" as it's used for metadata.
            NpcDefinition def = NpcDefinition.deserializeDefinitionFromDynamic(entry.getKey(),
                    new Dynamic<>(JsonOps.INSTANCE, entry.getValue()));
            DEFINITIONS.put(def.getDefinitionName(), def);
        }
        resolveDefinitions();
        if (serverStarted){
            syncToPlayers();
        }
    }

    public static void resolveDefinitions(){
        List<ResourceLocation> toRemove = new ArrayList<>();
        for (NpcDefinition def : DEFINITIONS.values()){
            boolean resolved = def.resolveParents();
            if (!resolved){
                MKNpc.LOGGER.info("Failed to resolve parents for {}, removing definition",
                        def.getDefinitionName());
                toRemove.add(def.getDefinitionName());
            }
        }
        for (ResourceLocation loc : toRemove){
            DEFINITIONS.remove(loc);
        }
        for (NpcDefinition def : DEFINITIONS.values()){
            def.resolveEntityType();
            CLIENT_DEFINITIONS.put(def.getDefinitionName(), new NpcDefinitionClient(def));
        }
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void playerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event){
        if (event.getPlayer() instanceof ServerPlayer){
            NpcDefinitionClientUpdatePacket updatePacket = new NpcDefinitionClientUpdatePacket(
                    CLIENT_DEFINITIONS.values());
            MKNpc.LOGGER.info("Sending {} update packet", event.getPlayer());
            ((ServerPlayer) event.getPlayer()).connection.send(
                    PacketHandler.getNetworkChannel().toVanillaPacket(
                            updatePacket, NetworkDirection.PLAY_TO_CLIENT));
        }
    }

    public void syncToPlayers(){
        NpcDefinitionClientUpdatePacket updatePacket = new NpcDefinitionClientUpdatePacket(CLIENT_DEFINITIONS.values());
        if (server != null){
            server.getPlayerList().broadcastAll(PacketHandler.getNetworkChannel().toVanillaPacket(
                    updatePacket, NetworkDirection.PLAY_TO_CLIENT));
        }
    }

    public static NpcDefinition getDefinition(ResourceLocation name){
        return DEFINITIONS.get(name);
    }

}
