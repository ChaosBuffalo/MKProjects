package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NotableNpcEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.quest.QuestStructureLocation;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.objective.UUIDInstanceData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestChainInstance;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class KillOneOfNotablesObjective extends QuestObjective<UUIDInstanceData> implements IKillObjectiveHandler {
    public static final MapCodec<KillOneOfNotablesObjective> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> {
        return builder.group(
                Codec.STRING.fieldOf("objectiveName").forGetter(i -> i.objectiveName),
                QuestStructureLocation.CODEC.fieldOf("structure").forGetter(i -> i.location),
                CommonCodecs.sortedSet(ResourceLocation.CODEC, ResourceLocation::compareNamespaced).fieldOf("npcDefinition").forGetter(i -> i.npcDefinitions)
        ).apply(builder, KillOneOfNotablesObjective::new);
    });


    private final Set<ResourceLocation> npcDefinitions;

    public KillOneOfNotablesObjective(String name, QuestStructureLocation structureLocation, Set<ResourceLocation> npcDef) {
        super(name, structureLocation);
        npcDefinitions = npcDef;
    }

    @Override
    public QuestObjectiveType<? extends QuestObjective<?>> getType() {
        return QuestObjectiveTypes.KILL_ONE_OF_NOTABLE.get();
    }

    @Override
    public List<Component> getDescription(IWorldNpcData worldData) {
        return List.of();
    }

    @Override
    public boolean onPlayerKillNpcDefEntity(Player player, PlayerQuestObjectiveData objectiveData, NpcDefinition def,
                                            LivingDeathEvent event, QuestData quest, PlayerQuestChainInstance playerChain) {
        if (!isComplete(objectiveData)) {
            UUIDInstanceData objData = getInstanceData(quest);
            boolean applies = IEntityNpcData.get(event.getEntity())
                    .map(x -> x.getNotableUUID().equals(objData.getUUID()))
                    .orElse(false);
            if (applies) {
                objectiveData.putBool("hasKilled", true);
                objectiveData.removeBlockPos("npcPos");
                player.sendSystemMessage(Component.translatable("mknpc.objective.kill_notable.complete",
                        event.getEntity().getDisplayName()).withStyle(ChatFormatting.GOLD));
                signalCompleted(objectiveData);
                playerChain.notifyDirty();
                return true;
            }
        }
        return false;
    }

    @Override
    public UUIDInstanceData generateInstanceData(Map<ResourceLocation, List<MKStructureEntry>> questStructures, Level level) {
        MKStructureEntry entry = questStructures.get(location.getStructureId()).get(location.getIndex());
        Optional<NotableNpcEntry> npcOpt = entry.getRandomNotableFromTypes(npcDefinitions, level.registryAccess());
        return npcOpt.map(x -> new UUIDInstanceData(x.getNotableId())).orElse(new UUIDInstanceData());
    }

    @Override
    public UUIDInstanceData instanceDataFactory() {
        return new UUIDInstanceData();
    }


    @Override
    public boolean isStructureRelevant(MKStructureEntry entry) {
        return location.getStructureId().equals(entry.getStructureName()) && entry.hasAnyNotableOfTypes(npcDefinitions, entry.getWorldData().getWorld().registryAccess());
    }

    @Override
    public PlayerQuestObjectiveData generatePlayerData(IWorldNpcData worldData, QuestData questData) {
        UUIDInstanceData objData = getInstanceData(questData);
        PlayerQuestObjectiveData newObj = new PlayerQuestObjectiveData(getObjectiveName(), getDescription(worldData));
        NotableNpcEntry notable = worldData.getNotableNpc(objData.getUUID());
        if (notable != null) {
            newObj.setDescription(Component.translatable("mknpc.objective.kill_notable.desc", notable.getName()));
            newObj.putBlockPos("npcPos", notable.getLocation());
        }
        newObj.putBool("hasKilled", false);
        return newObj;
    }
}
