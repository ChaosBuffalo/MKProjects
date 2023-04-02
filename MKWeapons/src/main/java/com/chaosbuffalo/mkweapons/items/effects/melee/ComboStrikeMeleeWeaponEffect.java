package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
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

public class ComboStrikeMeleeWeaponEffect extends SwingMeleeWeaponEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.combo_strike");

    public ComboStrikeMeleeWeaponEffect(int numberOfHits, double perHit) {
        super(NAME, ChatFormatting.GREEN, numberOfHits, perHit);
    }

    public ComboStrikeMeleeWeaponEffect(){
        super(NAME, ChatFormatting.GREEN);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip) {
        super.addInformation(stack, worldIn, tooltip);
        if (Screen.hasShiftDown()){
            tooltip.add(new TextComponent(I18n.get("mkweapons.weapon_effect.combo_strike.description",
                    getPerHit() * 100.0f, getNumberOfHits())));
        }
    }

    @Override
    public void postAttack(IMKMeleeWeapon weapon, ItemStack stack, LivingEntity attacker) {
        MKCore.getEntityData(attacker).ifPresent(cap -> {
            CombatExtensionModule combatModule = cap.getCombatExtension();
            if (combatModule.isMidCombo()) {
                int hit = combatModule.getCurrentSwingCount() % getNumberOfHits();
                double totalReduction = hit * getPerHit();
                double cooldownPeriod = EntityUtils.getCooldownPeriod(attacker);
                int newTicks = (int) Math.round(cooldownPeriod * totalReduction);
                combatModule.addEntityTicksSinceLastSwing(newTicks);
            }
        });
    }
}
