package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.combat.damage.MKDamageContext;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.utils.DamageUtils;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.ArrayList;
import java.util.List;

public class EntityHurtTriggers extends SpellTriggers.TriggerCollectionBase {
    @FunctionalInterface
    public interface Trigger {
        void apply(LivingDamageEvent.Pre event, DamageSource source, IMKEntityData victimData);
    }

    private static final String TAG = "ENTITY_HURT_LIVING";
    private final List<Trigger> entityHurtLivingPreTriggers = new ArrayList<>();
    private final List<Trigger> entityHurtLivingPostTriggers = new ArrayList<>();

    @Override
    public boolean hasTriggers() {
        return !entityHurtLivingPreTriggers.isEmpty() || !entityHurtLivingPostTriggers.isEmpty();
    }

    public void registerPreScale(Trigger trigger) {
        entityHurtLivingPreTriggers.add(trigger);
    }

    public void registerPostScale(Trigger trigger) {
        entityHurtLivingPostTriggers.add(trigger);
    }

    public void onEntityHurtLiving(LivingDamageEvent.Pre event, DamageSource source, IMKEntityData targetData) {
        applyResistance(event, source, targetData);
        dispatchTriggers(event, source, targetData);
    }

    public void applyResistance(LivingDamageEvent.Pre event, DamageSource source, IMKEntityData targetData) {
        if (source instanceof MKDamageSource mkDamageSource) {
            // we check unblockable here because if it is blockable than the armor calculation will already be applied
            // by vanilla mc, we don't want to apply armor reduction twice
            if (mkDamageSource.is(DamageTypeTags.BYPASSES_ARMOR)) {
                event.setNewDamage(mkDamageSource.getMKDamageType().applyResistance(targetData.getEntity(), event.getNewDamage(), source));
            }
        }
        if (DamageUtils.isProjectileDamage(source)) {
            event.setNewDamage((float) (event.getNewDamage()
                    * (1.0 - targetData.getEntity().getAttributeValue(MKAttributes.RANGED_RESISTANCE))));
        }
    }

    public void applyResistance(MKDamageContext context) {
        DamageSource source = context.getSource();
        IMKEntityData targetData = context.getTargetData();
        float damage = context.getWorkingDamage();
        if (source instanceof MKDamageSource mkDamageSource) {
            if (mkDamageSource.is(DamageTypeTags.BYPASSES_ARMOR)) {
                damage = mkDamageSource.getMKDamageType().applyResistance(targetData.getEntity(), damage, source);
            }
        }
        if (DamageUtils.isProjectileDamage(source)) {
            damage = (float) (damage * (1.0 - targetData.getEntity().getAttributeValue(MKAttributes.RANGED_RESISTANCE)));
        }
        context.setWorkingDamage(damage, "mkcore:victim_resistance");
    }

    public void dispatchTriggers(LivingDamageEvent.Pre event, DamageSource source, IMKEntityData targetData) {
        if (startTrigger(targetData, TAG))
            return;
        entityHurtLivingPreTriggers.forEach(f -> f.apply(event, source, targetData));
        entityHurtLivingPostTriggers.forEach(f -> f.apply(event, source, targetData));
        endTrigger(targetData, TAG);
    }

    public void dispatchTriggers(MKDamageContext context) {
        context.runLegacyEventMutation("mkcore:victim_triggers", event ->
                dispatchTriggers(event, context.getSource(), context.getTargetData()));
    }
}
