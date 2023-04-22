package com.chaosbuffalo.mkcore.mixins;

import com.chaosbuffalo.mkcore.core.damage.IMKDamageSourceExtensions;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DamageSource.class)
public class DamageSourceMixins implements IMKDamageSourceExtensions {
    private boolean canBlock = true;
    @Override
    public boolean canBlock() {
        return canBlock;
    }

    @Override
    public void setCanBlock(boolean value) {
        canBlock = value;
    }
}
