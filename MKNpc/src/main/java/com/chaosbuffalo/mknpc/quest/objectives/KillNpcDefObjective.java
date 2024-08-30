package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.objective.EmptyInstanceData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestChainInstance;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.List;
import java.util.Map;

public class KillNpcDefObjective extends QuestObjective<EmptyInstanceData> implements IKillObjectiveHandler {
    public static final MapCodec<KillNpcDefObjective> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> {
        return builder.group(
                Codec.STRING.fieldOf("objectiveName").forGetter(i -> i.objectiveName),
                ResourceLocation.CODEC.fieldOf("npcDefinition").forGetter(i -> i.npcDefinition),
                Codec.INT.fieldOf("count").forGetter(i -> i.requiredCount)
        ).apply(builder, KillNpcDefObjective::new);
    });

    public static final ResourceLocation NAME = MKNpc.id("objective.kill_npc_def");
    private final ResourceLocation npcDefinition;
    private final int requiredCount;

    public KillNpcDefObjective(String name, ResourceLocation definition, int count) {
        super(name);
        npcDefinition = definition;
        requiredCount = count;
    }

    @Override
    public QuestObjectiveType<? extends QuestObjective<?>> getType() {
        return QuestObjectiveTypes.KILL_NPC_DEF.get();
    }

    @Override
    public EmptyInstanceData generateInstanceData(Map<ResourceLocation, List<MKStructureEntry>> questStructures, Level level) {
        return new EmptyInstanceData();
    }

    @Override
    public EmptyInstanceData instanceDataFactory() {
        return new EmptyInstanceData();
    }

    @Override
    public List<Component> getDescription(IWorldNpcData worldData) {
        return List.of(getDescriptionWithKillCount(0, worldData.getWorld().getServer()));
    }

    private MutableComponent getDescriptionWithKillCount(int count, MinecraftServer server) {
        NpcDefinition def = server.registryAccess().registryOrThrow(NpcRegistries.NPC_DEFINITIONS).get(npcDefinition);
        return Component.translatable("mknpc.objective.kill_npc_def.desc", def.getDisplayName(),
                count, requiredCount);
    }

    @Override
    public PlayerQuestObjectiveData generatePlayerData(IWorldNpcData worldData, QuestData questData) {
        PlayerQuestObjectiveData newObj = new PlayerQuestObjectiveData(getObjectiveName(), getDescription(worldData));
        newObj.putInt("killCount", 0);
        return newObj;
    }

    @Override
    public boolean onPlayerKillNpcDefEntity(Player player, PlayerQuestObjectiveData objectiveData, NpcDefinition def,
                                            LivingDeathEvent event, QuestData questData, PlayerQuestChainInstance playerChain) {
        if (def.getDefinitionName().equals(npcDefinition) && !isComplete(objectiveData)) {
            int currentCount = objectiveData.getInt("killCount");
            currentCount++;
            objectiveData.putInt("killCount", currentCount);
            objectiveData.setDescription(getDescriptionWithKillCount(currentCount, player.getServer()));
            player.sendSystemMessage(getDescriptionWithKillCount(currentCount, player.getServer()).withStyle(ChatFormatting.GOLD));
            if (currentCount == requiredCount) {
                signalCompleted(objectiveData);
            }
            playerChain.notifyDirty();
            return true;

        }
        return false;
    }
}
