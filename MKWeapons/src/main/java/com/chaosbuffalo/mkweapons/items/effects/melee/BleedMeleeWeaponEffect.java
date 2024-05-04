package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.effects.BleedEffect;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

public class BleedMeleeWeaponEffect extends BaseMeleeWeaponEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.bleed");
    public static final Codec<BleedMeleeWeaponEffect> CODEC = RecordCodecBuilder.<BleedMeleeWeaponEffect>mapCodec(builder -> {
        return builder.group(
                Codec.FLOAT.fieldOf("damageMultiplier").forGetter(i -> i.damageMultiplier),
                Codec.INT.fieldOf("maxStacks").forGetter(i -> i.maxStacks),
                Codec.INT.fieldOf("durationSeconds").forGetter(i -> i.durationSeconds),
                ForgeRegistries.ATTRIBUTES.getCodec().fieldOf("skill").forGetter(i -> i.skill)
        ).apply(builder, BleedMeleeWeaponEffect::new);
    }).codec();

    private final float damageMultiplier;
    private final int maxStacks;
    private final int durationSeconds;
    private final Attribute skill;

    public BleedMeleeWeaponEffect(float damageMultiplier, int maxStacks, int durationSeconds, Attribute skill) {
        super(NAME, ChatFormatting.DARK_RED);
        this.damageMultiplier = damageMultiplier;
        this.maxStacks = maxStacks;
        this.durationSeconds = durationSeconds;
        this.skill = skill;
    }

    @Override
    public void onHit(IMKMeleeWeapon weapon, ItemStack stack,
                      IMKEntityData attackerData, LivingEntity target) {
        float damagePerSecond = damageMultiplier * weapon.getDamageForTier() / durationSeconds;
        float skillLevel = MKAbility.getSkillLevel(attackerData.getEntity(), skill);
//        MKCore.getPlayer(attacker).ifPresent(x -> x.getSkills().tryIncreaseSkill(skill));
        MKCore.getEntityData(target).ifPresent(targetData -> {
            MKEffectBuilder<?> effect = BleedEffect.from(attackerData.getEntity(), maxStacks, damagePerSecond, damagePerSecond, 1f)
                    .periodic(GameConstants.TICKS_PER_SECOND) // tick every second
                    .timed(GameConstants.TICKS_PER_SECOND * durationSeconds + 10)
                    .skillLevel(skillLevel);
            targetData.getEffects().addEffect(effect);
        });
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        super.addInformation(stack, player, tooltip);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("mkweapons.weapon_effect.bleed.description",
                    damageMultiplier, durationSeconds, maxStacks, Component.translatable(skill.getDescriptionId())));
        }
    }
}
