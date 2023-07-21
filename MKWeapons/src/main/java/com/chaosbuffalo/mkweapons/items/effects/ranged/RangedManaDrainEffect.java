package com.chaosbuffalo.mkweapons.items.effects.ranged;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
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

    private float damageMultiplier;
    private float efficiency;

    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.ranged_mana_drain");

    public RangedManaDrainEffect(float damageMultiplier, float efficiency) {
        this();
        this.damageMultiplier = damageMultiplier;
        this.efficiency = efficiency;
    }

    public RangedManaDrainEffect() {
        super(NAME, ChatFormatting.DARK_AQUA);
    }

    public void setDamageMultiplier(float damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public void setEfficiency(float efficiency) {
        this.efficiency = efficiency;
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        setEfficiency(dynamic.get("efficiency").asFloat(0.5f));
        setDamageMultiplier(dynamic.get("damage_multiplier").asFloat(0.5f));
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("damage_multiplier"), ops.createFloat(damageMultiplier));
        builder.put(ops.createString("efficiency"), ops.createFloat(efficiency));
    }

    @Override
    public void onProjectileHit(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                                LivingEntity livingSource, IMKEntityData sourceData, AbstractArrow arrow, ItemStack bow) {
        super.onProjectileHit(event, source, livingTarget, livingSource, sourceData, arrow, bow);
        MKCore.getEntityData(livingTarget).ifPresent(targetData -> {
            if (targetData.getStats().consumeMana(event.getAmount() * damageMultiplier)) {
                MKCore.getEntityData(livingSource).ifPresent(attackerData -> attackerData.getStats()
                        .addMana(event.getAmount() * damageMultiplier * efficiency));
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
