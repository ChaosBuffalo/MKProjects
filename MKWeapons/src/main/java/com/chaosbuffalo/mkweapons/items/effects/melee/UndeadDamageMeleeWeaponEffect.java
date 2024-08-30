package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class UndeadDamageMeleeWeaponEffect extends DamageMultiplierMeleeWeaponEffect {
    public static final ResourceLocation NAME = MKWeapons.id("weapon_effect.undead_damage");
    public static final MapCodec<UndeadDamageMeleeWeaponEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.FLOAT.fieldOf("multiplier").forGetter(DamageMultiplierMeleeWeaponEffect::getDamageMultiplier)
    ).apply(builder, UndeadDamageMeleeWeaponEffect::new));

    public UndeadDamageMeleeWeaponEffect(float multiplier) {
        super(NAME, ChatFormatting.GOLD, multiplier);
    }

    @Override
    public boolean isTargetSuitable(LivingEntity attacker, LivingEntity target, IMKMeleeWeapon weapon, ItemStack stack) {
        return target.isInvertedHealAndHarm();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        super.addInformation(stack, player, tooltip);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("mkweapons.weapon_effect.undead_damage.description",
                    MKAbility.PERCENT_FORMATTER.format(damageMultiplier)));
        }
    }
}
