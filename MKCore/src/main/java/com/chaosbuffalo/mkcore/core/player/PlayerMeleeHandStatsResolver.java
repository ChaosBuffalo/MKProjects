package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.combat.MeleeHandStatsResolver;
import net.minecraft.world.InteractionHand;

public final class PlayerMeleeHandStatsResolver {
    private PlayerMeleeHandStatsResolver() {
    }

    public static int getRequiredAttackStrengthTicks(PlayerCombatExtensionModule combat, InteractionHand hand) {
        return MeleeHandStatsResolver.getRequiredAttackStrengthTicks(combat.getPlayerData().getEntity(), hand);
    }

    public static float resolveAttackDamage(PlayerCombatExtensionModule combat, InteractionHand hand) {
        return MeleeHandStatsResolver.resolveAttackDamage(combat.getPlayerData(), hand);
    }

    public static float resolveAttackKnockback(PlayerCombatExtensionModule combat, InteractionHand hand) {
        return MeleeHandStatsResolver.resolveAttackKnockback(combat.getPlayerData().getEntity(), hand);
    }

    public static float resolveAttackSpeedDelay(PlayerCombatExtensionModule combat, InteractionHand hand) {
        return MeleeHandStatsResolver.resolveAttackSpeedDelay(combat.getPlayerData().getEntity(), hand);
    }
}
