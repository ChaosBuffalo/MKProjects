package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.status.StunEffect;
import com.chaosbuffalo.mkcore.fx.MKParticles;
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
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class StunMeleeWeaponEffect extends BaseMeleeWeaponEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.stun");
    public static final Codec<StunMeleeWeaponEffect> CODEC = RecordCodecBuilder.<StunMeleeWeaponEffect>mapCodec(builder -> {
        return builder.group(
                Codec.DOUBLE.fieldOf("chance").forGetter(i -> i.stunChance),
                Codec.INT.fieldOf("duration").forGetter(i -> i.stunDuration)
        ).apply(builder, StunMeleeWeaponEffect::new);
    }).codec();
    public static final ResourceLocation PARTICLES = new ResourceLocation(MKWeapons.MODID, "stun_effect");

    private final int stunDuration;
    private final double stunChance;

    public StunMeleeWeaponEffect(double stunChance, int stunSeconds) {
        super(NAME, ChatFormatting.DARK_PURPLE);
        this.stunChance = stunChance;
        this.stunDuration = stunSeconds;
    }

    @Override
    public void onHit(IMKMeleeWeapon weapon, ItemStack stack, IMKEntityData attackerData, LivingEntity target) {
        if (attackerData.getEntity().getRandom().nextDouble() >= (1.0 - stunChance)) {
            MKEffectBuilder<?> stun = StunEffect.from(attackerData.getEntity())
                    .timed(stunDuration * GameConstants.TICKS_PER_SECOND);
            MKCore.getEntityData(target).ifPresent(targetData -> targetData.getEffects().addEffect(stun));
            MKParticles.spawn(target, new Vec3(0.0, target.getBbHeight(), 0.0), PARTICLES);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        super.addInformation(stack, player, tooltip);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("mkweapons.weapon_effect.stun.description",
                    stunChance * 100.0, stunDuration));
        }
    }
}
