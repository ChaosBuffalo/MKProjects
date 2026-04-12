package com.chaosbuffalo.mkcore.core.combat;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public final class AbilityMeleeAttackHelper {
    private AbilityMeleeAttackHelper() {
    }

    public static InteractionHand resolveHand(LivingEntity entity, InteractionHand configuredHand) {
        if (configuredHand == InteractionHand.OFF_HAND && entity.getOffhandItem().isEmpty() && !entity.getMainHandItem().isEmpty()) {
            return InteractionHand.MAIN_HAND;
        }
        return configuredHand;
    }

    public static List<InteractionHand> resolveAttackHands(LivingEntity entity) {
        List<InteractionHand> hands = new ArrayList<>();
        if (!entity.getMainHandItem().isEmpty()) {
            hands.add(InteractionHand.MAIN_HAND);
        }
        if (MKMeleeManager.canUseForAttack(entity, InteractionHand.MAIN_HAND) &&
                MKMeleeManager.canUseForAttack(entity, InteractionHand.OFF_HAND) &&
                !entity.getOffhandItem().isEmpty()) {
            hands.add(InteractionHand.OFF_HAND);
        }
        if (hands.isEmpty()) {
            hands.add(InteractionHand.MAIN_HAND);
        }
        return hands;
    }

    public static float computeBonusDamage(IMKEntityData sourceData, float base, float scale, float skillLevel, float modifierScale) {
        float rawBonus = sourceData.getStats().getDamageTypeBonus(CoreDamageTypes.MeleeDamage.get());
        return base + scale * skillLevel + rawBonus * modifierScale;
    }
}
