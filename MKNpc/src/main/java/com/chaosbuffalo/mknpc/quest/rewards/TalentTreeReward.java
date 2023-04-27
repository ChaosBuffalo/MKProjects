package com.chaosbuffalo.mknpc.quest.rewards;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.talents.PlayerTalentKnowledge;
import com.chaosbuffalo.mkcore.core.talents.TalentManager;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
import com.chaosbuffalo.mkcore.serialization.attributes.ResourceLocationAttribute;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.chaosbuffalo.mknpc.MKNpc;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class TalentTreeReward extends QuestReward{
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKNpc.MODID, "quest_reward.talent_tree");
    private final ResourceLocationAttribute treeName = new ResourceLocationAttribute("tree_name",
            TalentManager.INVALID_TREE);


    public TalentTreeReward(ResourceLocation treeName) {
        this();
        this.treeName.setValue(treeName);
    }

    public TalentTreeReward() {
        super(TYPE_NAME, defaultDescription);
        addAttribute(treeName);
    }

    @Override
    protected boolean hasPersistentDescription() {
        return false;
    }

    @Override
    public MutableComponent getDescription() {
        TalentTreeDefinition def = MKCore.getTalentManager().getTalentTree(treeName.getValue());
        if (def != null) {
            return Component.translatable("mknpc.quest_reward.talent_tree_grant.message", def.getName());
        } else {
            return Component.translatable("mknpc.quest_reward.talent_tree_grant.message.error");
        }

    }

    @Override
    public void grantReward(Player player) {
        if (treeName.getValue().equals(TalentManager.INVALID_TREE)){
            MKNpc.LOGGER.warn("Failed to grant talent tree reward for player {}, talent tree is invalid.", player);
            return;
        }
        MKCore.getPlayer(player).ifPresent(cap -> {
            PlayerTalentKnowledge talentKnowledge = cap.getTalents();
            if (talentKnowledge.knowsTree(treeName.getValue())) {
                return;
            }
            if (talentKnowledge.unlockTree(treeName.getValue())) {
                ChatUtils.sendMessage(player, Component.translatable("mknpc.quest_reward.talent_tree_grant",
                        talentKnowledge.getTree(treeName.getValue()).getTreeDefinition().getName()).withStyle(ChatFormatting.GOLD));
            }
        });
    }
}
