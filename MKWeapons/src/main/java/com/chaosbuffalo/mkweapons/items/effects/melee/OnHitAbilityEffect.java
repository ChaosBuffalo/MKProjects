package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.EntityTargetingAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class OnHitAbilityEffect extends BaseMeleeWeaponEffect {
    private double procChance;
    private float skillLevel;
    private Supplier<? extends EntityTargetingAbility> abilitySupplier;

    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID,
            "weapon_effect.on_hit_ability");

    public OnHitAbilityEffect(double procChance, float skillLevel, Supplier<? extends EntityTargetingAbility> abilitySupplier) {
        this();
        this.procChance = procChance;
        this.skillLevel = skillLevel;
        this.abilitySupplier = abilitySupplier;
    }

    public OnHitAbilityEffect() {
        super(NAME, ChatFormatting.GOLD);
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        procChance = dynamic.get("chance").asDouble(0.05);
        skillLevel = dynamic.get("skill_level").asFloat(0f);
        dynamic.get("ability").asString().result()
                .ifPresent(abilityName -> {
                    MKAbility ability = MKCoreRegistry.ABILITIES.getValue(new ResourceLocation(abilityName));
                    if (ability instanceof EntityTargetingAbility entAbility) {
                        abilitySupplier = Lazy.of(() -> entAbility);
                    }
                });
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("chance"), ops.createDouble(procChance));
        builder.put(ops.createString("skill_level"), ops.createFloat(skillLevel));
        if (abilitySupplier.get() != null) {
            builder.put(ops.createString("ability"),
                    ops.createString(abilitySupplier.get().getAbilityId().toString()));
        }

    }

    @Override
    public void onHit(IMKMeleeWeapon weapon, ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getRandom().nextDouble() >= (1.0 - procChance)) {
            MKCore.getEntityData(attacker).ifPresent(entityData -> abilitySupplier.get().castAtEntity(entityData,
                    target, attr -> skillLevel));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip) {
        tooltip.add(Component.translatable(String.format("%s.%s.name",
                this.getTypeName().getNamespace(), this.getTypeName().getPath()), abilitySupplier.get().getAbilityName()).withStyle(color));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable(String.format("%s.%s.description",
                    this.getTypeName().getNamespace(), this.getTypeName().getPath()),
                    MKAbility.PERCENT_FORMATTER.format(procChance), MKAbility.NUMBER_FORMATTER.format(skillLevel)));
            if (Minecraft.getInstance().player != null) {
                MKCore.getEntityData(Minecraft.getInstance().player).ifPresent(entityData -> {
                    // FIXME: thread this through the effect better
                    MKAbilityInfo abilityInfo = abilitySupplier.get().createAbilityInfo();
                    abilityInfo.setSkillValueResolver(attr -> skillLevel);
                    tooltip.add(abilityInfo.getAbility().getAbilityDescription(entityData, abilityInfo));
                });
            }

        }
    }
}
