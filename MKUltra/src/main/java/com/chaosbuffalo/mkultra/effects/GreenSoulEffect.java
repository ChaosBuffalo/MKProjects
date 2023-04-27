package com.chaosbuffalo.mkultra.effects;


import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectState;
import com.chaosbuffalo.mkcore.effects.MKSimplePassiveState;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class GreenSoulEffect extends MKEffect {
    public final UUID healthId = UUID.fromString("bd8128b8-61e0-4633-8201-f55209788d61");
    public final UUID armorId = UUID.fromString("bbb26b0a-f8d1-4d25-a65c-68804f2252df");

    public GreenSoulEffect() {
        super(MobEffectCategory.BENEFICIAL);
        addAttribute(Attributes.MAX_HEALTH, healthId, 40, AttributeModifier.Operation.ADDITION);
        addAttribute(Attributes.ARMOR, armorId, 4, 2, AttributeModifier.Operation.ADDITION, MKAttributes.RESTORATION);
    }

    @Override
    public MKEffectState makeState() {
        return MKSimplePassiveState.INSTANCE;
    }
}
