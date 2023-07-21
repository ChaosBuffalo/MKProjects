package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class LivingDamageMeleeWeaponEffect extends DamageMultiplierMeleeWeaponEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.living_damage");

    public LivingDamageMeleeWeaponEffect(float multiplier) {
        this();
        this.damageMultiplier = multiplier;
    }

    @Override
    public boolean isTargetSuitable(LivingEntity attacker, LivingEntity target, IMKMeleeWeapon weapon, ItemStack stack) {
        return !target.isInvertedHealAndHarm();
    }

    public LivingDamageMeleeWeaponEffect() {
        super(NAME, ChatFormatting.RED);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        super.addInformation(stack, player, tooltip);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("mkweapons.weapon_effect.living_damage.description",
                    MKAbility.PERCENT_FORMATTER.format(damageMultiplier)));
        }
    }
}
