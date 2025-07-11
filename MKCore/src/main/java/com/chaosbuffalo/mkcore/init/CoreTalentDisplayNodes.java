package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.talents.TalentNodeDisplay;
import com.chaosbuffalo.mkcore.data.providers.TalentNodeDisplayProvider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CoreTalentDisplayNodes extends TalentNodeDisplayProvider {
    public CoreTalentDisplayNodes(PackOutput output, CompletableFuture<RegistrySetBuilder.PatchedRegistries> registries) {
        super(output, registries, Set.of(MKCore.MOD_ID));
    }

    private static ResourceKey<TalentNodeDisplay> key(String name) {
        return ResourceKey.create(MKCoreRegistry.TALENT_NODE_DISPLAY_REGISTRY_KEY, MKCore.id(name));
    }

    public static final ResourceKey<TalentNodeDisplay> MAX_HEALTH = key("max_health");
    public static final ResourceKey<TalentNodeDisplay> ARMOR = key("armor");
    public static final ResourceKey<TalentNodeDisplay> MOVEMENT_SPEED = key("movement_speed");
    public static final ResourceKey<TalentNodeDisplay> ATTACK_DAMAGE = key("attack_damage");
    public static final ResourceKey<TalentNodeDisplay> MAX_MANA = key("max_mana");
    public static final ResourceKey<TalentNodeDisplay> MANA_REGEN = key("mana_regen");
    public static final ResourceKey<TalentNodeDisplay> MELEE_CRIT = key("melee_crit");
    public static final ResourceKey<TalentNodeDisplay> MELEE_CRIT_MULTIPLIER = key("melee_crit_multiplier");
    public static final ResourceKey<TalentNodeDisplay> SPELL_CRIT = key("spell_crit");
    public static final ResourceKey<TalentNodeDisplay> SPELL_CRIT_MULTIPLIER = key("spell_crit_multiplier");
    public static final ResourceKey<TalentNodeDisplay> COOLDOWN_REDUCTION = key("cooldown_reduction");

    public static final ResourceKey<TalentNodeDisplay> HEAL_BONUS = key("heal_bonus");
    public static final ResourceKey<TalentNodeDisplay> MAX_POISE = key("max_poise");
    public static final ResourceKey<TalentNodeDisplay> POISE_REGEN = key("poise_regen");

    public static final ResourceKey<TalentNodeDisplay> BLOCK_EFFICIENCY = key("block_efficiency");
    public static final ResourceKey<TalentNodeDisplay> POISE_BREAK_CD = key("poise_break_cd");
    public static final ResourceKey<TalentNodeDisplay> HEAL_EFFICIENCY = key("heal_efficiency");

    public static final ResourceKey<TalentNodeDisplay> ARCANE_DAMAGE = key("arcane_damage");
    public static final ResourceKey<TalentNodeDisplay> FIRE_DAMAGE = key("fire_damage");
    public static final ResourceKey<TalentNodeDisplay> FROST_DAMAGE = key("frost_damage");
    public static final ResourceKey<TalentNodeDisplay> NATURE_DAMAGE = key("nature_damage");
    public static final ResourceKey<TalentNodeDisplay> SHADOW_DAMAGE = key("shadow_damage");
    public static final ResourceKey<TalentNodeDisplay> HOLY_DAMAGE = key("holy_damage");
    public static final ResourceKey<TalentNodeDisplay> POISON_DAMAGE = key("poison_damage");
    public static final ResourceKey<TalentNodeDisplay> BLEED_DAMAGE = key("bleed_damage");
    public static final ResourceKey<TalentNodeDisplay> RANGED_DAMAGE = key("ranged_damage");

    public static final ResourceKey<TalentNodeDisplay> ARCANE_RESISTANCE = key("arcane_resistance");
    public static final ResourceKey<TalentNodeDisplay> FIRE_RESISTANCE = key("fire_resistance");
    public static final ResourceKey<TalentNodeDisplay> FROST_RESISTANCE = key("frost_resistance");
    public static final ResourceKey<TalentNodeDisplay> NATURE_RESISTANCE = key("nature_resistance");
    public static final ResourceKey<TalentNodeDisplay> SHADOW_RESISTANCE = key("shadow_resistance");
    public static final ResourceKey<TalentNodeDisplay> HOLY_RESISTANCE = key("holy_resistance");
    public static final ResourceKey<TalentNodeDisplay> POISON_RESISTANCE = key("poison_resistance");
    public static final ResourceKey<TalentNodeDisplay> BLEED_RESISTANCE = key("bleed_resistance");
    public static final ResourceKey<TalentNodeDisplay> RANGED_RESISTANCE = key("ranged_resistance");

    public static final ResourceKey<TalentNodeDisplay> HEALTH_REGEN = key("health_regen");

    public static final ResourceKey<TalentNodeDisplay> ABILITY_SLOT = key("ability_slot");
    public static final ResourceKey<TalentNodeDisplay> PASSIVE_ABILITY_SLOT = key("passive_ability_slot");
    public static final ResourceKey<TalentNodeDisplay> ULTIMATE_ABILITY_SLOT = key("ultimate_ability_slot");
    public static final ResourceKey<TalentNodeDisplay> POOL_COUNT = key("pool_count");


    public static void bootstrap(BootstrapContext<TalentNodeDisplay> context) {
        createDefault(context, MAX_HEALTH);
        createDefault(context, ARMOR);
        createDefault(context, MOVEMENT_SPEED);
        createDefault(context, ATTACK_DAMAGE);
        createDefault(context, MAX_MANA);
        createDefault(context, MANA_REGEN);
        createDefault(context, MELEE_CRIT);
        createDefault(context, MELEE_CRIT_MULTIPLIER);
        createDefault(context, SPELL_CRIT);
        createDefault(context, SPELL_CRIT_MULTIPLIER);
        createDefault(context, COOLDOWN_REDUCTION);
        createDefault(context, HEAL_BONUS);
        createDefault(context, MAX_POISE);
        createDefault(context, POISE_REGEN);
        createDefault(context, BLOCK_EFFICIENCY);
        createDefault(context, POISE_BREAK_CD);
        createDefault(context, HEAL_EFFICIENCY);

        createDefault(context, ARCANE_DAMAGE);
        createDefault(context, ARCANE_RESISTANCE);
        createDefault(context, FIRE_DAMAGE);
        createDefault(context, FIRE_RESISTANCE);
        createDefault(context, FROST_DAMAGE);
        createDefault(context, FROST_RESISTANCE);
        createDefault(context, NATURE_DAMAGE);
        createDefault(context, NATURE_RESISTANCE);
        createDefault(context, HOLY_DAMAGE);
        createDefault(context, HOLY_RESISTANCE);
        createDefault(context, SHADOW_DAMAGE);
        createDefault(context, SHADOW_RESISTANCE);
        createDefault(context, POISON_DAMAGE);
        createDefault(context, POISON_RESISTANCE);
        createDefault(context, BLEED_DAMAGE);
        createDefault(context, BLEED_RESISTANCE);
        createDefault(context, RANGED_DAMAGE);
        createDefault(context, RANGED_RESISTANCE);

        createDefault(context, HEALTH_REGEN);

        createDefault(context, ABILITY_SLOT);
        createDefault(context, PASSIVE_ABILITY_SLOT);
        createDefault(context, ULTIMATE_ABILITY_SLOT);
        createDefault(context, POOL_COUNT);
    }

}
