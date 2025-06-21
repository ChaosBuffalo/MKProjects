package com.chaosbuffalo.mkultra.abilities.necromancer;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.projectiles.SimpleProjectileBehavior;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.utils.location.CircularLocationProvider;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class ShadowboltDualShotgunAbility extends ShadowBoltAbility {

    public ShadowboltDualShotgunAbility() {
        super();
        solveBallisticsForNpc.setDefaultValue(BallisticsSolveMode.PITCH);
        castBehavior.setDefaultValue(new SimpleProjectileBehavior(new CircularLocationProvider(
                new Vec3(0.0, 0.0, 0.0), 0.6f, 2, 1.0f,
                1f, -1f, true), true));
    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        return MKUAbilities.SHADOW_BOLT.get().getAbilityDescription(entityData, context);
    }

    @Override
    public MutableComponent getAbilityName() {
        return MKUAbilities.SHADOW_BOLT.get().getAbilityName();
    }

    @Override
    public ResourceLocation getAbilityIcon() {
        return MKUAbilities.SHADOW_BOLT.get().getAbilityIcon();
    }
}
