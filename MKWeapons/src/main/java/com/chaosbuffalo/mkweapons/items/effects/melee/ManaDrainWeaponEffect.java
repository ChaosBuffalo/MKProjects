package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
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

public class ManaDrainWeaponEffect extends BaseMeleeWeaponEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.mana_drain");
    public static final Codec<ManaDrainWeaponEffect> CODEC = RecordCodecBuilder.<ManaDrainWeaponEffect>mapCodec(builder -> {
        return builder.group(
                Codec.FLOAT.fieldOf("damage_multiplier").forGetter(i -> i.damageMultiplier),
                Codec.FLOAT.fieldOf("efficiency").forGetter(i -> i.efficiency)
        ).apply(builder, ManaDrainWeaponEffect::new);
    }).codec();

    private final float damageMultiplier;
    private final float efficiency;

    public ManaDrainWeaponEffect(float damageMultiplier, float efficiency) {
        super(NAME, ChatFormatting.DARK_AQUA);
        this.damageMultiplier = damageMultiplier;
        this.efficiency = efficiency;
    }

    @Override
    public void onHurt(float damage, IMKMeleeWeapon weapon, ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.onHurt(damage, weapon, stack, target, attacker);
        MKCore.getEntityData(target).ifPresent(targetData -> {
            if (targetData.getStats().consumeMana(damage * damageMultiplier)) {
                MKCore.getEntityData(attacker).ifPresent(attackerData -> attackerData.getStats()
                        .addMana(damage * damageMultiplier * efficiency));
            }
        });
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        super.addInformation(stack, player, tooltip);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("mkweapons.weapon_effect.mana_drain.description",
                    MKAbility.PERCENT_FORMATTER.format(damageMultiplier), MKAbility.PERCENT_FORMATTER.format(efficiency)));
        }
    }
}
