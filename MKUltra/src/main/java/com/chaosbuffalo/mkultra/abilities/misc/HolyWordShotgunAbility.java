package com.chaosbuffalo.mkultra.abilities.misc;

import com.chaosbuffalo.mkcore.abilities.projectiles.BurstProjectileBehavior;
import com.chaosbuffalo.mkcore.utils.location.CircularLocationProvider;
import net.minecraft.world.phys.Vec3;

public class HolyWordShotgunAbility extends HolyWordAbility{

    public HolyWordShotgunAbility() {
        solveBallisticsForNpc.setDefaultValue(BallisticsSolveMode.PITCH);
        castBehavior.setDefaultValue(new BurstProjectileBehavior(new CircularLocationProvider(
                new Vec3(0.0, 0.0, 0.0), 1.2f, 4, 1.0f,
                20f, -20f, true), true));

    }
}
