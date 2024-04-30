package com.chaosbuffalo.mknpc.quest.dialogue.effects;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffectType;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IPlayerQuestingData;
import com.chaosbuffalo.mknpc.capabilities.IWorldNpcData;
import com.chaosbuffalo.mknpc.content.ContentDB;
import com.chaosbuffalo.mknpc.dialogue.NpcDialogueEffectTypes;
import com.chaosbuffalo.mknpc.quest.Quest;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;
import java.util.UUID;

public class ObjectiveCompleteEffect extends DialogueEffect implements IReceivesChainId {
    public static final Codec<ObjectiveCompleteEffect> CODEC = RecordCodecBuilder.<ObjectiveCompleteEffect>mapCodec(builder ->
            builder.group(
                    UUIDUtil.STRING_CODEC.optionalFieldOf("chainId").forGetter(i -> i.chainId.equals(Util.NIL_UUID) ? Optional.empty() : Optional.of(i.chainId)),
                    Codec.STRING.fieldOf("objectiveName").forGetter(i -> i.objectiveName),
                    Codec.STRING.fieldOf("questName").forGetter(i -> i.questName)
            ).apply(builder, ObjectiveCompleteEffect::new)
    ).codec();

    private UUID chainId;
    private final String objectiveName;
    private final String questName;

    private ObjectiveCompleteEffect(Optional<UUID> chainId, String objectiveName, String questName) {
        this(chainId.orElse(Util.NIL_UUID), objectiveName, questName);
    }

    public ObjectiveCompleteEffect(UUID chainId, String objectiveName, String questName) {
        this.chainId = chainId;
        this.objectiveName = objectiveName;
        this.questName = questName;
    }

    public ObjectiveCompleteEffect(String objectiveName, String questName) {
        this(Util.NIL_UUID, objectiveName, questName);
    }

    @Override
    public DialogueEffectType<?> getType() {
        return NpcDialogueEffectTypes.OBJECTIVE_COMPLETE.get();
    }

    @Override
    public void setChainId(UUID chainId) {
        this.chainId = chainId;
    }

    @Override
    public ObjectiveCompleteEffect copy() {
        return new ObjectiveCompleteEffect(chainId, objectiveName, questName);
    }

    @Override
    public void applyEffect(ServerPlayer player, LivingEntity livingEntity, DialogueNode dialogueNode) {
        QuestChainInstance questChain = ContentDB.getQuestInstance(chainId);
        if (questChain == null) {
            return;
        }

        IPlayerQuestingData questingData = MKNpc.getPlayerQuestData(player).resolve().orElse(null);
        if (questingData == null) {
            return;
        }


        questingData.getQuestChain(chainId).ifPresent(playerChain -> {
            IWorldNpcData questDB = ContentDB.getQuestDB();
            Quest currentQuest = questChain.getDefinition().getQuest(questName);
            if (currentQuest == null) {
                return;
            }
            questChain.signalObjectiveComplete(objectiveName, questDB, questingData, currentQuest, playerChain);
        });
    }
}
