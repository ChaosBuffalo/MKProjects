package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class FuryStrikeMeleeWeaponEffect extends SwingMeleeWeaponEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.fury_strike");

    public FuryStrikeMeleeWeaponEffect(int numberOfHits, double perHit) {
        super(NAME, ChatFormatting.GREEN, numberOfHits, perHit);
    }

    public FuryStrikeMeleeWeaponEffect(){
        super(NAME, ChatFormatting.GREEN);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip) {
        super.addInformation(stack, worldIn, tooltip);
        if (Screen.hasShiftDown()) {
            tooltip.add(new TextComponent(I18n.get("mkweapons.weapon_effect.fury_strike.description",
                    getPerHit() * 100.0f, getNumberOfHits())));
        }
    }


    @Override
    public float modifyDamageDealt(float damage, IMKMeleeWeapon weapon, ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return MKCore.getEntityData(attacker).map(cap -> {
            CombatExtensionModule combatModule = cap.getCombatExtension();
            if (combatModule.isMidCombo()) {
                int hit = combatModule.getCurrentSwingCount() % getNumberOfHits();
                double damageIncrease = 1.0 + hit * getPerHit();
                return damageIncrease * damage;
            } else {
                return damage;
            }
        }).orElse(damage).floatValue();
    }
}
