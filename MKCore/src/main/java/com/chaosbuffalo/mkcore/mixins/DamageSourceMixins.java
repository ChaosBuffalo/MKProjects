package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.core.damage.IMKDamageSourceExtensions;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
public class DamageSourceMixins implements IMKDamageSourceExtensions {
    @Unique
    private boolean mkcore$wasBlocked = false;

    @Override
    public boolean wasBlocked() {
        return mkcore$wasBlocked;
    }

    @Override
    public void setWasBlocked(boolean value) {
        mkcore$wasBlocked = value;
    }
}
