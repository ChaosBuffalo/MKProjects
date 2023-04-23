package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.status.StunEffect;
import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class OnHitAbilityEffect extends BaseMeleeWeaponEffect {
    private double procChance;
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID,
            "weapon_effect.on_hit_ability");

    public OnHitAbilityEffect(double procChance) {
        this();
        this.procChance = procChance;
    }

    public OnHitAbilityEffect() {
        super(NAME, ChatFormatting.GOLD);
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        procChance = dynamic.get("chance").asDouble(0.05);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("chance"), ops.createDouble(procChance));
    }

    @Override
    public void onHit(IMKMeleeWeapon weapon, ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getRandom().nextDouble() >= (1.0 - procChance)) {
//            MKEffectBuilder<?> stun = StunEffect.from(attacker)
//                    .timed(stunDuration * GameConstants.TICKS_PER_SECOND);
//            MKCore.getEntityData(target).ifPresent(targetData -> targetData.getEffects().addEffect(stun));
//            MKParticles.spawn(target, new Vec3(0.0, target.getBbHeight(), 0.0), PARTICLES);
        }
    }

//    @Override
//    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip) {
//        super.addInformation(stack, worldIn, tooltip);
//        if (Screen.hasShiftDown()) {
//            tooltip.add(Component.translatable("mkweapons.weapon_effect.stun.description",
//                    stunChance * 100.0, stunDuration));
//        }
//    }
}
