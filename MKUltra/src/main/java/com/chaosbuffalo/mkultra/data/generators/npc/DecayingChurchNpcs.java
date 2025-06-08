package com.chaosbuffalo.mkultra.data.generators.npc;


import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkfaction.init.MKFactions;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.providers.NpcDefinitionProvider;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.entity.boss.BossStage;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import com.chaosbuffalo.mkultra.init.*;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import net.minecraft.data.CachedOutput;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.concurrent.CompletableFuture;


public class DecayingChurchNpcs {

    public static CompletableFuture<?> writeDefinitions(NpcDefinitionProvider provider, CachedOutput cache) {
        return CompletableFuture.allOf(
                provider.writeDefinition(DecayingChurchNpcs.generateAncientPriestGhost(), cache),
                provider.writeDefinition(DecayingChurchNpcs.generateAncientCardinal(), cache),
                provider.writeDefinition(DecayingChurchNpcs.generateGhostApprentice(), cache)
        );
    }

    static NpcDefinition generateGhostApprentice() {
        return new NpcDefinitionBuilder(MKUltra.id("ancient_apprentice_ghost"))
                .type(MKUEntities.HUMAN_GHOST_TYPE)
                .faction(MKUFactions.GHOSTS_OF_HYBORIA_NAME)
                .size(0.92f)
                .renderGroup(MKUHumans.GHOST_LOOK_CLEAN_SHORT_NAME)
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

    static NpcDefinition generateAncientPriestGhost() {
        return new NpcDefinitionBuilder(MKUltra.id("ancient_priest_ghost"))
                .type(MKUEntities.HUMAN_GHOST_TYPE)
                .faction(MKFactions.UNDEAD)
                .size(1.0f)
                .renderGroup(MKUHumans.GHOST_LOOK_CLEAN_NAME)
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
                .build();
    }

    static NpcDefinition generateAncientCardinal() {
        return new NpcDefinitionBuilder(MKUltra.id("ancient_cardinal"))
                .type(MKUEntities.HUMAN_GHOST_TYPE)
                .faction(MKFactions.UNDEAD)
                .size(1.2f)
                .renderGroup(MKUHumans.GHOST_LOOK_CLEAN_NAME)
                .attribute(Attributes.MAX_HEALTH, 350.0)
                .attribute(Attributes.ARMOR, 20.0)
                .attribute(Attributes.ATTACK_DAMAGE, 6.0)
                .attribute(MKAttributes.HOLY_RESISTANCE, 1.25)
                .attribute(MKAttributes.MAX_MANA, 350.0)
                .attribute(MKAttributes.MANA_REGEN, 6.0)
                .name("Ancient Cardinal")
                .mainHand(MKWeaponsItems.lookupMelee(MKWeaponsItems.GOLD_TIER, MeleeWeaponTypes.WARHAMMER_TYPE).orElseThrow())
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
                .xp(75)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .build();
    }
}
