package com.chaosbuffalo.mknpc.quest.rewards;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import java.util.UUID;

public class GrantEntitlementReward extends QuestReward {
    public static final MapCodec<GrantEntitlementReward> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            MKEntitlement.REFERENCE_CODEC.fieldOf("entitlement").forGetter(i -> i.entitlement)
    ).apply(builder, GrantEntitlementReward::new));

    private final Holder<MKEntitlement> entitlement;

    public GrantEntitlementReward(Holder<MKEntitlement> entitlement) {
        this.entitlement = entitlement;
    }

    @Override
    public QuestRewardType<? extends QuestReward> getType() {
        return QuestRewardTypes.ENTITLEMENT_REWARD.get();
    }

    @Override
    public Component getDescription() {
        return Component.translatable("mknpc.quest_reward.entitlement.message", entitlement.value().getName());
    }

    @Override
    public void grantReward(QuestRewardContext context) {
        if (entitlement != null) {
            MKCore.getPlayer(context.player()).ifPresent(x -> x.getEntitlements()
                    .addEntitlement(new EntitlementInstance(entitlement, UUID.randomUUID())));
            context.player().sendSystemMessage(Component.translatable("mknpc.grant_entitlement.message",
                    entitlement.value().getName()).withStyle(ChatFormatting.GOLD));
        }
    }
}
