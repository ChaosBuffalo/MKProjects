package com.chaosbuffalo.mknpc.quest.objectives;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.quest.QuestStructureLocation;
import com.chaosbuffalo.mknpc.quest.data.QuestData;
import com.chaosbuffalo.mknpc.quest.data.objective.EmptyInstanceData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestChainInstance;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestLootTypeTagObjective extends QuestObjective<EmptyInstanceData> implements IKillObjectiveHandler {
    public static final MapCodec<QuestLootTypeTagObjective> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> {
        return builder.group(
                Codec.STRING.fieldOf("objectiveName").forGetter(i -> i.objectiveName),
                TagKey.codec(BuiltInRegistries.ENTITY_TYPE.key()).fieldOf("tag").forGetter(i -> i.tag),
                Codec.DOUBLE.optionalFieldOf("chance", 1.0).forGetter(i -> i.chanceToFind),
                Codec.INT.optionalFieldOf("count", 1).forGetter(i -> i.requiredCount),
                ComponentSerialization.CODEC.fieldOf("itemDescription").forGetter(i -> i.itemDescription),
                Codec.STRING.fieldOf("tagDesc").forGetter(i -> i.tagDesc)
        ).apply(builder, QuestLootTypeTagObjective::new);
    });

    private final TagKey<EntityType<?>> tag;
    public static final ResourceLocation NAME = MKNpc.id("objective.quest_loot_type_tag");
    protected double chanceToFind;
    private final int requiredCount;
    protected Component itemDescription;
    protected List<Component> description = new ArrayList<>();
    private final String tagDesc;


    public QuestLootTypeTagObjective(String name, TagKey<EntityType<?>> tag,
                                     double chance, int count, Component itemDescription,
                                     String tagDesc) {
        super(name);
        this.tag = tag;
        chanceToFind = chance;
        requiredCount = count;
        this.itemDescription = itemDescription;
        this.tagDesc = tagDesc;
    }

    @Override
    public QuestObjectiveType<? extends QuestObjective<?>> getType() {
        return QuestObjectiveTypes.QUEST_LOOT_TYPE_TAG.get();
    }

    @Override
    public EmptyInstanceData generateInstanceData(Map<QuestStructureLocation, MKStructureEntry> questStructures, Level level) {
        return new EmptyInstanceData();
    }

    @Override
    public EmptyInstanceData instanceDataFactory() {
        return new EmptyInstanceData();
    }


    @Override
    public List<Component> getDescription(IWorldNpcData worldData) {
        return description;
    }

    private MutableComponent getDescriptionWithCount(int count, RegistryAccess registryAccess) {
        return net.minecraft.network.chat.Component.translatable("mknpc.objective.quest_loot_type_tag.desc", itemDescription, tagDesc,
                MKAbility.INTEGER_FORMATTER.format(count), MKAbility.INTEGER_FORMATTER.format(requiredCount));
    }

    private MutableComponent getProgressMessage(LivingEntity entity, int count) {
        return net.minecraft.network.chat.Component.translatable("mknpc.objective.quest_loot_type_tag.progress", itemDescription, entity.getName(),
                MKAbility.INTEGER_FORMATTER.format(count), MKAbility.INTEGER_FORMATTER.format(requiredCount));
    }

    @Override
    public boolean onPlayerKillNpcDefEntity(Player player, PlayerQuestObjectiveData objectiveData, NpcDefinition def,
            LivingDeathEvent event, QuestData quest, PlayerQuestChainInstance playerChain) {
        if (!isComplete(objectiveData)) {
            boolean applies = event.getEntity().getType().is(tag);
            if (applies && player.getRandom().nextDouble() <= chanceToFind) {
                int currentCount = objectiveData.getInt("lootCount");
                currentCount++;
                objectiveData.putInt("lootCount", currentCount);
                objectiveData.setDescription(getDescriptionWithCount(currentCount, player.registryAccess()));
                player.sendSystemMessage(getProgressMessage(event.getEntity(), currentCount)
                        .withStyle(ChatFormatting.GOLD));
                if (currentCount == requiredCount) {
                    signalCompleted(objectiveData);
                }
                playerChain.notifyDirty();
                return true;
            }
        }
        return false;
    }


    @Override
    public PlayerQuestObjectiveData generatePlayerData(IWorldNpcData worldData, QuestData questData) {
        PlayerQuestObjectiveData newObj = new PlayerQuestObjectiveData(getObjectiveName(), getDescription(worldData));
        newObj.putInt("lootCount", 0);
        newObj.setDescription(getDescriptionWithCount(0, worldData.getWorld().registryAccess()));
        return newObj;
    }
}
