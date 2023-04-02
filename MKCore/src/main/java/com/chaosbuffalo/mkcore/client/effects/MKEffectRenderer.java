package com.chaosbuffalo.mkcore.client.effects;


import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.EffectRenderer;

public class MKEffectRenderer extends EffectRenderer {
    protected final MKEffect effect;


    public MKEffectRenderer(MKEffect effect) {
        this.effect = effect;
    }
    @Override
    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui,
                                      PoseStack mStack, int x, int y, float z) {

    }

    @Override
    public void renderHUDEffect(MobEffectInstance effect, GuiComponent gui,
                                PoseStack mStack, int x, int y, float z, float alpha) {

    }

    @Override
    public boolean shouldRender(MobEffectInstance effect) {
        if (effect instanceof MKActiveEffect.MKMobEffectInstance) {
            return this.effect.shouldRender(((MKActiveEffect.MKMobEffectInstance) effect).getEffectInstance());
        } else {
            return true;
        }

    }

    @Override
    public boolean shouldRenderInvText(MobEffectInstance effect) {
        if (effect instanceof MKActiveEffect.MKMobEffectInstance) {
            return this.effect.shouldRenderInvText(((MKActiveEffect.MKMobEffectInstance) effect).getEffectInstance());
        } else {
            return true;
        }
    }

    @Override
    public boolean shouldRenderHUD(MobEffectInstance effect) {
        if (effect instanceof MKActiveEffect.MKMobEffectInstance) {
            return this.effect.shouldRenderHUD(((MKActiveEffect.MKMobEffectInstance) effect).getEffectInstance());
        } else {
            return true;
        }
    }
}
