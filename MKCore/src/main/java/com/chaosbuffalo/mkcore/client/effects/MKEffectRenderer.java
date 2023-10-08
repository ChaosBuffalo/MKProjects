package com.chaosbuffalo.mkcore.client.effects;


import com.chaosbuffalo.mkcore.effects.MKEffect;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

public class MKEffectRenderer implements IClientMobEffectExtensions {
    protected final MKEffect effect;


    public MKEffectRenderer(MKEffect effect) {
        this.effect = effect;
    }

//    @Override
//    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui,
//                                      PoseStack mStack, int x, int y, float z) {
//
//    }
//
//    @Override
//    public void renderHUDEffect(MobEffectInstance effect, GuiComponent gui,
//                                PoseStack mStack, int x, int y, float z, float alpha) {
//
//    }
//
//    @Override
//    public boolean shouldRender(MobEffectInstance effect) {
//        if (effect instanceof MKActiveEffect.MKMobEffectInstance) {
//            return this.effect.shouldRender(((MKActiveEffect.MKMobEffectInstance) effect).getEffectInstance());
//        } else {
//            return true;
//        }
//
//    }
//
//    @Override
//    public boolean shouldRenderInvText(MobEffectInstance effect) {
//        if (effect instanceof MKActiveEffect.MKMobEffectInstance) {
//            return this.effect.shouldRenderInvText(((MKActiveEffect.MKMobEffectInstance) effect).getEffectInstance());
//        } else {
//            return true;
//        }
//    }
//
//    @Override
//    public boolean shouldRenderHUD(MobEffectInstance effect) {
//        if (effect instanceof MKActiveEffect.MKMobEffectInstance) {
//            return this.effect.shouldRenderHUD(((MKActiveEffect.MKMobEffectInstance) effect).getEffectInstance());
//        } else {
//            return true;
//        }
//    }
}
