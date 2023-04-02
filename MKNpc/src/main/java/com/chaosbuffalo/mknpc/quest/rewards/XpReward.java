package com.chaosbuffalo.mknpc.quest.rewards;

import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mknpc.MKNpc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

public class XpReward extends QuestReward {
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKNpc.MODID, "quest_reward.xp");
    protected final IntAttribute xpAmount = new IntAttribute("xp", 0);


    public XpReward(int xp) {
        super(TYPE_NAME, defaultDescription);
        addAttribute(xpAmount);
        xpAmount.setValue(xp);
    }

    public XpReward() {
        this(0);
    }

    @Override
    public MutableComponent getDescription() {
        return new TranslatableComponent("mknpc.quest_reward.xp.name", xpAmount.value());
    }

    @Override
    protected boolean hasPersistentDescription() {
        return false;
    }

    @Override
    public void grantReward(Player player) {
        player.giveExperiencePoints(xpAmount.value());
        player.sendMessage(new TranslatableComponent("mknpc.quest_reward.xp.message", xpAmount.value())
                .withStyle(ChatFormatting.GOLD), Util.NIL_UUID);
    }
}
