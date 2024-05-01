package com.chaosbuffalo.mkweapons.items.effects.melee;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.EntityTargetingAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class OnHitAbilityEffect extends BaseMeleeWeaponEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "weapon_effect.on_hit_ability");
    public static final Codec<OnHitAbilityEffect> CODEC = ExtraCodecs.lazyInitializedCodec(() ->
            RecordCodecBuilder.<OnHitAbilityEffect>mapCodec(builder -> {
                return builder.group(
                        Codec.DOUBLE.fieldOf("chance").forGetter(i -> i.procChance),
                        Codec.FLOAT.fieldOf("skill_level").forGetter(i -> i.skillLevel),
                        MKCoreRegistry.ABILITIES.getCodec().comapFlatMap(ability -> {
                            if (ability instanceof EntityTargetingAbility targetingAbility) {
                                return DataResult.success(targetingAbility);
                            }
                            return DataResult.error(() -> "Ability " + ability + " is not an EntityTargetingAbility");
                        }, Function.identity()).fieldOf("ability").forGetter(i -> i.abilitySupplier.get())
                ).apply(builder, OnHitAbilityEffect::new);
            }).codec());


    private final double procChance;
    private final float skillLevel;
    @Nonnull
    private final Supplier<? extends EntityTargetingAbility> abilitySupplier;

    public OnHitAbilityEffect(double procChance, float skillLevel, EntityTargetingAbility ability) {
        this(procChance, skillLevel, () -> ability);
    }

    public OnHitAbilityEffect(double procChance, float skillLevel, @Nonnull Supplier<? extends EntityTargetingAbility> abilitySupplier) {
        super(NAME, ChatFormatting.GOLD);
        this.procChance = procChance;
        this.skillLevel = skillLevel;
        this.abilitySupplier = abilitySupplier;
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
