package com.chaosbuffalo.mkultra.abilities.misc;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.projectiles.BurstProjectileBehavior;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.utils.location.CircularLocationProvider;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public class HolyWordBurstAbility extends HolyWordAbility{

    public HolyWordBurstAbility() {
        solveBallisticsForNpc.setDefaultValue(BallisticsSolveMode.PITCH);
        castBehavior.setDefaultValue(new BurstProjectileBehavior(new CircularLocationProvider(
                new Vec3(0.0, 0.0, 0.0), 1.2f, 12, 1.0f,
                60f, -60f, true), true));
        setCastTime(GameConstants.TICKS_PER_SECOND * 4);

    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        return MKUAbilities.HOLY_WORD.get().getAbilityDescription(entityData, context);
    }

}
