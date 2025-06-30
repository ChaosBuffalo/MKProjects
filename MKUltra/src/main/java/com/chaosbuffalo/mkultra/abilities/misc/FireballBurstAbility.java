package com.chaosbuffalo.mkultra.abilities.misc;


import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.projectiles.BurstProjectileBehavior;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.utils.location.CircularLocationProvider;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class FireballBurstAbility extends FireballAbility {
    public FireballBurstAbility() {
        super();
        solveBallisticsForNpc.setDefaultValue(BallisticsSolveMode.PITCH);
        castBehavior.setDefaultValue(new BurstProjectileBehavior(new CircularLocationProvider(
                new Vec3(0.0, 0.0, 0.0), 1.2f, 4, 1.0f,
                0f, 0f, true), true));
        setCastTime(GameConstants.TICKS_PER_SECOND * 4);

    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        return MKUAbilities.FIREBALL.get().getAbilityDescription(entityData, context);
    }

    @Override
    public ResourceLocation getAbilityIcon() {
        return MKUAbilities.FIREBALL.get().getAbilityIcon();
    }

    @Override
    public MutableComponent getAbilityName() {
        return MKUAbilities.FIREBALL.get().getAbilityName();
    }
}
