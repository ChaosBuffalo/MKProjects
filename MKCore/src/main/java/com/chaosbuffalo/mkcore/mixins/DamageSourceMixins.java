package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.core.damage.IMKDamageSourceExtensions;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
public class DamageSourceMixins implements IMKDamageSourceExtensions {
    @Unique
    private boolean mkcore$canBlock = true;

    @Override
    public boolean canBlock() {
        return mkcore$canBlock;
    }

    @Override
    public void setCanBlock(boolean value) {
        mkcore$canBlock = value;
    }
}
