package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.MKConfig;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class MeleeSkillScalingEffect extends BaseMeleeWeaponEffect {
    public static final ResourceLocation NAME = MKWeapons.id("weapon_effect.skill_scaling");
    public static final MapCodec<MeleeSkillScalingEffect> MAP_CODEC = RecordCodecBuilder.<MeleeSkillScalingEffect>mapCodec(builder -> {
        return builder.group(
                Codec.DOUBLE.fieldOf("baseDamage").forGetter(i -> i.baseDamage),
                BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("skill").forGetter(i -> i.skill)
        ).apply(builder, MeleeSkillScalingEffect::new);
    });
    private final double baseDamage;
    private final Holder<Attribute> skill;

    public MeleeSkillScalingEffect(double baseDamage, Holder<Attribute> skill) {
        super(NAME, ChatFormatting.GRAY);
        this.baseDamage = baseDamage;
        this.skill = skill;
    }

    @Override
    public void onHit(IMKMeleeWeapon weapon, ItemStack stack, IMKEntityData attackerData, LivingEntity target) {
        if (attackerData instanceof MKPlayerData playerData) {
            playerData.getSkills().tryScaledIncreaseSkill(skill, 0.5);
        }
    }

    @Override
    public float modifyBaseAttackDamage(float damage, IMKMeleeWeapon weapon, ItemStack stack,
                                        LivingEntity attacker, InteractionHand hand) {
        if (this.skill == null) {
            return damage;
        }
        float skillLevel = MKAbility.getSkillLevel(attacker, skill);
        double amount = skillLevel * baseDamage * MKConfig.SERVER.skillScalingMultiplier.getAsDouble();
        return damage + (float) amount;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        tooltip.add(Component.translatable(skill.value().getDescriptionId()).withStyle(color));
        if (Screen.hasShiftDown()) {
            float skillLevel = player != null ? MKAbility.getSkillLevel(player, skill) : 0.0f;
            double bonus = skillLevel * baseDamage * MKConfig.SERVER.skillScalingMultiplier.getAsDouble();
            tooltip.add(Component.translatable("mkweapons.weapon_effect.skill_scaling.description",
                    Component.translatable(skill.value().getDescriptionId()), MKAbility.NUMBER_FORMATTER.format(bonus)));
        }
    }
}
