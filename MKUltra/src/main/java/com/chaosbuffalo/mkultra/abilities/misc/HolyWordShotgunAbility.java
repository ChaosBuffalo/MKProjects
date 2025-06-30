package com.chaosbuffalo.mkultra.abilities.misc;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.projectiles.SimpleProjectileBehavior;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.utils.location.CircularLocationProvider;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class HolyWordShotgunAbility extends HolyWordAbility{

    public HolyWordShotgunAbility() {
        super();
        solveBallisticsForNpc.setDefaultValue(BallisticsSolveMode.PITCH);
        castBehavior.setDefaultValue(new SimpleProjectileBehavior(new CircularLocationProvider(
                new Vec3(0.0, 0.0, 0.0), 1.2f, 4, 1.0f,
                20f, -20f, true), true));

    }

    @Override
    public Component getAbilityDescription(IMKEntityData entityData, AbilityContext context) {
        return MKUAbilities.HOLY_WORD.get().getAbilityDescription(entityData, context);
    }

    @Override
    public ResourceLocation getAbilityIcon() {
        return MKUAbilities.HOLY_WORD.get().getAbilityIcon();
    }

    @Override
    public MutableComponent getAbilityName() {
        return MKUAbilities.HOLY_WORD.get().getAbilityName();
    }
}
