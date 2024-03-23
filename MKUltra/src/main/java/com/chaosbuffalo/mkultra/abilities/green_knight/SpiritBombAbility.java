package com.chaosbuffalo.mkultra.abilities.green_knight;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.AbilityTargetSelector;
import com.chaosbuffalo.mkcore.abilities.AbilityTargeting;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.entities.projectiles.SpiritBombProjectileEntity;
import com.chaosbuffalo.mkultra.init.MKUEntities;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

import javax.annotation.Nullable;
import java.util.function.Function;

public class SpiritBombAbility extends MKAbility {
    public static final ResourceLocation CASTING_PARTICLES = new ResourceLocation(MKUltra.MODID, "spirit_bomb_casting");
    protected final FloatAttribute baseDamage = new FloatAttribute("baseDamage", 4.0f);
    protected final FloatAttribute scaleDamage = new FloatAttribute("scaleDamage", 4.0f);
    protected final FloatAttribute projectileSpeed = new FloatAttribute("projectileSpeed", 1.25f);
    protected final FloatAttribute projectileInaccuracy = new FloatAttribute("projectileInaccuracy", 0.2f);
    protected final FloatAttribute modifierScaling = new FloatAttribute("modifierScaling", 1.0f);

    public SpiritBombAbility() {
        super();
        setCooldownSeconds(10);
        setCastTime(GameConstants.TICKS_PER_SECOND + (GameConstants.TICKS_PER_SECOND / 4));
        setManaCost(4);
        addAttributes(baseDamage, scaleDamage, projectileSpeed, projectileInaccuracy, modifierScaling);
        addSkillAttribute(MKAttributes.EVOCATION);
        casting_particles.setDefaultValue(CASTING_PARTICLES);
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 50.0f;
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.PROJECTILE;
    }

    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.casting_holy.get();
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return MKUSounds.spell_magic_whoosh_1.get();
    }

    public float getBaseDamage() {
        return baseDamage.value();
    }

    public float getScaleDamage() {
        return scaleDamage.value();
    }

    public float getModifierScaling() {
        return modifierScaling.value();
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        Component damageStr = getDamageDescription(entityData, CoreDamageTypes.NatureDamage.get(),
                baseDamage.value(),
                scaleDamage.value(),
                context.getSkill(MKAttributes.EVOCATION),
                modifierScaling.value());
        return Component.translatable(getDescriptionTranslationKey(), damageStr);
    }

    @Override
    public void endCast(LivingEntity entity, IMKEntityData data, AbilityContext context) {
        super.endCast(entity, data, context);
        float level = context.getSkill(MKAttributes.EVOCATION);
        SpiritBombProjectileEntity proj = new SpiritBombProjectileEntity(MKUEntities.SPIRIT_BOMB_TYPE.get(), entity.level);
        proj.setOwner(entity);
        proj.setSkillLevel(level);
        shootProjectile(proj, projectileSpeed.value(), projectileInaccuracy.value(), entity, context);
        entity.level.addFreshEntity(proj);
    }
}
