package com.chaosbuffalo.mkweapons.items.effects.ranged;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkweapons.MKWeapons;
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

public class RapidFireRangedWeaponEffect extends BaseRangedWeaponEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.rapid_fire");
    public static final Codec<RapidFireRangedWeaponEffect> CODEC = RecordCodecBuilder.<RapidFireRangedWeaponEffect>mapCodec(builder -> {
        return builder.group(
                Codec.INT.fieldOf("maxHits").forGetter(i -> i.maxHits),
                Codec.FLOAT.fieldOf("perHit").forGetter(i -> i.perHitReduction)
        ).apply(builder, RapidFireRangedWeaponEffect::new);
    }).codec();

    private final int maxHits;
    private final float perHitReduction;

    public RapidFireRangedWeaponEffect(int maxHits, float perHitReduction) {
        super(NAME, ChatFormatting.DARK_RED);
        this.maxHits = maxHits;
        this.perHitReduction = perHitReduction;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Player player,
                               List<Component> tooltip) {
        super.addInformation(stack, player, tooltip);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("mkweapons.weapon_effect.rapid_fire.description",
                    perHitReduction * 100.0f, maxHits * perHitReduction * 100.0f));
        }
    }

    @Override
    public float modifyDrawTime(float inTime, ItemStack item, LivingEntity entity) {
        return MKCore.getEntityData(entity).map(cap -> {
            int totalToReduce = Math.min(cap.getCombatExtension().getCurrentProjectileHitCount(), maxHits);
            float timeReduction = totalToReduce * perHitReduction;
            return (1.0f - timeReduction) * inTime;
        }).orElse(inTime);

    }
}
