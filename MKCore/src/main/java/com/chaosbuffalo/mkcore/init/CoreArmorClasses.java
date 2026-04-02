package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CoreArmorClasses {

    public static final ResourceKey<ArmorClass> ROBES_ARMOR = key("armor_class.robes");
    public static final ResourceKey<ArmorClass> LIGHT_ARMOR = key("armor_class.light");
    public static final ResourceKey<ArmorClass> MEDIUM_ARMOR = key("armor_class.medium");
    public static final ResourceKey<ArmorClass> HEAVY_ARMOR = key("armor_class.heavy");

    private static ResourceKey<ArmorClass> key(String name) {
        return ResourceKey.create(MKCoreRegistry.ARMOR_CLASS_REGISTRY_KEY, MKCore.id(name));
    }

    public static void bootstrap(BootstrapContext<ArmorClass> context) {

        var robes = new ArmorClass.Builder(Component.translatable("mkcore.armor_class.robes.name"))
                .addPositiveEffect(Attributes.MOVEMENT_SPEED, 0.025, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addPositiveEffect(MKAttributes.CASTING_SPEED, 0.025, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addPositiveEffect(MKAttributes.MANA_REGEN, 0.025, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addNegativeEffect(Attributes.ARMOR, -0.04, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addNegativeEffect(Attributes.MAX_HEALTH, -0.02, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        context.register(ROBES_ARMOR, robes.build());


        var light = new ArmorClass.Builder(Component.translatable("mkcore.armor_class.light.name"))
                .addPositiveEffect(MKAttributes.MELEE_CRIT, 0.02, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addPositiveEffect(MKAttributes.SPELL_CRIT, 0.02, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addPositiveEffect(MKAttributes.RANGED_CRIT, 0.02, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addPositiveEffect(MKAttributes.COOLDOWN, 0.02, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addNegativeEffect(MKAttributes.CASTING_SPEED, -0.02, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addNegativeEffect(Attributes.MAX_HEALTH, -0.04, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        context.register(LIGHT_ARMOR, light.build());


        var medium = new ArmorClass.Builder(Component.translatable("mkcore.armor_class.medium.name"))
                .addPositiveEffect(MKAttributes.HEAL_BONUS, 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addPositiveEffect(Attributes.ATTACK_SPEED, 0.03, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addPositiveEffect(Attributes.MAX_HEALTH, 0.02, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addNegativeEffect(MKAttributes.COOLDOWN, -0.02, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addNegativeEffect(MKAttributes.CASTING_SPEED, -0.03, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        context.register(MEDIUM_ARMOR, medium.build());


        var heavy = new ArmorClass.Builder(Component.translatable("mkcore.armor_class.heavy.name"))
                .addPositiveEffect(Attributes.ATTACK_DAMAGE, 0.025, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addPositiveEffect(Attributes.MAX_HEALTH, 0.04, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addPositiveEffect(MKAttributes.MAX_POISE, 0.03, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addPositiveEffect(MKAttributes.ARCANE_RESISTANCE, 0.015, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addPositiveEffect(MKAttributes.FIRE_RESISTANCE, 0.015, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addPositiveEffect(MKAttributes.FROST_RESISTANCE, 0.015, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addPositiveEffect(MKAttributes.NATURE_RESISTANCE, 0.015, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addPositiveEffect(MKAttributes.POISON_RESISTANCE, 0.015, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addPositiveEffect(Attributes.ARMOR_TOUGHNESS, 0.025, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addNegativeEffect(Attributes.MOVEMENT_SPEED, -0.025, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addNegativeEffect(MKAttributes.COOLDOWN, -0.04, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addNegativeEffect(MKAttributes.CASTING_SPEED, -0.04, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
                .addNegativeEffect(Attributes.ATTACK_SPEED, -0.025, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        context.register(HEAVY_ARMOR, heavy.build());
    }
}
