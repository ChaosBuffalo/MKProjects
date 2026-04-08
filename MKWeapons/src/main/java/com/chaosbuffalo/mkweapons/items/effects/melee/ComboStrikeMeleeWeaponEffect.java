package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class ComboStrikeMeleeWeaponEffect extends BaseMeleeWeaponEffect {
    public static final ResourceLocation NAME = MKWeapons.id("weapon_effect.combo_strike");
    public static final MapCodec<ComboStrikeMeleeWeaponEffect> MAP_CODEC = RecordCodecBuilder.<ComboStrikeMeleeWeaponEffect>mapCodec(builder -> {
        return builder.group(
                Codec.INT.fieldOf("numberOfHits").forGetter(ComboStrikeMeleeWeaponEffect::getNumberOfHits),
                Codec.DOUBLE.fieldOf("perHit").forGetter(ComboStrikeMeleeWeaponEffect::getPerHit)
        ).apply(builder, ComboStrikeMeleeWeaponEffect::new);
    });

    protected final int numberOfHits;
    protected final double perHit;

    public ComboStrikeMeleeWeaponEffect(int numberOfHits, double perHit) {
        super(NAME, ChatFormatting.GREEN);
        this.numberOfHits = numberOfHits;
        this.perHit = perHit;
    }

    public double getPerHit() {
        return perHit;
    }

    public int getNumberOfHits() {
        return numberOfHits;
    }

    public double getCooldownReductionForCompletedHits(int completedHits) {
        if (getNumberOfHits() <= 0) {
            return 0.0;
        }
        int hit = Math.floorMod(completedHits, getNumberOfHits());
        return hit * getPerHit();
    }

    public int getCooldownAdjustmentTicks(int cooldownTicks, int completedHits) {
        return (int) Math.round(cooldownTicks * getCooldownReductionForCompletedHits(completedHits));
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
        if (combatModule.isMidMeleeCombo()) {
            int newTicks = getCooldownAdjustmentTicks((int) Math.round(EntityUtils.getCooldownPeriod(attackerData.getEntity())),
                    combatModule.getCurrentSwingCount());
            combatModule.increaseAttackStrengthTicks(newTicks);
        }
    }
}
