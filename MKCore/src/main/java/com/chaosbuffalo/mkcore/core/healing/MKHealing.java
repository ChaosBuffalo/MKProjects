package com.chaosbuffalo.mkcore.core.healing;

import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKCombatFormulas;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;

public class MKHealing {

    public static void healEntityFrom(LivingEntity target, float amount, MKHealSource healSource) {
        float finalValue = MKCore.getEntityData(healSource.getSourceEntity())
                .map(casterData -> MKCombatFormulas.applyHealBonus(casterData, amount, healSource.getModifierScaling()))
                .orElse(amount);

        MKAbilityHealEvent event = new MKAbilityHealEvent(target, finalValue, healSource);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            if (wouldHealHurtUndead(healSource.getSourceEntity(), target) && healSource.doesDamageUndead()) {
                float healDamageMultiplier = MKConfig.SERVER.undeadHealDamageMultiplier.get().floatValue();
                target.hurt(convertHealingToDamage(healSource), healDamageMultiplier * event.getAmount());
            } else {
                float afterEfficiency = MKCore.getEntityData(target).map(targetData ->
                        MKCombatFormulas.applyHealEfficiency(targetData, event.getAmount())).orElse(event.getAmount());
                target.heal(afterEfficiency);
            }
        }
    }

    private static MKDamageSource convertHealingToDamage(MKHealSource healSource) {
        return MKDamageSource.causeAbilityDamage(healSource.getDamageType(), healSource.getAbilityId(),
                healSource.getDirectEntity(), healSource.getSourceEntity());
    }

    public static boolean wouldHealHurtUndead(@Nullable LivingEntity caster, LivingEntity target) {
        if (caster != null && caster.isInvertedHealAndHarm()) {
            return false;
        }
        return wouldHealHurtUndead(target);
    }

    public static boolean wouldHealHurtUndead(LivingEntity target) {
        return (target.isInvertedHealAndHarm() && MKConfig.SERVER.healsDamageUndead.get());
    }
}
