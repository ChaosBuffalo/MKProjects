package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.CombatExtensionModule;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.mojang.serialization.Codec;
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

public class FuryStrikeMeleeWeaponEffect extends BaseMeleeWeaponEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.fury_strike");
    public static final Codec<FuryStrikeMeleeWeaponEffect> CODEC = RecordCodecBuilder.<FuryStrikeMeleeWeaponEffect>mapCodec(builder -> {
        return builder.group(
                Codec.INT.fieldOf("numberOfHits").forGetter(FuryStrikeMeleeWeaponEffect::getNumberOfHits),
                Codec.DOUBLE.fieldOf("perHit").forGetter(FuryStrikeMeleeWeaponEffect::getPerHit)
        ).apply(builder, FuryStrikeMeleeWeaponEffect::new);
    }).codec();

    protected final int numberOfHits;
    protected final double perHit;

    public FuryStrikeMeleeWeaponEffect(int numberOfHits, double perHit) {
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

    @Override
    public void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        super.addInformation(stack, player, tooltip);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("mkweapons.weapon_effect.fury_strike.description",
                    getPerHit() * 100.0f, getNumberOfHits()));
        }
    }


    @Override
    public float modifyDamageDealt(float damage, IMKMeleeWeapon weapon, ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return MKCore.getEntityData(attacker).map(cap -> {
            CombatExtensionModule combatModule = cap.getCombatExtension();
            if (combatModule.isMidMeleeCombo()) {
                int hit = combatModule.getCurrentSwingCount() % getNumberOfHits();
                double damageIncrease = 1.0 + hit * getPerHit();
                return damageIncrease * damage;
            } else {
                return damage;
            }
        }).orElse(damage).floatValue();
    }
}
