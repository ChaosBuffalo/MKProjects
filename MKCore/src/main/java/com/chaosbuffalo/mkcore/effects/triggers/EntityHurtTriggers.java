package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import com.chaosbuffalo.mkcore.utils.DamageUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.ArrayList;
import java.util.List;

public class EntityHurtTriggers extends SpellTriggers.TriggerCollectionBase {
    @FunctionalInterface
    public interface Trigger {
        void apply(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget, IMKEntityData targetData);
    }

    private static final String TAG = "ENTITY_HURT_LIVING";
    private final List<Trigger> entityHurtLivingPreTriggers = new ArrayList<>();
    private final List<Trigger> entityHurtLivingPostTriggers = new ArrayList<>();

    @Override
    public boolean hasTriggers() {
        return entityHurtLivingPreTriggers.size() > 0 || entityHurtLivingPostTriggers.size() > 0;
    }

    public void registerPreScale(Trigger trigger) {
        entityHurtLivingPreTriggers.add(trigger);
    }

    public void registerPostScale(Trigger trigger) {
        entityHurtLivingPostTriggers.add(trigger);
    }

    public void onEntityHurtLiving(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                                   IMKEntityData targetData) {
        if (startTrigger(livingTarget, TAG))
            return;
        entityHurtLivingPreTriggers.forEach(f -> f.apply(event, source, livingTarget, targetData));

        if (DamageUtils.isMKDamage(source)) {
            MKDamageSource mkDamageSource = (MKDamageSource) source;
            // we check unblockable here because if it is blockable than the armor calculation will already be applied
            // by vanilla mc, we don't want to apply armor reduction twice
            if (mkDamageSource.isBypassArmor()) {
                event.setAmount(mkDamageSource.getMKDamageType().applyResistance(livingTarget, event.getAmount()));
            }

        }

        entityHurtLivingPostTriggers.forEach(f -> f.apply(event, source, livingTarget, targetData));
        endTrigger(livingTarget, TAG);
    }
}
