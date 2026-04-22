package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.chaosbuffalo.mknpc.MKNpc;
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
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QuestLootNotableObjective extends QuestObjective<UUIDInstanceData> implements IKillObjectiveHandler {
    public static final MapCodec<QuestLootNotableObjective> MAP_CODEC = RecordCodecBuilder.<QuestLootNotableObjective>mapCodec(builder -> builder.group(
            Codec.STRING.fieldOf("objectiveName").forGetter(i -> i.objectiveName),
            QuestStructureLocation.CODEC.fieldOf("structure").forGetter(i -> i.location),
            NpcDefinition.KEY_CODEC.fieldOf("npcDefinition").forGetter(i -> i.npcDefinition),
            Codec.DOUBLE.optionalFieldOf("chance", 1.0).forGetter(i -> i.chanceToFind),
            Codec.INT.optionalFieldOf("count", 1).forGetter(i -> i.requiredCount),
            ComponentSerialization.CODEC.fieldOf("itemDescription").forGetter(i -> i.itemDescription)
    ).apply(builder, QuestLootNotableObjective::new));


    public static final ResourceLocation NAME = MKNpc.id("objective.quest_loot_notable");
    protected Component itemDescription;
    private final ResourceKey<NpcDefinition> npcDefinition;
    private final double chanceToFind;
    private final int requiredCount;

    public QuestLootNotableObjective(String name, QuestStructureLocation structureLocation, ResourceKey<NpcDefinition> npcDefinition,
                                     double chance, int count, Component itemDescription) {
        super(name, structureLocation);
        this.npcDefinition = npcDefinition;
        chanceToFind = chance;
        requiredCount = count;
        this.itemDescription = itemDescription;
    }

    @Override
    public QuestObjectiveType<? extends QuestObjective<?>> getType() {
        return QuestObjectiveTypes.QUEST_LOOT_NOTABLE.get();
    }

    @Override
    public List<Component> getDescription(IWorldNpcData worldData) {
        return List.of();
    }

    private MutableComponent getDescriptionWithCount(Component name, int count) {
        return Component.translatable("mknpc.objective.quest_loot_npc.desc", itemDescription, name,
                MKAbility.INTEGER_FORMATTER.format(count), MKAbility.INTEGER_FORMATTER.format(requiredCount));
    }

    private MutableComponent getProgressMessage(LivingEntity entity, int count) {
        return Component.translatable("mknpc.objective.quest_loot_npc.progress", itemDescription, entity.getName(),
                MKAbility.INTEGER_FORMATTER.format(count), MKAbility.INTEGER_FORMATTER.format(requiredCount));
    }

    @Override
    public boolean onPlayerKillNpcDefEntity(Player player, PlayerQuestObjectiveData objectiveData, NpcDefinition def,
                                            LivingDeathEvent event, QuestData quest, PlayerQuestChainInstance playerChain) {
        if (!isComplete(objectiveData)) {
            UUIDInstanceData objData = getInstanceData(quest);
            boolean applies = IEntityNpcData.get(event.getEntity())
                    .map(x -> x.getNotableUUID().equals(objData.getUUID()))
                    .orElse(false);
            if (applies && MathUtils.rollLuck(player, chanceToFind)) {
                int currentCount = objectiveData.getInt("lootCount");
                currentCount++;
                objectiveData.putInt("lootCount", currentCount);
                objectiveData.setDescription(getDescriptionWithCount(event.getEntity().getName(), currentCount));
                player.sendSystemMessage(getProgressMessage(event.getEntity(), currentCount)
                        .withStyle(ChatFormatting.GOLD));
                if (currentCount == requiredCount) {
                    signalCompleted(objectiveData);
                    objectiveData.removeBlockPos("npcPos");
                }
                playerChain.notifyDirty();
                return true;
            }
        }
        return false;
    }

    @Override
    public UUIDInstanceData generateInstanceData(Map<QuestStructureLocation, MKStructureEntry> questStructures, Level level) {
        MKStructureEntry entry = questStructures.get(location);
        Optional<NotableNpcEntry> npcOpt = entry.getFirstNotableOfType(npcDefinition);
        return npcOpt.map(x -> new UUIDInstanceData(x.getNotableId())).orElse(new UUIDInstanceData());
    }

    @Override
    public UUIDInstanceData instanceDataFactory() {
        return new UUIDInstanceData();
    }


    @Override
    public boolean isStructureRelevant(MKStructureEntry entry) {
        return location.getStructureId().equals(entry.getStructureName()) && entry.hasNotableOfType(npcDefinition);
    }

    @Override
    public PlayerQuestObjectiveData generatePlayerData(IWorldNpcData worldData, QuestData questData) {
        UUIDInstanceData objData = getInstanceData(questData);
        PlayerQuestObjectiveData newObj = new PlayerQuestObjectiveData(getObjectiveName(), getDescription(worldData));
        NotableNpcEntry notable = worldData.getNotableNpc(objData.getUUID());
        if (notable != null) {
            newObj.setDescription(getDescriptionWithCount(notable.getName(), 0));
            newObj.putBlockPos("npcPos", notable.getLocation());
        }
        newObj.putInt("lootCount", 0);
        return newObj;
    }
}