package com.chaosbuffalo.mknpc.quest.rewards;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class GrantEntitlementReward extends QuestReward {
    public static final Codec<GrantEntitlementReward> CODEC = ExtraCodecs.lazyInitializedCodec(() ->
            RecordCodecBuilder.<GrantEntitlementReward>mapCodec(builder ->
                    builder.group(
                            MKCoreRegistry.ENTITLEMENTS.getCodec().fieldOf("entitlement").forGetter(i -> i.entitlement)
                    ).apply(builder, GrantEntitlementReward::new)
            ).codec());

    private final MKEntitlement entitlement;

    public GrantEntitlementReward(MKEntitlement entitlement) {
        this.entitlement = entitlement;
    }

    @Override
    public QuestRewardType<? extends QuestReward> getType() {
        return QuestRewardTypes.ENTITLEMENT_REWARD.get();
    }

    @Override
    public Component getDescription() {
        return Component.translatable("mknpc.quest_reward.entitlement.message", entitlement.getDescription());
    }

    @Override
    public void grantReward(Player player) {
        if (entitlement != null) {
            MKCore.getPlayer(player).ifPresent(x -> x.getEntitlements()
                    .addEntitlement(new EntitlementInstance(entitlement, UUID.randomUUID())));
            player.sendSystemMessage(Component.translatable("mknpc.grant_entitlement.message",
                    entitlement.getDescription()).withStyle(ChatFormatting.GOLD));
        }
    }
}
