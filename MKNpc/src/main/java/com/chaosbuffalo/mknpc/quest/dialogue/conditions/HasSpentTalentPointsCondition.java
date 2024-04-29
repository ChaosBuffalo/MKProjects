package com.chaosbuffalo.mknpc.quest.dialogue.conditions;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueConditionType;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mknpc.dialogue.NpcDialogueConditionTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;


public class HasSpentTalentPointsCondition extends DialogueCondition {
    public static final Codec<HasSpentTalentPointsCondition> CODEC = RecordCodecBuilder.<HasSpentTalentPointsCondition>mapCodec(builder ->
            builder.group(
                    Codec.INT.fieldOf("talentCount").forGetter(i -> i.talentCount)
            ).apply(builder, HasSpentTalentPointsCondition::new)
    ).codec();

    private final int talentCount;

    public HasSpentTalentPointsCondition(int talentCount) {
        this.talentCount = talentCount;
    }

    @Override
    public DialogueConditionType<? extends DialogueCondition> getType() {
        return NpcDialogueConditionTypes.HAS_SPENT_TALENTS.get();
    }

    @Override
    public boolean meetsCondition(ServerPlayer player, LivingEntity source) {
        return MKCore.getPlayer(player).map(x -> {
            int unspent = x.getTalents().getUnspentTalentPoints();
            int total = x.getTalents().getTotalTalentPoints();
            int spent = total - unspent;
            return spent >= talentCount;
        }).orElse(false);
    }

    @Override
    public HasSpentTalentPointsCondition copy() {
        return new HasSpentTalentPointsCondition(talentCount);
    }
}
