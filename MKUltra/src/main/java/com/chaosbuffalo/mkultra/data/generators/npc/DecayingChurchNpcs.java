package com.chaosbuffalo.mkultra.data.generators.npc;


import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkfaction.init.MKFactions;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.entity.boss.BossStage;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.options.TempAbilitiesOption;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import com.chaosbuffalo.mkultra.init.*;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.Attributes;


public class DecayingChurchNpcs {

    public static final ResourceKey<NpcDefinition> ancient_apprentice_ghost = MKUNpcs.key("ancient_apprentice_ghost");
    public static final ResourceKey<NpcDefinition> ancient_priest_ghost = MKUNpcs.key("ancient_priest_ghost");
    public static final ResourceKey<NpcDefinition> ancient_cardinal = MKUNpcs.key("ancient_cardinal");


    public static void bootstrap(BootstrapContext<NpcDefinition> context) {
        context.register(ancient_apprentice_ghost, generateGhostApprentice(ancient_apprentice_ghost));
        context.register(ancient_priest_ghost, generateAncientPriestGhost(ancient_priest_ghost));
        context.register(ancient_cardinal, generateAncientCardinal(ancient_cardinal));
    }


    static NpcDefinition generateGhostApprentice(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HUMAN_GHOST_TYPE)
                .faction(MKUFactions.GHOSTS_OF_HYBORIA_NAME)
                .size(0.92f)
                .renderGroup(MKUHumans.GHOST_ENTITY_GHOST_CLEAN_SHORT_LOOK)
                .attribute(Attributes.MAX_HEALTH, 100.0)
                .attribute(MKAttributes.MAX_MANA, 100.0)
                .attribute(MKAttributes.MANA_REGEN, 3.0)
                .titledFactionName("Apprentice", false)
                .ghost(0.7f)
                .ability(MKUAbilities.HEAL, 1, 1.0)
                .ability(MKUAbilities.SMITE, 2, 1.0)
                .notable()
                .dropChance(0.05f)
                .chestplate(MKUItems.ancientPriestChestplate)
                .leggings(MKUItems.ancientPriestLeggings)
                .boots(MKUItems.ancientPriestBoots)
                .skillClass(NpcGenUtils.NpcSkillClass.PALADIN)
                .build();
    }

    static NpcDefinition generateAncientPriestGhost(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HUMAN_GHOST_TYPE)
                .faction(MKFactions.UNDEAD)
                .size(1.0f)
                .renderGroup(MKUHumans.GHOST_ENTITY_GHOST_CLEAN_LOOK)
                .attribute(Attributes.MAX_HEALTH, 100.0)
                .attribute(MKAttributes.MAX_MANA, 100.0)
                .attribute(MKAttributes.MANA_REGEN, 3.0)
                .name("An Ancient Priest")
                .ghost(0.7f)
                .ability(MKUAbilities.HEAL, 1, 1.0)
                .ability(MKUAbilities.HOLY_WORD, 2, 1.0)
                .ability(MKUAbilities.HOLY_FIRE, 3, 0.5)
                .dropChance(0.05f)
                .helmet(MKUItems.ancientPriestHelmet)
                .chestplate(MKUItems.ancientPriestChestplate)
                .leggings(MKUItems.ancientPriestLeggings)
                .boots(MKUItems.ancientPriestBoots)
                .skillClass(NpcGenUtils.NpcSkillClass.CLERIC)
                .xp(35)
                .build();
    }

    static NpcDefinition generateAncientCardinal(ResourceKey<NpcDefinition> key) {
        return new NpcDefinitionBuilder(key, MKUEntities.HUMAN_GHOST_TYPE)
                .faction(MKFactions.UNDEAD)
                .size(1.2f)
                .renderGroup(MKUHumans.GHOST_ENTITY_GHOST_CLEAN_LOOK)
                .attribute(Attributes.MAX_HEALTH, 350.0)
                .attribute(Attributes.ARMOR, 20.0)
                .attribute(Attributes.ATTACK_DAMAGE, 6.0)
                .attribute(MKAttributes.HOLY_RESISTANCE, 1.25)
                .attribute(MKAttributes.MAX_MANA, 350.0)
                .attribute(MKAttributes.MANA_REGEN, 6.0)
                .name("Ancient Cardinal")
                .mainHand(MKWeaponsItems.lookupWeapon(MKWeaponsItems.GOLD_TIER, MeleeWeaponTypes.WARHAMMER_TYPE))
                .dropChance(0.15f)
                .helmet(MKUItems.ancientCardinalHelmet)
                .chestplate(MKUItems.ancientCardinalChestplate)
                .leggings(MKUItems.ancientCardinalLeggings)
                .boots(MKUItems.ancientCardinalBoots)
                .notable()
                .bossStage(new BossStage()
                        .withOption(new TempAbilitiesOption()
                                .withAbilityOption(MKUAbilities.HOLY_FIRE.get(), 1, 1.0)
                                .withAbilityOption(MKUAbilities.HOLY_WORD_SHOTGUN.get(), 2, 1.0)
                                .withAbilityOption(MKUAbilities.HOLY_WORD.get(), 3, 1.0))
                )
                .bossStage(new BossStage()
                        .withOption(new TempAbilitiesOption()
                                .withAbilityOption(MKUAbilities.HOLY_FIRE_FLURRY.get(), 1, 1.0)
                                .withAbilityOption(MKUAbilities.HOLY_FIRE.get(), 2, 1.0)
                                .withAbilityOption(MKUAbilities.HOLY_WORD_BURST.get(), 3, 1.0)
                                .withAbilityOption(MKUAbilities.HOLY_WORD_SHOTGUN.get(), 4, 1.0)
                                .withAbilityOption(MKUAbilities.HOLY_WORD.get(), 5, 1.0))
                        .withParticleMode(BossStage.ParticleMode.LINE_HEIGHT)
                        .withTransitionParticles(MKUltra.id("wrath_skeleton_transition"))
                        .withTransitionSound(MKUSounds.spell_holy_9.getId())
                )
                .xp(150)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .build();
    }
}
