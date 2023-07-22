package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class ComboStrikeMeleeWeaponEffect extends SwingMeleeWeaponEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.combo_strike");

    public ComboStrikeMeleeWeaponEffect(int numberOfHits, double perHit) {
        super(NAME, ChatFormatting.GREEN, numberOfHits, perHit);
    }

    public ComboStrikeMeleeWeaponEffect() {
        super(NAME, ChatFormatting.GREEN);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        super.addInformation(stack, player, tooltip);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("mkweapons.weapon_effect.combo_strike.description",
                    getPerHit() * 100.0f, getNumberOfHits()));
        }
    }

    @Override
    public void postAttack(IMKMeleeWeapon weapon, ItemStack stack, IMKEntityData attackerData) {
        CombatExtensionModule combatModule = attackerData.getCombatExtension();
        if (combatModule.isMidCombo()) {
            int hit = combatModule.getCurrentSwingCount() % getNumberOfHits();
            double totalReduction = hit * getPerHit();
            double cooldownPeriod = EntityUtils.getCooldownPeriod(attackerData.getEntity());
            int newTicks = (int) Math.round(cooldownPeriod * totalReduction);
            combatModule.addEntityTicksSinceLastSwing(newTicks);
        }
    }
}
