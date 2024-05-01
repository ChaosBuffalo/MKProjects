package com.chaosbuffalo.mkweapons.items.effects.ranged;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import javax.annotation.Nullable;
import java.util.List;

public class RangedManaDrainEffect extends BaseRangedWeaponEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.ranged_mana_drain");
    public static final Codec<RangedManaDrainEffect> CODEC = RecordCodecBuilder.<RangedManaDrainEffect>mapCodec(builder -> {
        return builder.group(
                Codec.FLOAT.fieldOf("damage_multiplier").forGetter(i -> i.damageMultiplier),
                Codec.FLOAT.fieldOf("efficiency").forGetter(i -> i.efficiency)
        ).apply(builder, RangedManaDrainEffect::new);
    }).codec();

    private final float damageMultiplier;
    private final float efficiency;

    public RangedManaDrainEffect(float damageMultiplier, float efficiency) {
        super(NAME, ChatFormatting.DARK_AQUA);
        this.damageMultiplier = damageMultiplier;
        this.efficiency = efficiency;
    }

    @Override
    public void onProjectileHit(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                                IMKEntityData attackerData, AbstractArrow arrow, ItemStack bow) {
        MKCore.getEntityData(livingTarget).ifPresent(targetData -> {
            if (targetData.getStats().consumeMana(event.getAmount() * damageMultiplier)) {
                attackerData.getStats().addMana(event.getAmount() * damageMultiplier * efficiency);
            }
        });
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        super.addInformation(stack, player, tooltip);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("mkweapons.weapon_effect.ranged_mana_drain.description",
                    MKAbility.PERCENT_FORMATTER.format(damageMultiplier), MKAbility.PERCENT_FORMATTER.format(efficiency)));
        }
    }
}
