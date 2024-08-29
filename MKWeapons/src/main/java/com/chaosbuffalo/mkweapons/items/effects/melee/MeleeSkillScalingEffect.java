package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class MeleeSkillScalingEffect extends BaseMeleeWeaponEffect {
    public static final ResourceLocation NAME = MKWeapons.id("weapon_effect.skill_scaling");
    public static final Codec<MeleeSkillScalingEffect> CODEC = RecordCodecBuilder.<MeleeSkillScalingEffect>mapCodec(builder -> {
        return builder.group(
                Codec.DOUBLE.fieldOf("baseDamage").forGetter(i -> i.baseDamage),
                BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("skill").forGetter(i -> i.skill)
        ).apply(builder, MeleeSkillScalingEffect::new);
    }).codec();
    public static final ResourceLocation skillScaling = MKWeapons.id("melee_skill_scaling");
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
    public void onSkillChange(Player player) {
        onEntityUnequip(player);
        onEntityEquip(player);
    }

    @Override
    public void onEntityEquip(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr != null) {
            if (attr.getModifier(skillScaling) == null) {
                float skillLevel = MKAbility.getSkillLevel(entity, skill);
                attr.addTransientModifier(new AttributeModifier(skillScaling, skillLevel * baseDamage, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    @Override
    public void onEntityUnequip(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attr != null) {
            attr.removeModifier(skillScaling);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        tooltip.add(Component.translatable(skill.value().getDescriptionId()).withStyle(color));
        if (Screen.hasShiftDown()) {
            float skillLevel = player != null ? MKAbility.getSkillLevel(player, skill) : 0.0f;
            double bonus = skillLevel * baseDamage;
            tooltip.add(Component.translatable("mkweapons.weapon_effect.skill_scaling.description",
                    Component.translatable(skill.value().getDescriptionId()), MKAbility.NUMBER_FORMATTER.format(bonus)));
        }
    }
}
