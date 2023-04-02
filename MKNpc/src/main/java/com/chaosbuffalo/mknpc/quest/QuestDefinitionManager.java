package com.chaosbuffalo.mknpc.quest;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.quest.objectives.*;
import com.chaosbuffalo.mknpc.quest.requirements.HasEntitlementRequirement;
import com.chaosbuffalo.mknpc.quest.requirements.QuestRequirement;
import com.chaosbuffalo.mknpc.quest.rewards.GrantEntitlementReward;
import com.chaosbuffalo.mknpc.quest.rewards.MKLootReward;
import com.chaosbuffalo.mknpc.quest.rewards.QuestReward;
import com.chaosbuffalo.mknpc.quest.rewards.XpReward;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class QuestDefinitionManager extends SimpleJsonResourceReloadListener {
    public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static final String DEFINITION_FOLDER = "mkquests";
    private MinecraftServer server;
    private boolean serverStarted = false;

    public static final ResourceLocation INVALID_QUEST = new ResourceLocation(MKNpc.MODID, "invalid_quest");

    public static final Map<ResourceLocation, QuestDefinition> DEFINITIONS = new HashMap<>();

    public static final Map<ResourceLocation, Supplier<QuestObjective<?>>> OBJECTIVE_DESERIALIZERS = new HashMap<>();
    public static final Map<ResourceLocation, Supplier<QuestReward>> REWARD_DESERIALIZERS = new HashMap<>();
    public static final Map<ResourceLocation, Supplier<QuestRequirement>> REQUIREMENT_DESERIALIZERS = new HashMap<>();

    public QuestDefinitionManager(){
        super(GSON, DEFINITION_FOLDER);
        MinecraftForge.EVENT_BUS.register(this);
    }


    public static void putObjectiveDeserializer(ResourceLocation name, Supplier<QuestObjective<?>> deserializer){
        OBJECTIVE_DESERIALIZERS.put(name, deserializer);
    }

    public static void putRequirementDeserializer(ResourceLocation name, Supplier<QuestRequirement> deserializer){
        REQUIREMENT_DESERIALIZERS.put(name, deserializer);
    }

    public static void putRewardDeserializer(ResourceLocation name, Supplier<QuestReward> deserializer){
        REWARD_DESERIALIZERS.put(name, deserializer);
    }

    @SubscribeEvent
    public void subscribeEvent(AddReloadListenerEvent event){
        event.addListener(this);
    }

    @Nullable
    public static Supplier<QuestRequirement> getRequirementDeserializer(ResourceLocation name){
        return REQUIREMENT_DESERIALIZERS.get(name);
    }

    @Nullable
    public static Supplier<QuestObjective<?>> getObjectiveDeserializer(ResourceLocation name){
        return OBJECTIVE_DESERIALIZERS.get(name);
    }

    @Nullable
    public static Supplier<QuestReward> getRewardDeserializer(ResourceLocation name){
        return REWARD_DESERIALIZERS.get(name);
    }

    public static void setupDeserializers(){
        putObjectiveDeserializer(LootChestObjective.NAME, LootChestObjective::new);
        putObjectiveDeserializer(TalkToNpcObjective.NAME, TalkToNpcObjective::new);
        putObjectiveDeserializer(KillNpcDefObjective.NAME, KillNpcDefObjective::new);
        putObjectiveDeserializer(TradeItemsObjective.NAME, TradeItemsObjective::new);
        putRewardDeserializer(XpReward.TYPE_NAME, XpReward::new);
        putRewardDeserializer(MKLootReward.TYPE_NAME, MKLootReward::new);
        putRequirementDeserializer(HasEntitlementRequirement.TYPE_NAME, HasEntitlementRequirement::new);
        putObjectiveDeserializer(KillNotableNpcObjective.NAME, KillNotableNpcObjective::new);
        putRewardDeserializer(GrantEntitlementReward.TYPE_NAME, GrantEntitlementReward::new);
        putObjectiveDeserializer(QuestLootNpcObjective.NAME, QuestLootNpcObjective::new);
        putObjectiveDeserializer(QuestLootNotableObjective.NAME, QuestLootNotableObjective::new);
        putObjectiveDeserializer(KillWithAbilityObjective.NAME, KillWithAbilityObjective::new);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        DEFINITIONS.clear();
        for(Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            MKNpc.LOGGER.info("Found Quest Definition file: {}", resourcelocation);
            if (resourcelocation.getPath().startsWith("_")) continue; //Forge: filter anything beginning with "_" as it's used for metadata.
            QuestDefinition def = new QuestDefinition(resourcelocation);
            def.deserialize(new Dynamic<>(JsonOps.INSTANCE, entry.getValue()));
            DEFINITIONS.put(def.getName(), def);
        }
    }

    public static QuestDefinition getDefinition(ResourceLocation questName){
        return DEFINITIONS.get(questName);
    }

    @SubscribeEvent
    public void serverStop(ServerStoppingEvent event) {
        serverStarted = false;
        server = null;
    }

    @SubscribeEvent
    public void serverStart(ServerAboutToStartEvent event) {
        server = event.getServer();
        serverStarted = true;
    }

    @SubscribeEvent
    public void onDataPackSync(OnDatapackSyncEvent event) {

    }
}
