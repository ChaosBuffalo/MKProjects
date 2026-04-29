package com.chaosbuffalo.mkultra.abilities.passives;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKPassiveAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContext;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.function.Function;

public class SoulDrainAbility extends MKPassiveAbility {
    private static final FormulaParameterKey BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("soul_drain.base"));
    private static final FormulaParameterKey PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("soul_drain.per_level"));
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(BASE_PARAMETER, 4.0f)
                    .with(PER_LEVEL_PARAMETER, 4.0f)
                    .build());
    protected final FormulaAttribute drainFormula = new FormulaAttribute("drainFormula", AbilityFormula.skilledLinear(BASE_PARAMETER, PER_LEVEL_PARAMETER));

    public SoulDrainAbility() {
        super();
        addSkillAttribute(MKAttributes.EVOCATION);
        addAttributes(formulaParameters, drainFormula);
    }

    @Override
    public MKEffect getPassiveEffect() {
        return MKUEffects.SOUL_DRAIN.get();
    }

    public float getDrainValue(Function<Holder<Attribute>, Float> skillSupplier) {
        float skillLevel = skillSupplier.apply(MKAttributes.EVOCATION);
        FormulaContext context = FormulaContext.builder()
                .withSkillLevel(skillLevel)
                .build();
        return drainFormula.value().bindParametersStrict(formulaParameters.value()).evaluate(context);
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float skillLevel = context.getSkill(MKAttributes.EVOCATION);
        Component valueStr = formatManaValue(entityData, drainFormula.value(), formulaParameters.value(), skillLevel);
        return Component.translatable(getDescriptionTranslationKey(), valueStr);
    }
}
