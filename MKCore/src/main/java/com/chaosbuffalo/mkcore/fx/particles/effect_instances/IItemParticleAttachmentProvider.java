package com.chaosbuffalo.mkcore.fx.particles.effect_instances;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface IItemParticleAttachmentProvider {
    @Nullable
    ItemParticleAttachmentProfile getParticleAttachmentProfile(ItemStack stack);
}
