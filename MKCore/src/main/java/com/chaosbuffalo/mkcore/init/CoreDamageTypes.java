package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.core.damage.MeleeDamageType;
import com.chaosbuffalo.mkcore.core.damage.RangedDamageType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;


public class CoreDamageTypes {

    private static final DeferredRegister<MKDamageType> REGISTRY =
            DeferredRegister.create(MKCoreRegistry.DAMAGE_TYPE_REGISTRY_NAME, MKCore.MOD_ID);

    public static final ResourceKey<DamageType> MK_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            new ResourceLocation(MKCore.MOD_ID, "mk_damage"));

    public static final RegistryObject<MKDamageType> FireDamage = REGISTRY.register("fire",
            () -> new MKDamageType(MKAttributes.FIRE_DAMAGE, MKAttributes.FIRE_RESISTANCE,
                    MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
                    ChatFormatting.RED));

    public static final RegistryObject<MKDamageType> FrostDamage = REGISTRY.register("frost",
            () -> new MKDamageType(MKAttributes.FROST_DAMAGE, MKAttributes.FROST_RESISTANCE,
                    MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
                    ChatFormatting.AQUA));

    public static final RegistryObject<MKDamageType> HolyDamage = REGISTRY.register("holy",
            () -> new MKDamageType(MKAttributes.HOLY_DAMAGE, MKAttributes.HOLY_RESISTANCE,
                    MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
                    ChatFormatting.GOLD).setCritMultiplier(2.0f));

    public static final RegistryObject<MKDamageType> PoisonDamage = REGISTRY.register("poison",
            () -> new MKDamageType(MKAttributes.POISON_DAMAGE, MKAttributes.POISON_RESISTANCE,
                    MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
                    ChatFormatting.GREEN));

    public static final RegistryObject<MKDamageType> ShadowDamage = REGISTRY.register("shadow",
            () -> new MKDamageType(MKAttributes.SHADOW_DAMAGE, MKAttributes.SHADOW_RESISTANCE,
                    MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
                    ChatFormatting.DARK_PURPLE));

    public static final RegistryObject<MKDamageType> ArcaneDamage = REGISTRY.register("arcane",
            () -> new MKDamageType(MKAttributes.ARCANE_DAMAGE, MKAttributes.ARCANE_RESISTANCE,
                    MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
                    ChatFormatting.LIGHT_PURPLE));

    public static final RegistryObject<MKDamageType> NatureDamage = REGISTRY.register("nature",
            () -> new MKDamageType(MKAttributes.NATURE_DAMAGE, MKAttributes.NATURE_RESISTANCE,
                    MKAttributes.SPELL_CRIT, MKAttributes.SPELL_CRIT_MULTIPLIER,
                    ChatFormatting.DARK_GREEN));

    public static final RegistryObject<MKDamageType> MeleeDamage = REGISTRY.register("melee",
            MeleeDamageType::new);

    public static final RegistryObject<RangedDamageType> RangedDamage = REGISTRY.register("ranged",
            RangedDamageType::new);

    public static final RegistryObject<MKDamageType> BleedDamage = REGISTRY.register("bleed",
            () -> new MKDamageType(MKAttributes.BLEED_DAMAGE, MKAttributes.BLEED_RESISTANCE,
                    MKAttributes.MELEE_CRIT, MKAttributes.MELEE_CRIT_MULTIPLIER,
                    ChatFormatting.DARK_RED));


    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
