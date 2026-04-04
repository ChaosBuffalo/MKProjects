package com.chaosbuffalo.mkcore.utils;

import com.chaosbuffalo.mkcore.core.damage.IMKDamageSourceExtensions;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.init.CoreTags;
import net.minecraft.world.damagesource.DamageSource;

public class DamageUtils {

    public static boolean isMKDamage(DamageSource source) {
        return source instanceof MKDamageSource;
    }

    public static boolean wasAlreadyPartiallyBlocked(DamageSource source) {
        return source instanceof IMKDamageSourceExtensions ext && ext.wasBlocked();
    }

    public static boolean isFullyBlockedDamage(DamageSource source, float damage) {
        // `wasBlocked` marks any successful shield interaction. A hit is fully blocked
        // only when that interaction leaves no damage for LivingDamageEvent.Pre.
        return wasAlreadyPartiallyBlocked(source) && damage <= 0.0f;
    }

    public static boolean isVanillaMeleeDamage(DamageSource source) {
        return source.is(CoreTags.DamageTypes.VANILLA_MELEE_DAMAGE);
    }

    public static boolean isMeleeDamage(DamageSource source) {
        return isVanillaMeleeDamage(source) ||
                (source instanceof MKDamageSource mkDamageSource && mkDamageSource.isMeleeDamage());
    }

    public static boolean isSpellDamage(DamageSource source) {
        return source instanceof MKDamageSource && !((MKDamageSource) source).isMeleeDamage();
    }

    public static boolean isProjectileDamage(DamageSource source) {
        return source.is(CoreTags.DamageTypes.MK_PROJECTILE_DAMAGE);
    }

    public static boolean isVanillaProjectileDamage(DamageSource source) {
        return isProjectileDamage(source) && !isMKDamage(source);
    }
}
