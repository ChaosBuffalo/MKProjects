package com.chaosbuffalo.mknpc.quest.dialogue.conditions;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueConditionType;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mknpc.dialogue.NpcDialogueConditionTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class HasEntitlementCondition extends DialogueCondition {
    public static final MapCodec<HasEntitlementCondition> MAP_CODEC = RecordCodecBuilder.<HasEntitlementCondition>mapCodec(builder -> builder.group(
            MKEntitlement.REFERENCE_CODEC.fieldOf("entitlement").forGetter(i -> i.entitlement)
    ).apply(builder, HasEntitlementCondition::new));

    private final Holder<MKEntitlement> entitlement;

    public HasEntitlementCondition(Holder<MKEntitlement> entitlement) {
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
        return MKCore.getPlayer(serverPlayerEntity).map(x -> x.getEntitlements().hasEntitlement(entitlement.value())).orElse(false);
    }

    @Override
    public HasEntitlementCondition copy() {
        return new HasEntitlementCondition(entitlement);
    }
}
