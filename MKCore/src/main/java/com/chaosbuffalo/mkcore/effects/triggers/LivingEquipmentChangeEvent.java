package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.SpellTriggers;
import net.minecraft.world.entity.LivingEntity;

public class LivingEquipmentChangeEvent extends SpellTriggers.EffectBasedTriggerCollection<LivingEquipmentChangeEvent.EquipmentChangeTrigger> {
    @FunctionalInterface
    public interface EquipmentChangeTrigger {
        void apply(net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent event, IMKEntityData data, LivingEntity player);
    }

    private static final String TAG = "LIVING_EQUIPMENT_CHANGE";

    public void onEquipmentChange(net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent event, IMKEntityData data, LivingEntity player) {
        runTrigger(player, TAG, (trigger, instance) -> trigger.apply(event, data, player));
    }
}
