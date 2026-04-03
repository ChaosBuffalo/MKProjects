package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
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
        if (startTrigger(targetData, TAG))
            return;
        entityHurtLivingPreTriggers.forEach(f -> f.apply(event, source, targetData));
        targetData.getTriggers().dispatch(CoreTriggerTypes.VICTIM_PRE_SCALE,
                new VictimDamageTriggerContext(event, source, targetData));

        if (source instanceof MKDamageSource mkDamageSource) {
            // we check unblockable here because if it is blockable than the armor calculation will already be applied
            // by vanilla mc, we don't want to apply armor reduction twice
            if (mkDamageSource.is(DamageTypeTags.BYPASSES_ARMOR)) {
                event.setNewDamage(mkDamageSource.getMKDamageType().applyResistance(targetData.getEntity(), event.getNewDamage(), source));
            }
        }

        entityHurtLivingPostTriggers.forEach(f -> f.apply(event, source, targetData));
        targetData.getTriggers().dispatch(CoreTriggerTypes.VICTIM_POST_SCALE,
                new VictimDamageTriggerContext(event, source, targetData));
        endTrigger(targetData, TAG);
    }
}
