package com.chaosbuffalo.mkweapons.items.effects.ranged;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class RangedSkillScalingEffect extends BaseRangedWeaponEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.ranged_skill_scaling");
    public static final Codec<RangedSkillScalingEffect> CODEC = RecordCodecBuilder.<RangedSkillScalingEffect>mapCodec(builder -> {
        return builder.group(
                Codec.DOUBLE.fieldOf("baseDamage").forGetter(i -> i.baseDamage),
                ForgeRegistries.ATTRIBUTES.getCodec().fieldOf("skill").forGetter(i -> i.skill)
        ).apply(builder, RangedSkillScalingEffect::new);
    }).codec();

    public static final UUID skillScaling = UUID.fromString("e4e8a04a-6c8e-43f6-9599-99a84f207c60");
    private final double baseDamage;
    private final Attribute skill;

    public RangedSkillScalingEffect(double baseDamage, Attribute skill) {
        super(NAME, ChatFormatting.GRAY);
        this.baseDamage = baseDamage;
        this.skill = skill;
    }

    @Override
    public void onProjectileHit(LivingHurtEvent event, DamageSource source, LivingEntity livingTarget,
                                IMKEntityData attackerData, AbstractArrow arrow, ItemStack bow) {
        if (attackerData instanceof MKPlayerData playerData) {
            playerData.getSkills().tryScaledIncreaseSkill(skill, 0.5);
        }
    }

//    @Override
//    public double modifyArrowDamage(double inDamage, LivingEntity shooter, AbstractArrowEntity arrow) {
//        float skillLevel = MKAbility.getSkillLevel(shooter, skill);
//        double bonus = skillLevel * baseDamage;
//        return inDamage + bonus;
//    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        tooltip.add(Component.translatable(skill.getDescriptionId()).withStyle(color));
        if (Screen.hasShiftDown()) {
            float skillLevel = player != null ? MKAbility.getSkillLevel(player, skill) : 0.0f;
            double bonus = skillLevel * baseDamage;
            tooltip.add(Component.translatable("mkweapons.weapon_effect.ranged_skill_scaling.description",
                    Component.translatable(skill.getDescriptionId()), MKAbility.NUMBER_FORMATTER.format(bonus)));
        }
    }

    @Override
    public void onEntityEquip(LivingEntity entity) {
        float skillLevel = MKAbility.getSkillLevel(entity, skill);
        AttributeInstance attr = entity.getAttribute(MKAttributes.RANGED_DAMAGE);
        if (attr != null) {
            if (attr.getModifier(skillScaling) == null) {
                attr.addTransientModifier(new AttributeModifier(skillScaling, "skill scaling", skillLevel * baseDamage, AttributeModifier.Operation.ADDITION));
            }
        }
    }

    @Override
    public void onEntityUnequip(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(MKAttributes.RANGED_DAMAGE);
        if (attr != null) {
            attr.removeModifier(skillScaling);
        }
    }

    @Override
    public void onSkillChange(Player player) {
        onEntityUnequip(player);
        onEntityEquip(player);
    }
}
