package com.chaosbuffalo.mkultra.abilities.passives;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKPassiveAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;
import com.chaosbuffalo.mkcore.formulas.FormulaContext;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.serialization.attributes.BonusFormulaSpecAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.function.Function;

public class LifeSiphonAbility extends MKPassiveAbility {
    private static final FormulaParameterKey HEAL_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("life_siphon.heal.base"));
    private static final FormulaParameterKey HEAL_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("life_siphon.heal.per_level"));
    private static final FormulaParameterKey HEAL_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("life_siphon.heal.modifier_scaling"));
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(HEAL_BASE_PARAMETER, 4.0f)
                    .with(HEAL_PER_LEVEL_PARAMETER, 4.0f)
                    .with(HEAL_MODIFIER_SCALING_PARAMETER, 1.0f)
                    .build());
    protected final BonusFormulaSpecAttribute healing = new BonusFormulaSpecAttribute("healing",
            BonusFormulaSpec.skilledBonusScaled(HEAL_BASE_PARAMETER, HEAL_PER_LEVEL_PARAMETER,
                    FormulaContextKey.HEAL_BONUS, HEAL_MODIFIER_SCALING_PARAMETER));

    public LifeSiphonAbility() {
        super();
        addSkillAttribute(MKAttributes.NECROMANCY);
        addAttributes(formulaParameters, healing);
    }

    public BonusFormulaSpec getBoundHealingSpec() {
        return healing.value().bindStrict(formulaParameters.value());
    }

    public float getBaseHealingValue(LivingEntity entity) {
        float necromancyLevel = MKAbility.getSkillLevel(entity, MKAttributes.NECROMANCY);
        return getBoundHealingSpec().baseFormula().evaluate(FormulaContext.builder()
                .withSkillLevel(necromancyLevel)
                .build());
    }

    public AbilityFormula getHealingBonusFormula() {
        return getBoundHealingSpec().bonusFormula();
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(MKAttributes.NECROMANCY);
        Component valueStr = getHealDescription(entityData, healing.value(), formulaParameters.value(), level);
        return Component.translatable(getDescriptionTranslationKey(), valueStr);
    }

    @Override
    public MKEffect getPassiveEffect() {
        return MKUEffects.LIFE_SIPHON.get();
    }

}
