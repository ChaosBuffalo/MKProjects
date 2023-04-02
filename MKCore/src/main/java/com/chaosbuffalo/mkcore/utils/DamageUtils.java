package com.chaosbuffalo.mkcore.utils;

import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import net.minecraft.world.damagesource.DamageSource;

public class DamageUtils {

    public static boolean isMKDamage(DamageSource source) {
        return source instanceof MKDamageSource;
    }

    public static boolean isMinecraftPhysicalDamage(DamageSource source) {
        return !source.isFire() &&
                !source.isExplosion() &&
                !source.isMagic() &&
                (source.getMsgId().equals("player") || source.getMsgId().equals("mob"));
    }

    public static boolean isMeleeDamage(DamageSource source) {
        return isMinecraftPhysicalDamage(source) ||
                (source instanceof MKDamageSource && ((MKDamageSource) source).isMeleeDamage());
    }

    public static boolean isSpellDamage(DamageSource source) {
        return source instanceof MKDamageSource && !((MKDamageSource) source).isMeleeDamage();
    }

    public static boolean isProjectileDamage(DamageSource source) {
        return source.isProjectile();
    }

    public static boolean isNonMKProjectileDamage(DamageSource source) {
        return isProjectileDamage(source) && !isMKDamage(source);
    }
}
