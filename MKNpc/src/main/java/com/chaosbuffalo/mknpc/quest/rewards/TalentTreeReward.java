package com.chaosbuffalo.mknpc.quest.rewards;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.talents.PlayerTalentKnowledge;
import com.chaosbuffalo.mkcore.core.talents.TalentManager;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.chaosbuffalo.mknpc.MKNpc;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class TalentTreeReward extends QuestReward {
    public static final Codec<TalentTreeReward> CODEC = RecordCodecBuilder.<TalentTreeReward>mapCodec(builder ->
            builder.group(
                    ResourceLocation.CODEC.fieldOf("tree_name").forGetter(i -> i.tree)
            ).apply(builder, TalentTreeReward::new)
    ).codec();

    private final ResourceLocation tree;

    public TalentTreeReward(ResourceLocation treeName) {
        this.tree = treeName;
    }

    @Override
    public QuestRewardType<? extends QuestReward> getType() {
        return QuestRewardTypes.TALENT_TREE_REWARD.get();
    }

    @Override
    public Component getDescription() {
        TalentTreeDefinition def = MKCore.getTalentManager().getTalentTree(tree);
        if (def != null) {
            return Component.translatable("mknpc.quest_reward.talent_tree_grant.message", def.getName());
        } else {
            return Component.translatable("mknpc.quest_reward.talent_tree_grant.message.error");
        }
    }

    @Override
    public void grantReward(Player player) {
        if (tree.equals(TalentManager.INVALID_TREE)) {
            MKNpc.LOGGER.warn("Failed to grant talent tree reward for player {}, talent tree is invalid.", player);
            return;
        }
        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getTalents();
            if (talentKnowledge.knowsTree(tree)) {
                return;
            }
            if (talentKnowledge.unlockTree(tree)) {
                ChatUtils.sendMessage(player, Component.translatable("mknpc.quest_reward.talent_tree_grant",
                        talentKnowledge.getTree(tree).getTreeDefinition().getName()).withStyle(ChatFormatting.GOLD));
            }
        });
    }
}
