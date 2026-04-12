package com.chaosbuffalo.mkcore.core.combat;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MKMeleeManager {
    private static final List<DualWieldResolver> RESOLVERS = new CopyOnWriteArrayList<>();

    public static void registerResolver(DualWieldResolver resolver) {
        RESOLVERS.add(resolver);
    }

    public static boolean canUseForAttack(LivingEntity entity, InteractionHand hand) {
        for (DualWieldResolver resolver : RESOLVERS) {
            if (resolver.canUseForAttack(entity, hand)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canUseCustomMelee(LivingEntity entity, InteractionHand hand) {
        for (DualWieldResolver resolver : RESOLVERS) {
            if (resolver.canUseCustomMelee(entity, hand)) {
                return true;
            }
        }
        return false;
    }
}
