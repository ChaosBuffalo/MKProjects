package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class LivingDamageMeleeWeaponEffect extends DamageMultiplierMeleeWeaponEffect {
    public static final ResourceLocation NAME = MKWeapons.id("weapon_effect.living_damage");
    public static final MapCodec<LivingDamageMeleeWeaponEffect> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.FLOAT.fieldOf("multiplier").forGetter(LivingDamageMeleeWeaponEffect::getDamageMultiplier)
    ).apply(builder, LivingDamageMeleeWeaponEffect::new));
    public static final StreamCodec<FriendlyByteBuf, LivingDamageMeleeWeaponEffect> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, LivingDamageMeleeWeaponEffect::getDamageMultiplier,
            LivingDamageMeleeWeaponEffect::new
    );

    public LivingDamageMeleeWeaponEffect(float multiplier) {
        super(NAME, ChatFormatting.RED, multiplier);
    }

    @Override
    public boolean isTargetSuitable(LivingEntity attacker, LivingEntity target, IMKMeleeWeapon weapon, ItemStack stack) {
        return !target.isInvertedHealAndHarm();
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
