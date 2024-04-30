package com.chaosbuffalo.mknpc.quest.dialogue.conditions;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueConditionType;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mknpc.dialogue.NpcDialogueConditionTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;

public class HasEntitlementCondition extends DialogueCondition {
    public static final Codec<HasEntitlementCondition> CODEC = ExtraCodecs.lazyInitializedCodec(() ->
            RecordCodecBuilder.<HasEntitlementCondition>mapCodec(builder ->
                    builder.group(
                            MKCoreRegistry.ENTITLEMENTS.getCodec().fieldOf("entitlement").forGetter(i -> i.entitlement)
                    ).apply(builder, HasEntitlementCondition::new)
            ).codec());

    private final MKEntitlement entitlement;

    public HasEntitlementCondition(MKEntitlement entitlement) {
        this.entitlement = entitlement;
    }

    @Override
    public DialogueConditionType<? extends DialogueCondition> getType() {
        return NpcDialogueConditionTypes.HAS_ENTITLEMENT.get();
    }

    @Override
    public boolean meetsCondition(ServerPlayer serverPlayerEntity, LivingEntity livingEntity) {
        if (entitlement == null) {
            return false;
        }
        return MKCore.getPlayer(serverPlayerEntity).map(x -> x.getEntitlements().hasEntitlement(entitlement)).orElse(false);
    }

    @Override
    public HasEntitlementCondition copy() {
        return new HasEntitlementCondition(entitlement);
    }
}
