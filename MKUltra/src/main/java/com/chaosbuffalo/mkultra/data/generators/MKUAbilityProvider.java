package com.chaosbuffalo.mkultra.data.generators;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.projectiles.BurstProjectileBehavior;
import com.chaosbuffalo.mkcore.abilities.projectiles.ProjectileCastBehavior;
import com.chaosbuffalo.mkcore.abilities.projectiles.SimpleProjectileBehavior;
import com.chaosbuffalo.mkcore.data.providers.MKAbilityProvider;
import com.chaosbuffalo.mkcore.formulas.AbilityFormula;
import com.chaosbuffalo.mkcore.formulas.FormulaContextKey;
import com.chaosbuffalo.mkcore.formulas.FormulaParameters;
import com.chaosbuffalo.mkcore.utils.location.CircularLocationProvider;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.abilities.cleric.HealAbility;
import com.chaosbuffalo.mkultra.abilities.cleric.SmiteAbility;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MKUAbilityProvider extends MKAbilityProvider {
    private static final Set<ResourceLocation> VARIANT_IDS = Set.of(
            MKUAbilities.GREATER_HEAL.getId(),
            MKUAbilities.GREATER_SMITE.getId(),
            MKUAbilities.EMPOWERED_SMITE.getId(),
            MKUAbilities.HOLY_WORD_BURST.getId(),
            MKUAbilities.HOLY_WORD_SHOTGUN.getId(),
            MKUAbilities.SHADOW_BOLT_DUAL_SHOTGUN.getId(),
            MKUAbilities.FIREBALL_BURST.getId()
    );

    public MKUAbilityProvider(DataGenerator generator) {
        super(generator, MKUltra.MODID);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        MKCoreRegistry.ABILITIES.entrySet().stream()
                .filter(entry -> entry.getKey().location().getNamespace().equals(getModId()))
                .filter(entry -> !VARIANT_IDS.contains(entry.getKey().location()))
                .forEach(entry -> futures.add(writeAbility(entry.getKey().location(), entry.getValue(), pOutput)));

        futures.add(writeVariant(MKUAbilities.GREATER_HEAL.getId(), MKUAbilities.HEAL.get(),
                AbilityVariantPatch.builder()
                        .cooldown(160)
                        .manaCost(6.0f)
                        .mergeFormulaParameter(HealAbility.BASE_PARAMETER, 8.0f)
                        .mergeFormulaParameter(HealAbility.PER_LEVEL_PARAMETER, 7.0f)
                        .mergeFormulaParameter(HealAbility.MODIFIER_SCALING_PARAMETER, 1.25f)
                        .build(),
                pOutput));
        futures.add(writeVariant(MKUAbilities.GREATER_SMITE.getId(), MKUAbilities.SMITE.get(),
                AbilityVariantPatch.builder()
                        .cooldown(160)
                        .manaCost(6.0f)
                        .mergeFormulaParameter(SmiteAbility.BASE_PARAMETER, 8.0f)
                        .mergeFormulaParameter(SmiteAbility.PER_LEVEL_PARAMETER, 6.0f)
                        .mergeFormulaParameter(SmiteAbility.MODIFIER_SCALING_PARAMETER, 1.25f)
                        .build(),
                pOutput));
        futures.add(writeVariant(MKUAbilities.EMPOWERED_SMITE.getId(), MKUAbilities.SMITE.get(),
                AbilityVariantPatch.builder()
                        .cooldown(180)
                        .manaCost(7.0f)
                        .mergeFormulaParameter(SmiteAbility.BASE_PARAMETER, 10.0f)
                        .mergeFormulaParameter(SmiteAbility.PER_LEVEL_PARAMETER, 7.0f)
                        .mergeFormulaParameter(SmiteAbility.MODIFIER_SCALING_PARAMETER, 1.5f)
                        .mergeFormulaParameter(SmiteAbility.DURATION_BASE_PARAMETER, 1.0f)
                        .mergeFormulaParameter(SmiteAbility.DURATION_PER_LEVEL_PARAMETER, 1.0f)
                        .build(),
                pOutput));
        futures.add(writeVariant(MKUAbilities.HOLY_WORD_BURST.getId(), MKUAbilities.HOLY_WORD.get(),
                circularProjectileVariant(new BurstProjectileBehavior(
                        circularLocation(1.2f, 12, 60.0f, -60.0f), true))
                        .castTime(80)
                        .build(),
                pOutput));
        futures.add(writeVariant(MKUAbilities.HOLY_WORD_SHOTGUN.getId(), MKUAbilities.HOLY_WORD.get(),
                circularProjectileVariant(new SimpleProjectileBehavior(
                        circularLocation(1.2f, 4, 20.0f, -20.0f), true))
                        .build(),
                pOutput));
        futures.add(writeVariant(MKUAbilities.SHADOW_BOLT_DUAL_SHOTGUN.getId(), MKUAbilities.SHADOW_BOLT.get(),
                circularProjectileVariant(new SimpleProjectileBehavior(
                        circularLocation(0.6f, 2, 1.0f, -1.0f), true))
                        .build(),
                pOutput));
        futures.add(writeVariant(MKUAbilities.FIREBALL_BURST.getId(), MKUAbilities.FIREBALL.get(),
                circularProjectileVariant(new BurstProjectileBehavior(
                        circularLocation(1.2f, 4, 0.0f, 0.0f), true))
                        .castTime(80)
                        .build(),
                pOutput));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private static AbilityVariantPatch.Builder circularProjectileVariant(ProjectileCastBehavior behavior) {
        return AbilityVariantPatch.builder()
                .replaceAttribute("npc_solve_ballistics", 1)
                .replaceAttribute("cast_behavior", ProjectileCastBehavior.CODEC, behavior);
    }

    private static CircularLocationProvider circularLocation(float percentEyeHeight,
                                                             int count,
                                                             float minDegrees,
                                                             float maxDegrees) {
        return new CircularLocationProvider(Vec3.ZERO, percentEyeHeight, count, 1.0f,
                minDegrees, maxDegrees, true);
    }
}
