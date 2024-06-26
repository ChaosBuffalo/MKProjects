package com.chaosbuffalo.mkultra.abilities.passives;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKPassiveAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.function.Function;

public class SoulDrainAbility extends MKPassiveAbility {
    protected final FloatAttribute base = new FloatAttribute("base", 4.0f);
    protected final FloatAttribute scale = new FloatAttribute("scale", 4.0f);

    public SoulDrainAbility() {
        super();
        addSkillAttribute(MKAttributes.EVOCATION);
        addAttributes(base, scale);
    }

    @Override
    public MKEffect getPassiveEffect() {
        return MKUEffects.SOUL_DRAIN.get();
    }

    public float getDrainValue(Function<Attribute, Float> skillSupplier) {
        float skillLevel = skillSupplier.apply(MKAttributes.EVOCATION);
        return base.value() + scale.value() * skillLevel;
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float value = getDrainValue(context::getSkill);
        return Component.translatable(getDescriptionTranslationKey(), value);
    }
}
