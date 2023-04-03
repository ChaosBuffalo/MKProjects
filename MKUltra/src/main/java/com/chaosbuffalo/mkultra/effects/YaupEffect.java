package com.chaosbuffalo.mkultra.effects;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.MKSimplePassiveState;
import com.chaosbuffalo.mkultra.init.MKUEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;


public class YaupEffect extends MKEffect {
    private static final UUID hasteUUID = UUID.fromString("ffa941c5-ee35-48f3-a30b-56ca67af695f");
    private static final UUID dmgUUID = UUID.fromString("19e97391-a2cc-4883-9310-82784f642b9f");


    public static MKEffectBuilder<?> from(LivingEntity source, float skillLevel, int duration) {
        return MKUEffects.YAUP.get().builder(source).skillLevel(skillLevel).timed(duration);
    }

    public YaupEffect() {
        super(MobEffectCategory.BENEFICIAL);
        addAttribute(Attributes.ATTACK_SPEED, hasteUUID, 0.1, 0.02, AttributeModifier.Operation.MULTIPLY_TOTAL, MKAttributes.ARETE);
        addAttribute(Attributes.ATTACK_DAMAGE, dmgUUID, 0.2, 0.01, AttributeModifier.Operation.MULTIPLY_TOTAL, MKAttributes.ARETE);
    }


    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}
