package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.init.MKNpcEntityTypes;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mkultra.init.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class FireShrineNpcs {

    public static final ResourceKey<NpcDefinition> fire_sprite = MKUNpcs.key("fire_sprite");
    public static final ResourceKey<NpcDefinition> flaming_skull = MKUNpcs.key("flaming_skull");
    public static final ResourceKey<NpcDefinition> cinder_herald = MKUNpcs.key("cinder_herald");


    public static void bootstrap(BootstrapContext<NpcDefinition> context) {
        context.register(fire_sprite, generateFireSprite(fire_sprite, context));
        context.register(flaming_skull, generateFlamingSkull(flaming_skull, context));
        context.register(cinder_herald, generateCinderHerald(cinder_herald, context));
        context.register(MKUNpcs.key("cinder_herald_2"), generateCinderHerald2(MKUNpcs.key("cinder_herald_2"), context));
    }


    static NpcDefinition generateFireSprite(ResourceKey<NpcDefinition> key, BootstrapContext<NpcDefinition> context) {
        return new NpcDefinitionBuilder(key, MKNpcEntityTypes.BLAZE_TYPE)
                .faction(MKUFactions.FIRE_SHRINE_GUARDIANS_NAME)
                .size(0.9f)
                .name("Fire Sprite")
                .health(50.0)
                .mana(50.0)
                .manaRegen(5.0)
                .ability(MKUAbilities.FIREBALL, 1, 1.0)
                .ability(MKUAbilities.FIREBALL_BURST, 2, 0.3)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .xp(25)
                .build();
    }

    static NpcDefinition generateFlamingSkull(ResourceKey<NpcDefinition> key, BootstrapContext<NpcDefinition> context) {
        return new NpcDefinitionBuilder(key, MKNpcEntityTypes.FLYING_SKULL_TYPE)
                .faction(MKUFactions.FIRE_SHRINE_GUARDIANS_NAME)
                .size(1.0f)
                .name("Flaming Skull")
                .health(35.0)
                .mana(40.0)
                .manaRegen(4.0)
                .ability(MKUAbilities.SKULL_FLAME_BREATH, 1, 1.0)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .xp(20)
                .build();
    }

    static NpcDefinition generateCinderHerald(ResourceKey<NpcDefinition> key, BootstrapContext<NpcDefinition> context) {
        return new NpcDefinitionBuilder(key, MKNpcEntityTypes.FIRE_ELEMENTAL_TYPE)
                .faction(MKUFactions.FIRE_SHRINE_GUARDIANS_NAME)
                .size(1.5f)
                .name("Cinder Herald")
                .health(220.0)
                .mana(160.0)
                .manaRegen(5.0)
                .ability(MKUAbilities.SEARING_FURROW, 1, 1.0)
                .mainHand(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("mkweapons:battleaxe_stone")).get())
//                .ability(MKUAbilities.FIRE_ARMOR, 2, 0.5)
//                .ability(MKUAbilities.FLAME_WAVE, 3, 0.5)
                .notable()
                .xp(75)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .build();
    }

    static NpcDefinition generateCinderHerald2(ResourceKey<NpcDefinition> key, BootstrapContext<NpcDefinition> context) {
        return new NpcDefinitionBuilder(key, MKNpcEntityTypes.FIRE_ELEMENTAL_TYPE)
                .faction(MKUFactions.FIRE_SHRINE_GUARDIANS_NAME)
                .size(1.0f)
                .name("Cinder Herald 2")
                .health(220.0)
                .mana(160.0)
                .manaRegen(5.0)
                .ability(MKUAbilities.SEARING_FURROW, 1, 1.0)
                .mainHand(BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse("mkweapons:battleaxe_stone")).get())
//                .ability(MKUAbilities.FIRE_ARMOR, 2, 0.5)
//                .ability(MKUAbilities.FLAME_WAVE, 3, 0.5)
                .notable()
                .xp(75)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .build();
    }
}
