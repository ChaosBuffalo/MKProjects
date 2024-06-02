package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.abilities.description.AbilityDescriptions;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public abstract class MKPassiveAbility extends MKAbility {
    public MKPassiveAbility() {
        super();
    }

    @Override
    public AbilityType getType() {
        return AbilityType.Passive;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.SELF;
    }

    public abstract MKEffect getPassiveEffect();

    @Override
    public void buildDescription(IMKEntityData casterData, AbilityContext context, Consumer<Component> consumer) {
        consumer.accept(Component.translatable("mkcore.ability.description.passive"));
        consumer.accept(getAbilityDescription(casterData, context));
        AbilityDescriptions.getEffectModifiers(getPassiveEffect(), context, false, consumer);
    }

    public void activate(IMKEntityData casterData, MKAbilityInfo abilityInfo) {
        if (!casterData.getEffects().isEffectActive(getPassiveEffect())) {
            MKEffectBuilder<?> effect = getPassiveEffect().builder(casterData.getEntity())
                    .ability(this)
                    .temporary() // Abilities slotted to the passive group are re-executed when joining the world
                    .infinite();
            casterData.getEffects().addEffect(effect);
        }
    }

    public void deactivate(IMKEntityData entityData, MKAbilityInfo abilityInfo) {
        MKEffect passiveEffect = getPassiveEffect();
        if (entityData.getEffects().isEffectActive(passiveEffect)) {
            entityData.getEffects().removeEffect(passiveEffect);
        }
    }
}
