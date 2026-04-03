package com.chaosbuffalo.mkcore.effects.triggers;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

public record FallTriggerContext(LivingFallEvent event, IMKEntityData entityData) {
}
