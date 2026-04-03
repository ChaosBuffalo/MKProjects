package com.chaosbuffalo.mknpc.quest.rewards;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.talents.PlayerTalentKnowledge;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import java.util.Objects;

public class TalentTreeReward extends QuestReward {
    public static final MapCodec<TalentTreeReward> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            TalentTreeDefinition.REFERENCE_CODEC.fieldOf("tree_name").forGetter(i -> i.treeHolder)
    ).apply(builder, TalentTreeReward::new));

    private final Holder<TalentTreeDefinition> treeHolder;

    public TalentTreeReward(Holder<TalentTreeDefinition> definitionHolder) {
        this.treeHolder = definitionHolder;
    }

    @Override
    public QuestRewardType<? extends QuestReward> getType() {
        return QuestRewardTypes.TALENT_TREE_REWARD.get();
    }

    @Override
    public Component getDescription() {
        return Component.translatable("mknpc.quest_reward.talent_tree_grant.message", treeHolder.value().getName());
    }

    @Override
    public void grantReward(QuestRewardContext context) {
        var treeKey = treeHolder.getKey();
        Objects.requireNonNull(treeKey);

        var playerData = MKCore.getPlayerOrThrow(context.player());
        PlayerTalentKnowledge talentKnowledge = playerData.getTalents();
        if (talentKnowledge.knowsTree(treeKey)) {
            return;
        }
        if (talentKnowledge.unlockTree(treeKey)) {
            ChatUtils.sendMessage(context.player(), Component.translatable("mknpc.quest_reward.talent_tree_grant",
                    treeHolder.value().getName()).withStyle(ChatFormatting.GOLD));
        }
    }
}
