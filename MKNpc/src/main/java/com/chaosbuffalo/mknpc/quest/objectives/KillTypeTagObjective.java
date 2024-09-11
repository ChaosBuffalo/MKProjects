package com.chaosbuffalo.mknpc.quest.objectives;

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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.List;
import java.util.Map;

public class KillTypeTagObjective extends QuestObjective<EmptyInstanceData> implements IKillObjectiveHandler {
    public static final MapCodec<KillTypeTagObjective> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.fieldOf("objectiveName").forGetter(i -> i.objectiveName),
            TagKey.codec(BuiltInRegistries.ENTITY_TYPE.key()).fieldOf("tag").forGetter(i -> i.tag),
            Codec.STRING.fieldOf("tagDesc").forGetter(i -> i.tagDesc),
            Codec.INT.fieldOf("count").forGetter(i -> i.requiredCount)
    ).apply(builder, KillTypeTagObjective::new));

    private final TagKey<EntityType<?>> tag;
    private final String tagDesc;
    private final int requiredCount;

    public KillTypeTagObjective(String name, TagKey<EntityType<?>> tag, String tagDesc, int count) {
        super(name);
        requiredCount = count;
        this.tag = tag;
        this.tagDesc = tagDesc;
    }

    @Override
    public QuestObjectiveType<? extends QuestObjective<?>> getType() {
        return QuestObjectiveTypes.KILL_TYPE_TAG.get();
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
        return List.of(getDescriptionWithKillCount(0));
    }

    private MutableComponent getDescriptionWithKillCount(int count) {
        return Component.translatable("mknpc.objective.kill_type_tag.desc", tagDesc, count, requiredCount);
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
        if (event.getEntity().getType().is(tag) && !isComplete(objectiveData)) {
            int currentCount = objectiveData.getInt("killCount");
            currentCount++;
            objectiveData.putInt("killCount", currentCount);
            objectiveData.setDescription(getDescriptionWithKillCount(currentCount));
            player.sendSystemMessage(getDescriptionWithKillCount(currentCount).withStyle(ChatFormatting.GOLD));
            if (currentCount == requiredCount) {
                signalCompleted(objectiveData);
            }
            playerChain.notifyDirty();
            return true;
        }
        return false;
    }
}

