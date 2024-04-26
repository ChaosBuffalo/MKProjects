package com.chaosbuffalo.mknpc.quest.dialogue.effects;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffect;
import com.chaosbuffalo.mkchat.dialogue.effects.DialogueEffectType;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mknpc.dialogue.effects.NpcDialogueEffectTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class GrantEntitlementEffect extends DialogueEffect {
    public static final Codec<GrantEntitlementEffect> CODEC = ExtraCodecs.lazyInitializedCodec(() ->
            RecordCodecBuilder.<GrantEntitlementEffect>mapCodec(builder ->
                    builder.group(
                            MKCoreRegistry.ENTITLEMENTS.getCodec().fieldOf("entitlement").forGetter(i -> i.entitlement)
                    ).apply(builder, GrantEntitlementEffect::new)
            ).codec());


    private final MKEntitlement entitlement;

    public GrantEntitlementEffect(MKEntitlement entitlement) {
        this.entitlement = entitlement;
    }

    @Override
    public DialogueEffectType<?> getType() {
        return NpcDialogueEffectTypes.GRANT_ENTITLEMENT.get();
    }

    @Override
    public GrantEntitlementEffect copy() {
        // No runtime mutable state
        return this;
    }

    @Override
    public void applyEffect(ServerPlayer player, LivingEntity livingEntity, DialogueNode dialogueNode) {
        if (entitlement != null) {
            MKCore.getPlayer(player).ifPresent(x -> x.getEntitlements()
                    .addEntitlement(new EntitlementInstance(entitlement, UUID.randomUUID())));
            player.sendSystemMessage(Component.translatable("mknpc.grant_entitlement.message",
                    entitlement.getDescription()).withStyle(ChatFormatting.GOLD));
        }
    }
}
