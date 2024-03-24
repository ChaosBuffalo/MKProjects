package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.EntityTargetingAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class OnHitAbilityEffect extends BaseMeleeWeaponEffect {
    private double procChance;
    private float skillLevel;
    @Nonnull
    private Supplier<? extends EntityTargetingAbility> abilitySupplier;

    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID,
            "weapon_effect.on_hit_ability");

    public OnHitAbilityEffect(double procChance, float skillLevel, @Nonnull Supplier<? extends EntityTargetingAbility> abilitySupplier) {
        this();
        this.procChance = procChance;
        this.skillLevel = skillLevel;
        this.abilitySupplier = abilitySupplier;
    }

    public OnHitAbilityEffect() {
        super(NAME, ChatFormatting.GOLD);
        abilitySupplier = () -> null;
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
                        abilitySupplier = () -> entAbility;
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

    protected AbilityContext createAbilityContext(IMKEntityData casterData) {
        AbilityContext context = AbilityContext.forCaster(casterData, abilitySupplier.get());
        context.setSkillResolver((e, attr) -> skillLevel);
        return context;
    }

    @Override
    public void onHit(IMKMeleeWeapon weapon, ItemStack stack, IMKEntityData attackerData, LivingEntity target) {
        EntityTargetingAbility ability = abilitySupplier.get();
        if (ability != null && attackerData.getEntity().getRandom().nextDouble() >= (1.0 - procChance)) {
            AbilityContext context = createAbilityContext(attackerData);
            ability.castAtEntity(attackerData, target, context);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
        MKAbility ability = abilitySupplier.get();
        if (ability == null)
            return;

        tooltip.add(Component.translatable(String.format("%s.%s.name",
                this.getTypeName().getNamespace(), this.getTypeName().getPath()), ability.getAbilityName()).withStyle(color));
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable(String.format("%s.%s.description",
                            this.getTypeName().getNamespace(), this.getTypeName().getPath()),
                    MKAbility.PERCENT_FORMATTER.format(procChance), MKAbility.NUMBER_FORMATTER.format(skillLevel)));
            if (player != null) {
                MKCore.getEntityData(player).ifPresent(entityData -> {
                    AbilityContext context = createAbilityContext(entityData);
                    tooltip.add(ability.getAbilityDescription(entityData, context));
                });
            }
        }
    }
}
