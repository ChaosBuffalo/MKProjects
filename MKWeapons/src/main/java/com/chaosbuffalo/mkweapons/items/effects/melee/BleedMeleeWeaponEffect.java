package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.effects.BleedEffect;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

public class BleedMeleeWeaponEffect extends BaseMeleeWeaponEffect {

    private float damageMultiplier;
    private int maxStacks;
    private int durationSeconds;
    private Attribute skill;
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.bleed");

    public BleedMeleeWeaponEffect(float damageMultiplier, int maxStacks, int durationSeconds, Attribute skill) {
        this();
        this.damageMultiplier = damageMultiplier;
        this.maxStacks = maxStacks;
        this.durationSeconds = durationSeconds;
        this.skill = skill;
    }

    public BleedMeleeWeaponEffect() {
        super(NAME, ChatFormatting.DARK_RED);
    }

    public void setDamageMultiplier(float damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public void setMaxStacks(int maxStacks) {
        this.maxStacks = maxStacks;
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        setMaxStacks(dynamic.get("maxStacks").asInt(5));
        setDurationSeconds(dynamic.get("durationSeconds").asInt(5));
        setDamageMultiplier(dynamic.get("damageMultiplier").asFloat(2.0f));
        dynamic.get("skill").asString().result().ifPresent(x -> {
            skill = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(x));
        });
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("damageMultiplier"), ops.createFloat(damageMultiplier));
        builder.put(ops.createString("maxStacks"), ops.createInt(maxStacks));
        builder.put(ops.createString("durationSeconds"), ops.createInt(durationSeconds));
        builder.put(ops.createString("skill"), ops.createString(ForgeRegistries.ATTRIBUTES.getKey(skill).toString()));
    }

    @Override
    public void onHit(IMKMeleeWeapon weapon, ItemStack stack,
                      LivingEntity target, LivingEntity attacker) {
        float damagePerSecond = damageMultiplier * weapon.getDamageForTier() / durationSeconds;
        float skillLevel = MKAbility.getSkillLevel(attacker, skill);
//        MKCore.getPlayer(attacker).ifPresent(x -> x.getSkills().tryIncreaseSkill(skill));
        MKCore.getEntityData(target).ifPresent(targetData -> {
            MKEffectBuilder<?> effect = BleedEffect.from(attacker, maxStacks, damagePerSecond, damagePerSecond, 1f)
                    .periodic(GameConstants.TICKS_PER_SECOND) // tick every second
                    .timed(GameConstants.TICKS_PER_SECOND * durationSeconds + 10)
                    .skillLevel(skillLevel);
            targetData.getEffects().addEffect(effect);
        });
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip) {
        super.addInformation(stack, worldIn, tooltip);
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("mkweapons.weapon_effect.bleed.description",
                    damageMultiplier, durationSeconds, maxStacks, new TranslatableComponent(skill.getDescriptionId())));
        }
    }

}
