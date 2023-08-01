package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;

public class LivingEquipmentChangeTriggers extends SpellTriggers.EffectBasedTriggerCollection<LivingEquipmentChangeTriggers.EquipmentChangeTrigger> {
    @FunctionalInterface
    public interface EquipmentChangeTrigger {
        void apply(LivingEquipmentChangeEvent event, IMKEntityData data, LivingEntity player);
    }

    private static final String TAG = "LIVING_EQUIPMENT_CHANGE";

    public void onEquipmentChange(LivingEquipmentChangeEvent event, IMKEntityData data) {
        runTrigger(data, TAG, (trigger, instance) -> trigger.apply(event, data, data.getEntity()));
    }
}
