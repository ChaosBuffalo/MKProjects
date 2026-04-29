package com.chaosbuffalo.mkultra.abilities.misc;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.ProjectileAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.instant.MKAbilityDamageEffect;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.BonusFormulaSpec;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameterKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import com.chaosbuffalo.mkcore.serialization.attributes.BonusFormulaSpecAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FormulaParameterMapAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.effects.HolyWordEffect;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import com.chaosbuffalo.mkultra.init.MKUItems;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class HolyWordAbility extends ProjectileAbility {
    private static final FormulaParameterKey DAMAGE_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("holy_word.damage.base"));
    private static final FormulaParameterKey DAMAGE_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("holy_word.damage.per_level"));
    private static final FormulaParameterKey DAMAGE_MODIFIER_SCALING_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("holy_word.damage.modifier_scaling"));
    private static final FormulaParameterKey DURATION_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("holy_word.duration.base"));
    private static final FormulaParameterKey DURATION_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("holy_word.duration.per_level"));
    private static final FormulaParameterKey STUN_DURATION_BASE_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("holy_word.stun_duration.base"));
    private static final FormulaParameterKey STUN_DURATION_PER_LEVEL_PARAMETER =
            FormulaParameterKey.of(MKUltra.id("holy_word.stun_duration.per_level"));

    public static final ResourceLocation CASTING_PARTICLES = MKUltra.id("holy_word_casting");
    public static final ResourceLocation TRAIL_PARTICLES = MKUltra.id("holy_word_trail");
    public static final ResourceLocation DETONATE_PARTICLES = MKUltra.id("holy_word_detonate");
    protected final FormulaParameterMapAttribute formulaParameters = new FormulaParameterMapAttribute("formulaParameters",
            FormulaParameters.builder()
                    .with(DAMAGE_BASE_PARAMETER, 5.0f)
                    .with(DAMAGE_PER_LEVEL_PARAMETER, 3.0f)
                    .with(DAMAGE_MODIFIER_SCALING_PARAMETER, 1.0f)
                    .with(DURATION_BASE_PARAMETER, 30.0f)
                    .with(DURATION_PER_LEVEL_PARAMETER, 10.0f)
                    .with(STUN_DURATION_BASE_PARAMETER, 3.0f)
                    .with(STUN_DURATION_PER_LEVEL_PARAMETER, 1.0f)
                    .build());
    protected final BonusFormulaSpecAttribute damage = new BonusFormulaSpecAttribute("damage",
            BonusFormulaSpec.skilledBonusScaled(DAMAGE_BASE_PARAMETER, DAMAGE_PER_LEVEL_PARAMETER,
                    FormulaContextKey.DAMAGE_BONUS, DAMAGE_MODIFIER_SCALING_PARAMETER));
    protected final FormulaAttribute durationFormula = new FormulaAttribute("durationFormula", AbilityFormula.skilledLinear(DURATION_BASE_PARAMETER, DURATION_PER_LEVEL_PARAMETER));
    protected final FormulaAttribute stunDurationFormula = new FormulaAttribute("stunDurationFormula", AbilityFormula.skilledLinear(STUN_DURATION_BASE_PARAMETER, STUN_DURATION_PER_LEVEL_PARAMETER));
    protected final IntAttribute stacks = new IntAttribute("stacks", 5);

    public HolyWordAbility() {
        super(MKAttributes.EVOCATION, false);
        castingParticles.setDefaultValue(CASTING_PARTICLES);
        trailParticles.setDefaultValue(TRAIL_PARTICLES);
        addAttributes(formulaParameters, damage, durationFormula, stunDurationFormula, stacks);
        detonateParticles.setDefaultValue(DETONATE_PARTICLES);
        projectileSpeed.setDefaultValue(0.8f);
        setCastTime(GameConstants.TICKS_PER_SECOND + GameConstants.TICKS_PER_SECOND / 4);
        setCooldownTicks(GameConstants.TICKS_PER_SECOND * 5);
        projectileInaccuracy.setDefaultValue(0.0f);
    }

    @Override
    public boolean onImpact(AbilityProjectileEntity projectile, LivingEntity caster, HitResult result, int amplifier) {
        SoundSource cat = caster instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
        SoundUtils.serverPlaySoundAtEntity(projectile, MKUSounds.spell_holy_3.value(), cat);
        MKParticles.spawnOffset(projectile, new Vec3(0.0, 0.0, 0.0), detonateParticles.getValue());
        if (result.getType().equals(HitResult.Type.ENTITY)) {
            EntityHitResult entityTrace = (EntityHitResult) result;

            MKCore.getEntityData(caster).ifPresent(casterData -> {
                float skillLevel = getSkillLevel(caster, skill);
                MKEffectBuilder<?> damageEffect = MKAbilityDamageEffect.from(caster, CoreDamageTypes.HolyDamage.get(),
                                damage.value(),
                                formulaParameters.value())
                        .ability(this)
                        .directEntity(projectile)
                        .skillLevel(skillLevel)
                        .amplify(amplifier);

                MKEffectBuilder<?> stunCounter = HolyWordEffect.from(caster,
                                stunDurationFormula.value(),
                                formulaParameters.value(),
                                stacks.value())
                        .ability(this)
                        .directEntity(projectile)
                        .skillLevel(skillLevel)
                        .amplify(amplifier)
                        .timed(getBuffDuration(casterData, durationFormula.value(), formulaParameters.value(), skillLevel));


                MKCore.getEntityData(entityTrace.getEntity()).ifPresent(x -> {
                    x.getEffects().addEffect(damageEffect);
                    x.getEffects().addEffect(stunCounter);
                });
            });
        }
        return true;
    }

    @Nullable
    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.hostile_casting_holy.value();
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_holy_2.value();
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        float level = context.getSkill(skill);
        Component dmg = getDamageDescription(entityData,
                CoreDamageTypes.HolyDamage.get(), damage.value(), formulaParameters.value(), level);
        float duration = convertDurationToSeconds(getBuffDuration(entityData, durationFormula.value(),
                formulaParameters.value(), level));
        return Component.translatable(getDescriptionTranslationKey(),
                dmg,
                MKUEffects.HOLY_WORD_EFFECT.get().getDisplayName(),
                NUMBER_FORMATTER.format(duration),
                stacks.value(),
                NUMBER_FORMATTER.format(convertDurationToSeconds(
                        getBuffDuration(entityData, stunDurationFormula.value(), formulaParameters.value(), level)))
                );
    }

    @Override
    public AbilityProjectileEntity makeProjectile(IMKEntityData data, AbilityContext context) {
        AbilityProjectileEntity projectile = new AbilityProjectileEntity(CoreEntities.ABILITY_PROJECTILE_TYPE.get(), data.getEntity().level());
        projectile.setAbility(() -> this);
        projectile.setTrailAnimation(trailParticles.getValue());
        projectile.setItem(new ItemStack(MKUItems.holyWordProjectileItem.get()));
        projectile.setDeathTime(GameConstants.TICKS_PER_SECOND * 3);
        return projectile;
    }
}
