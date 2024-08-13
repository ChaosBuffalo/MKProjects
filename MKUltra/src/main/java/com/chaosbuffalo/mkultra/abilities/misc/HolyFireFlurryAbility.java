package com.chaosbuffalo.mkultra.abilities.misc;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.PositionFlurryAbility;
import com.chaosbuffalo.mkcore.abilities.ai.conditions.StandardUseCondition;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUSounds;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.chaosbuffalo.targeting_api.TargetingContexts;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public class HolyFireFlurryAbility extends PositionFlurryAbility {
    private static final ResourceLocation CASTING_PARTICLES = new ResourceLocation(MKUltra.MODID, "holy_fire_casting");

    public HolyFireFlurryAbility() {
        super(MKUAbilities.HOLY_FIRE);
        setCastTime(GameConstants.TICKS_PER_SECOND / 2);
        setCooldownSeconds(10);
        setManaCost(12);
        addSkillAttribute(MKAttributes.EVOCATION);
        setUseCondition(new StandardUseCondition(this));
        castingParticles.setDefaultValue(CASTING_PARTICLES);
    }

    @Override
    public TargetingContext getTargetContext() {
        return TargetingContexts.ENEMY;
    }

    @Override
    public float getDistance(LivingEntity entity) {
        return 25.0f;
    }

    @Nullable
    @Override
    public SoundEvent getCastingSoundEvent() {
        return MKUSounds.hostile_casting_holy.get();
    }

    @Nullable
    @Override
    public SoundEvent getSpellCompleteSoundEvent() {
        return null;
    }
}
