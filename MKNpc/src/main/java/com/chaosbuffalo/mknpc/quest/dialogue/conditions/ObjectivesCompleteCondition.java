package com.chaosbuffalo.mknpc.quest.dialogue.conditions;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueConditionType;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.dialogue.NpcDialogueConditionTypes;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestChainInstance;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestData;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestObjectiveData;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.IReceivesChainId;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

public class ObjectivesCompleteCondition extends DialogueCondition implements IReceivesChainId {
    public static final Codec<ObjectivesCompleteCondition> CODEC = RecordCodecBuilder.<ObjectivesCompleteCondition>mapCodec(builder ->
            builder.group(
                    Codec.STRING.fieldOf("questName").forGetter(i -> i.questName),
                    Codec.list(Codec.STRING).fieldOf("objectiveNames").forGetter(i -> i.objectiveNames),
                    UUIDUtil.STRING_CODEC.optionalFieldOf("chainId").forGetter(i -> i.chainId.equals(Util.NIL_UUID) ? Optional.empty() : Optional.of(i.chainId))
            ).apply(builder, ObjectivesCompleteCondition::new)
    ).codec();

    private final List<String> objectiveNames = new ArrayList<>();
    private final String questName;
    private UUID chainId;

    private ObjectivesCompleteCondition(String questName, List<String> objectiveNames, Optional<UUID> chainId) {
        this.questName = questName;
        this.objectiveNames.addAll(objectiveNames);
        this.chainId = chainId.orElse(Util.NIL_UUID);
    }

    public ObjectivesCompleteCondition(String questName, List<String> objectiveNames) {
        this.objectiveNames.addAll(objectiveNames);
        this.chainId = Util.NIL_UUID;
        this.questName = questName;
    }

    @Override
    public DialogueConditionType<? extends DialogueCondition> getType() {
        return NpcDialogueConditionTypes.OBJECTIVES_COMPLETE.get();
    }

    @Override
    public boolean meetsCondition(ServerPlayer serverPlayerEntity, LivingEntity livingEntity) {
        return MKNpc.getPlayerQuestData(serverPlayerEntity).map(x -> {
            Optional<PlayerQuestChainInstance> chainInstance = x.getQuestChain(chainId);
            if (chainInstance.isPresent()) {
                PlayerQuestChainInstance chain = chainInstance.get();
                return objectiveNames.stream().allMatch(name -> {
                    PlayerQuestData questData = chain.getQuestData(questName);
                    if (questData == null) {
                        return false;
                    }
                    PlayerQuestObjectiveData pObj = questData.getObjective(name);
                    return pObj != null && pObj.isComplete();
                });
            }
            return false;
        }).orElse(false);
    }

    @Override
    public ObjectivesCompleteCondition copy() {
        return new ObjectivesCompleteCondition(questName, objectiveNames);
    }

    @Override
    public void setChainId(UUID chainId) {
        this.chainId = chainId;
    }
}
