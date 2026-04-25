package com.chaosbuffalo.mkcore.data.content;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.data.providers.MKLanguageProvider;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.init.CoreEntitlements;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Locale;

class CoreLanguageProvider extends MKLanguageProvider {

    public CoreLanguageProvider(PackOutput output, String locale) {
        super(output, MKCore.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        addLegacy();
        addGui();
        addParticles();
        addCommands();
        addDamageTypes();
        addAttributes();
        addEntitlements();
        addTalents();
    }

    private void addEntitlements() {
        // Entitlement grant talents use the name and description here

        entitlement(CoreEntitlements.ABILITY_POOL_SIZE, "Ability Pool", "Expands your ability pool by 1");

        entitlement(CoreEntitlements.BASIC_ABILITY_SLOT, "Basic Ability Slot", "Adds a basic ability slot to your loadout");
        entitlement(CoreEntitlements.PASSIVE_ABILITY_SLOT, "Passive Ability Slot", "Adds a passive ability slot to your loadout");
        entitlement(CoreEntitlements.ULTIMATE_ABILITY_SLOT, "Ultimate Ability Slot", "Adds an ultimate ability slot to your loadout");

        entitlement(CoreEntitlements.ROBE_ARMOR_MASTERY, "Robes Armor Mastery", "Allows using robes without penalties");
        entitlement(CoreEntitlements.LIGHT_ARMOR_MASTERY, "Light Armor Mastery", "Allows using light armor without penalties");
        entitlement(CoreEntitlements.MEDIUM_ARMOR_MASTERY, "Medium Armor Mastery", "Allows using medium armor without penalties");
        entitlement(CoreEntitlements.HEAVY_ARMOR_MASTERY, "Heavy Armor Mastery", "Allows using heavy without penalties");
    }

    private void addTalents() {
        add("talent_type.mkcore.ability_grant.basic.name", "Basic Ability Talent");
        add("talent_type.mkcore.ability_grant.passive.name", "Passive Ability Talent");
        add("talent_type.mkcore.ability_grant.ultimate.name", "Ultimate Ability Talent");
        add("talent_type.mkcore.ability_grant.description", "Grants the %s %s ability");
        add("talent_type.mkcore.entitlement_grant.name", "Entitlement Grant");
        add("talent_type.mkcore.attribute.name", "Attribute Bonus");

        attributeTalent(Attributes.MAX_HEALTH, "Max Health", "Increases your total health by %s");
        attributeTalent(Attributes.ARMOR, "Armor", "Increases your armor by %s");
        attributeTalent(Attributes.MOVEMENT_SPEED, "Movement Speed", "Increases your movement speed by %s");
        attributeTalent(Attributes.ATTACK_SPEED, "Attack Speed", "Increases your attack speed by %s");
        attributeTalent(Attributes.ATTACK_DAMAGE, "Attack Damage", "Increases your attack damage by %s");


        attributeTalent(MKAttributes.HEALTH_REGEN, "Health Regen", "Increases your health regen by %s");
        attributeTalent(MKAttributes.MAX_MANA, "Max Mana", "Increases your total mana by %s");
        attributeTalent(MKAttributes.MANA_REGEN, "Mana Regen", "Increases your mana regen by %s");
        attributeTalent(MKAttributes.MELEE_CRIT, "Melee Critical Chance", "Increases your melee critical chance by %s");
        attributeTalent(MKAttributes.MELEE_CRIT_MULTIPLIER, "Melee Critical Damage", "Increases your melee critical damage by %s");
        attributeTalent(MKAttributes.MULTI_ATTACK_CHANCE, "Multi Attack Chance", "Increases your chance to make extra melee attacks by %s");
        attributeTalent(MKAttributes.ARMOR_PIERCING, "Armor Piercing", "Increases the amount of armor your attacks ignore by %s");

        attributeTalent(MKAttributes.SPELL_CRIT, "Spell Critical Chance", "Increases your spell critical chance by %s");
        attributeTalent(MKAttributes.SPELL_CRIT_MULTIPLIER, "Spell Critical Damage", "Increases your spell critical damage by %s");


        attributeTalent(MKAttributes.COOLDOWN, "Cooldown Reduction", "Increases your cooldown reduction by %s");
        attributeTalent(MKAttributes.HEAL_BONUS, "Heal Bonus", "Increases your healing bonus by %s");
        attributeTalent(MKAttributes.HEAL_EFFICIENCY, "Heal Efficiency", "Increases your healing efficiency by %s");


        attributeTalent(MKAttributes.BLOCK_EFFICIENCY, "Block Efficiency", "Increases your block efficiency by %s");
        attributeTalent(MKAttributes.MAX_POISE, "Max Poise", "Increases your max poise by %s");
        attributeTalent(MKAttributes.POISE_REGEN, "Poise Regen", "Increases your poise regeneration by %s");
        attributeTalent(MKAttributes.POISE_BREAK_CD, "Poise Break Cooldown", "Decreases your poise break cooldown by %s seconds");

        damageAttrTalents(MKAttributes.RANGED_DAMAGE, MKAttributes.RANGED_RESISTANCE, "Ranged");

        damageAttrTalents(MKAttributes.ARCANE_DAMAGE, MKAttributes.ARCANE_RESISTANCE, "Arcane");
        damageAttrTalents(MKAttributes.FIRE_DAMAGE, MKAttributes.FIRE_RESISTANCE, "Fire");
        damageAttrTalents(MKAttributes.FROST_DAMAGE, MKAttributes.FROST_RESISTANCE, "Frost");
        damageAttrTalents(MKAttributes.SHADOW_DAMAGE, MKAttributes.SHADOW_RESISTANCE, "Shadow");
        damageAttrTalents(MKAttributes.HOLY_DAMAGE, MKAttributes.HOLY_RESISTANCE, "Holy");
        damageAttrTalents(MKAttributes.NATURE_DAMAGE, MKAttributes.NATURE_RESISTANCE, "Nature");

        damageAttrTalents(MKAttributes.POISON_DAMAGE, MKAttributes.POISON_RESISTANCE, "Poison");
        damageAttrTalents(MKAttributes.BLEED_DAMAGE, MKAttributes.BLEED_RESISTANCE, "Bleed");
    }

    protected void damageAttrTalents(Holder<Attribute> damageAttr, Holder<Attribute> resistAttr, String name) {
        String damageName = String.format("%s Damage", name);
        String damageDesc = String.format("Increases your %s damage by %%s", name.toLowerCase(Locale.ROOT));
        attributeTalent(damageAttr, damageName, damageDesc);

        String resistName = String.format("%s Resistance", name);
        String resistDesc = String.format("Increases your %s resistance by %%s", name.toLowerCase(Locale.ROOT));
        attributeTalent(resistAttr, resistName, resistDesc);
    }

    private void addGui() {
        add("mkcore.gui.character.stats", "Stats");
        add("mkcore.gui.character.damages", "Damages");
        add("mkcore.gui.character.abilities", "Abilities");
        add("mkcore.gui.character.talents", "Talents");
        add("mkcore.gui.passives", "Passives");
        add("mkcore.gui.ultimates", "Ultimates");
        add("mkcore.gui.actives", "Active");
        add("mkcore.gui.character.abilities.title", "Character");
        add("mkcore.gui.select_ability", "Select an Ability to inspect it.");
        add("mkcore.gui.select_talent_tree", "Select a Talent Tree to inspect it.");
        add("mkcore.gui.item.armor_class.name", "Armor Class");
        add("mkcore.gui.item.armor_class.effect_prompt", "Hold <shift> to see Armor Class Effects");
        add("mkcore.gui.item.armor_class.effect.name", "- Effect: ");
        add("mkcore.gui.character.persona_name", "Persona: %s");
        add("mkcore.gui.character.current_health", "Health: %s / %s");
        add("mkcore.gui.character.current_mana", "Mana: %s / %s");
        add("mkcore.gui.character.forget_ability", "Your memory is full, you must forget %d to learn %s.");
        add("mkcore.gui.character.already_known", "Already Known");
        add("mkcore.gui.character.unmet_req", "Unmet Requirement");
        add("mkcore.gui.character.can_learn", "Can Learn");
        add("mkcore.gui.character.learn", "Learn");
        add("mkcore.gui.character.unmet_req_tooltip", "You do not meet the requirements to learn this ability.");
        add("mkcore.gui.character.learn_ability_prompt", "Select an ability to learn.");
        add("mkcore.gui.xp_bar.name", "Next Talent:");
        add("mkcore.gui.character.forget_confirm", "Forget");
        add("mkcore.gui.character.forget", "Do you want to forget any known abilities?");
        add("mkcore.gui.manage_memory", "Manage");
        add("mkcore.gui.memory_pool", "%d/%d");
        add("mkcore.gui.memory_pool_tooltip", "Memory Pool Slot Usage");
        add("mkcore.ability.feedback.global_cooldown", "Abilities are not ready yet");
        add("mkcore.ability.feedback.stunned", "You are stunned");
        add("mkcore.ability.feedback.busy", "You are already casting");
        add("mkcore.ability.feedback.invalid_target", "No valid target for %s");
        add("mkcore.ability.feedback.not_enough_resource", "Not enough mana for %s");
        add("mkcore.ability.feedback.on_cooldown", "%s is not ready yet");
        add("mkcore.ability.feedback.unavailable", "%s cannot be used right now");
        add("mkcore.item_tooltip.grants_ability", "Grants Ability: %s");
        add("key.hud.active_ability1", "Ability Slot 1");
        add("key.hud.active_ability2", "Ability Slot 2");
        add("key.hud.active_ability3", "Ability Slot 3");
        add("key.hud.active_ability4", "Ability Slot 4");
        add("key.hud.active_ability5", "Ability Slot 5");
        add("key.hud.item_ability", "Item Ability");
        add("key.hud.ultimate_ability1", "Ultimate Slot 1");
        add("key.hud.ultimate_ability2", "Ultimate Slot 2");
        add("key.hud.particle_editor", "Open Particle Editor");
        add("key.hud.playermenu", "Open Character Sheet");
        add("key.core.abilitybar", "Ability Bar");
        add("key.mkcore.category", "MKCore");
        add("mkcore.configuration.showMyCrits", "Show My Crits");
        add("mkcore.configuration.showOthersCrits", "Show Other Crits");
        add("mkcore.configuration.enablePlayerCastAnimations", "Enable Player Cast Animations");
        add("mkcore.configuration.showArmorClassOnTooltip", "Show Armor Class On Tooltip");
        add("mkcore.configuration.showArmorClassEffectsOnTooltip", "Show Armor Class Effects On Tooltip");
        add("mkcore.configuration.disableAttackForFriend", "Disable Attack On Friendlies");
        add("mkcore.configuration.gameplay", "Gameplay");
        add("mkcore.configuration.general", "General");
        add("mkcore.configuration.healsDamageUndead", "Heals Hurt Undead");
        add("mkcore.configuration.undeadHealDamageMultiplier", "Undead Heal Damage Multiplier");
        add("mkcore.configuration.enablePartyXpShare", "Enable Party XP Share");
        add("mkcore.configuration.partyXpShareDistance", "XP Share Distance");
        add("mkcore.configuration.enablePartyXpShareMending", "Party XP Trigger Mending");
        add("mk.core.gui.party_invite.desc", "%s is inviting you to their party. Will you accept?");
        add("mk.core.gui.party_invite.accept", "Accept");
        add("mk.core.gui.party_invite.reject", "Reject");
        add("mk.core.party.invitee.text", "%s invited you to their party.");
        add("mk.core.party.inviter.text", "You invited %s to your party.");
        add("mk.core.gui.party_invite.title", "Party Invite");
        add("mk.core.party.invitee.decline.text", "You declined the party invite from %s.");
        add("mk.core.party.inviter.decline.text", "%s declined your party invite.");
        add("mk.core.party.invitee.accept.text", "You accepted the party invite from %s.");
        add("mk.core.party.inviter.accept.text", "%s accepted your party invite.");
        add("mk.core.party.name", "%s's Party");
        add("mk.core.party.info.name", "Party: %s");
        add("mk.core.party.info.members", "Members: %s");
        add("mk.core.party.info.none", "You are not in a party!");
        add("mk.core.party.invite_self", "You can't invite yourself to a party!");
        add("mkcore.configuration.skillScalingMultiplier", "Weapon Skill Scaling Multiplier");
        add("mkcore.configuration.difficultyBandIncrease", "Difficulty Band Increase");
        add("mkcore.configuration.worldDifficultyBandSize", "World Difficulty Band Size");
        add("mkcore.configuration.maxTalentPoints", "Max Talent Points");
        add("mkcore.configuration.talentPointsPerSkill", "Talent Points Per Skill Tier");
        add("mkcore.configuration.baseXpPerTalentPoint", "Base XP Per Talent Point");
        add("mkcore.configuration.totalTalentXpMultiplier", "Total Talent Multiplier");
        add("mkcore.configuration.scalingXpPerTalentPoint", "Scaling XP Per Talent Point");
        add("mkcore.ui.search", "Search");
    }

    private void addCommands() {
        add("mkcore.command.ability.unlearn.not_known", "Player '%s' doesn't know ability %s");
        add("mkcore.command.ability.learn.success", "Player '%s' learned ability %s");
    }

    private void addLegacy() {
        add("mk.editors.particle_editor.name", "Particle Editor");
        add("mkcore.talent_tree.knight.name", "Knight Tree");
        add("mkcore.ability.description.cast_time", "Cast Time: %s");
        add("mkcore.ability.description.cooldown", "Cooldown: %s seconds");
        add("mkcore.ability.description.mana_cost", "Mana Cost: %s");
        add("mkcore.ability.description.instant", "Instant");
        add("mkcore.ability.description.seconds", "%s seconds");
        add("mkcore.ability.description.passive", "Passive");
        add("mkcore.ability.description.range", "Range: %s");
        add("mkcore.ability_target.single_target", "Single Target");
        add("mkcore.ability_target.single_target_self", "Single Target or Self");
        add("mkcore.ability_target.self", "Self");
        add("mkcore.ability_target.none", "None");
        add("mkcore.ability_description.target", "Targeting: %s");
        add("mkcore.ability_description.target_with_type", "Targeting: %s (%s)");
        add("mkcore.ability_description.target_type", "Target Type: %s");
        add("mkcore.ability_target.pbaoe", "PBAoE");
        add("mkcore.ability_target.projectile", "Projectile");
        add("mkcore.ability_target.line", "Line");
        add("mkcore.ability.description.effect_with_name", "%s Modifiers:");
        add("mkcore.ability.description.effect", "Modifiers:");
        add("mkcore.ability.description.skill", "Skill: %s");
        add("mkcore.armor_class.light.name", "Light");
        add("mkcore.armor_class.medium.name", "Medium");
        add("mkcore.armor_class.heavy.name", "Heavy");
        add("mkcore.armor_class.robes.name", "Robes");
        add("mkcore.crit.melee.self", "You just crit %s with %s for %s");
        add("mkcore.crit.melee.other", "%s just crit %s with %s for %s");
        add("mkcore.crit.effect.self", "Your %s just crit %s for %s");
        add("mkcore.crit.effect.other", "%s's %s just crit %s for %s");
        add("mkcore.crit.projectile.self", "You just crit %s with %s for %s");
        add("mkcore.crit.projectile.other", "%s just crit %s with %s for %s");
        add("mkcore.crit.ability.self", "Your %s spell just crit %s for %s");
        add("mkcore.crit.ability.other", "%s's %s spell just crit %s for %s");
        add("effect.mkcore.effect.stun", "Stun");
        add("mkcore.subtitle.casting_default", "The sound of spell casting");
        add("mkcore.subtitle.spell_cast", "The sound of a spell cast going off");
        add("mkcore.ability.description.per_level", "per skill level");
        add("mkcore.particle_editor.track_type.color", "Color Track: %s");
        add("mkcore.particle_editor.track_type.scale", "Scale Track: %s");
        add("mkcore.particle_editor.track_type.motion", "Motion Track: %s");
        add("mkcore.particle_editor.track_type.unknown", "Unknown Track: %s");
        add("mkcore.particle_editor.add_track", "Add Track");
        add("mkcore.particle_editor.delete_track", "Delete");
        add("mkcore.particle_editor.choose_track", "Choose Animation Track");
        add("mkcore.particle_editor.choose_spawn_pattern", "Choose Spawn Pattern");
        add("mkcore.particle_editor.prompt_save", "Enter save name:");
        add("mkcore.particle_editor.choose_particle_type", "Choose Particle Type:");
        add("mkcore.spawn_pattern.particle_spawn_pattern.circle.name", "Circular");
        add("mkcore.spawn_pattern.particle_spawn_pattern.sphere.name", "Spherical");
        add("mkcore.spawn_pattern.particle_spawn_pattern.single.name", "Single Point");
        add("mkcore.spawn_pattern.particle_spawn_pattern.pillar.name", "Pillar");
        add("mkcore.spawn_pattern.particle_spawn_pattern.spiral.name", "Spiral");
        add("mkcore.spawn_pattern.particle_spawn_pattern.line.name", "Line");
        add("mkcore.particle_editor.set_spawn_pattern", "Set Spawn Pattern");
        add("mkcore.anim_track.particle_anim.orbit_in_plane.name", "Orbit In Plane");
        add("mkcore.anim_track.particle_anim.brownian_motion.name", "Brownian");
        add("mkcore.anim_track.particle_anim.particle_motion.name", "Inherit");
        add("mkcore.anim_track.particle_anim.linear_motion.name", "Linear");
        add("mkcore.anim_track.particle_anim.render_scale.name", "Render Scale");
        add("mkcore.anim_track.particle_anim.lerp_color.name", "Lerp Color");
        add("mkcore.anim_track.particle_anim.static_color.name", "Static Color");
        add("mkcore.subtitle.level_up", "An aetherial ding echoes in the area");
        add("mkcore.ability.description.uses_pool", "Using Memory Slot");
        add("mkcore.block_efficiency.description", "Block Efficiency: %.2f%%");
        add("mkcore.max_poise.description", "Max Poise: %.0f");
        add("mkcore.subtitle.block_break", "The sound of someone's block breaking");
        add("mkcore.subtitle.fist_block", "The sound of fist on shield rings out");
        add("mkcore.subtitle.weapon_block", "The sound of weapon shield rings out");
        add("mkcore.subtitle.arrow_block", "The sound of an arrow thudding into a shield");
        add("mkcore.subtitle.parry", "The sound of a deftly executed parry rings in the air");
        add("mkcore.subtitle.attack_cd_reset", "The sound of you swiftly readying your weapon for another strike");
        add("mkcore.subtitle.stun", "The sound of someone getting stunned");
        add("mkcore.subtitle.quest_complete", "The sound of someone completing a quest");
        add("mkcore.skill.increase", "Your %s Skill Increased to %.0f");
        add("mkcore.spawn_pattern.particle_spawn_pattern.advanced_line.name", "Advanced Line");
        add("mkcore.spawn_pattern.particle_spawn_pattern.advanced_perpendicular_line.name", "Advanced Perpendicular Line");
        add("mkcore.anim_track.particle_anim.flip_motion.name", "Flip");
        add("mkcore.ability_target.position_include_entities", "Single Target or Point In World");
        add("mkcore.spawn_pattern.particle_spawn_pattern.cone.name", "Cone");
        add("mkcore.spawn_pattern.particle_spawn_pattern.directional_cone.name", "Directional Cone");
        add("mkcore.ability.projectile.desc", "Behavior: %s");
        add("location_provider.single.desc", "a single projectile");
        add("projectile_behavior.simple", "Fires %s at end of cast.");
        add("location_provider.perpendicular_line", "%d projectiles in a perpendicular line");
        add("location_provider.circular", "%d projectiles in an arc from %s° to %s°");
        add("projectile_behavior.burst", "Fires %s, one after another over %s seconds.");
    }

    private void addParticles() {
        add("mkcore.particle_editor.spawn", "Spawn");
        add("mkcore.particle_editor.save", "Save");
        add("mkcore.particle_editor.load", "Load");
        add("mkcore.particle_editor.new", "New");
        add("mkcore.particle_editor.back", "Back");
        add("mkcore.particle_editor.add", "Add");
        add("mkcore.particle_editor.empty", "Empty");
        add("mkcore.particle_editor.select_or_add_keyframe", "Click a current key frame or add a new one to edit");

        add("particle.mkcore.magic_cross", "Magic Cross");
        add("particle.mkcore.magic_clover", "Magic Clover");
        add("particle.mkcore.magic_line", "Magic Line");
        add("particle.mkcore.magic_circle", "Magic Circle");
        add("particle.mkcore.magic_gradient_square", "Magic Gradient Square");
        add("particle.mkcore.magic_sideways_line", "Magic Sideways Line");
        add("particle.mkcore.magic_chip", "Magic Chip");
        add("particle.mkcore.black_magic_cross", "Black Magic Cross");
        add("particle.mkcore.black_magic_clover", "Black Magic Clover");
        add("particle.mkcore.black_magic_line", "Black Magic Line");
        add("particle.mkcore.black_magic_circle", "Black Magic Circle");
        add("particle.mkcore.black_magic_gradient_square", "Black Magic Gradient Square");
    }


    private void addAttributes() {
        // Base stats
        attribute(MKAttributes.HEALTH_REGEN, "Health Regen");
        attribute(MKAttributes.MANA_REGEN, "Mana Regen");
        attribute(MKAttributes.MAX_MANA, "Max Mana");
        attribute(MKAttributes.MAX_POISE, "Max Poise");
        attribute(MKAttributes.POISE_REGEN, "Poise Regen");
        attribute(MKAttributes.POISE_BREAK_CD, "Poise Break Time");
        attribute(MKAttributes.COOLDOWN, "Cooldown Rate");
        attribute(MKAttributes.BUFF_DURATION, "Buff Duration");
        attribute(MKAttributes.CASTING_SPEED, "Casting Speed");
        attribute(MKAttributes.HEAL_BONUS, "Heal Bonus");
        attribute(MKAttributes.HEAL_EFFICIENCY, "Heal Efficiency");

        // Crit chances
        attribute(MKAttributes.MELEE_CRIT, "Melee Crit Chance");
        attribute(MKAttributes.MELEE_CRIT_MULTIPLIER, "Melee Crit Multiplier");
        attribute(MKAttributes.MULTI_ATTACK_CHANCE, "Multi Attack Chance");
        attribute(MKAttributes.ARMOR_PIERCING, "Armor Piercing");
        attribute(MKAttributes.RANGED_CRIT, "Ranged Crit Chance");
        attribute(MKAttributes.RANGED_CRIT_MULTIPLIER, "Ranged Crit Multiplier");
        attribute(MKAttributes.SPELL_CRIT, "Spell Crit Chance");
        attribute(MKAttributes.SPELL_CRIT_MULTIPLIER, "Spell Crit Multiplier");

        // Damage types
        attribute(MKAttributes.RANGED_DAMAGE, "Ranged Damage Bonus");
        attribute(MKAttributes.RANGED_RESISTANCE, "Ranged Damage Resistance");
        attribute(MKAttributes.ARCANE_DAMAGE, "Arcane Damage");
        attribute(MKAttributes.ARCANE_RESISTANCE, "Arcane Resistance");
        attribute(MKAttributes.FIRE_DAMAGE, "Fire Damage");
        attribute(MKAttributes.FIRE_RESISTANCE, "Fire Resistance");
        attribute(MKAttributes.FROST_DAMAGE, "Frost Damage");
        attribute(MKAttributes.FROST_RESISTANCE, "Frost Resistance");
        attribute(MKAttributes.SHADOW_DAMAGE, "Shadow Damage");
        attribute(MKAttributes.SHADOW_RESISTANCE, "Shadow Resistance");
        attribute(MKAttributes.HOLY_DAMAGE, "Holy Damage");
        attribute(MKAttributes.HOLY_RESISTANCE, "Holy Resistance");
        attribute(MKAttributes.NATURE_DAMAGE, "Nature Damage");
        attribute(MKAttributes.NATURE_RESISTANCE, "Nature Resistance");
        attribute(MKAttributes.POISON_DAMAGE, "Poison Damage");
        attribute(MKAttributes.POISON_RESISTANCE, "Poison Resistance");
        attribute(MKAttributes.BLEED_DAMAGE, "Bleed Damage");
        attribute(MKAttributes.BLEED_RESISTANCE, "Bleed Resistance");

        // Spell schools
        attribute(MKAttributes.ABJURATION, "Abjuration");
        attribute(MKAttributes.ALTERATON, "Alteration");
        attribute(MKAttributes.CONJURATION, "Conjuration");
        attribute(MKAttributes.DIVINATION, "Divination");
        attribute(MKAttributes.ENCHANTMENT, "Enchantment");
        attribute(MKAttributes.EVOCATION, "Evocation");
        attribute(MKAttributes.PHANTASM, "Phantasm");
        attribute(MKAttributes.NECROMANCY, "Necromancy");
        attribute(MKAttributes.RESTORATION, "Restoration");
        attribute(MKAttributes.ARETE, "Arete");
        attribute(MKAttributes.PNEUMA, "Pneuma");
        attribute(MKAttributes.PANKRATION, "Pankration");
        attribute(MKAttributes.MARKSMANSHIP, "Marksmanship");


        // Weapon skills
        attribute(MKAttributes.HAND_TO_HAND, "Hand to Hand");
        attribute(MKAttributes.TWO_HAND_SLASH, "2H Slash");
        attribute(MKAttributes.ONE_HAND_SLASH, "1H Slash");
        attribute(MKAttributes.TWO_HAND_BLUNT, "2H Blunt");
        attribute(MKAttributes.ONE_HAND_BLUNT, "1H Blunt");
        attribute(MKAttributes.TWO_HAND_PIERCE, "2H Pierce");
        attribute(MKAttributes.ONE_HAND_PIERCE, "1H Pierce");
        attribute(MKAttributes.BLOCK, "Block");
        attribute(MKAttributes.BLOCK_EFFICIENCY, "Block Efficiency");
    }

    private void addDamageTypes() {
        damageType(CoreDamageTypes.FireDamage, "Fire Damage", "Burning Damage");
        damageType(CoreDamageTypes.FrostDamage, "Frost Damage", "Freezing Damage");
        damageType(CoreDamageTypes.HolyDamage, "Holy Damage", "Purifying Damage");
        damageType(CoreDamageTypes.PoisonDamage, "Poison Damage", "Poisoning Damage");
        damageType(CoreDamageTypes.ShadowDamage, "Shadow Damage", "Corrupting Damage");
        damageType(CoreDamageTypes.ArcaneDamage, "Arcane Damage", "Aetheric Damage");
        damageType(CoreDamageTypes.NatureDamage, "Nature Damage", "Wild Damage");
        damageType(CoreDamageTypes.BleedDamage, "Bleed Damage", "Bleeding Damage");
        damageType(CoreDamageTypes.MeleeDamage, "Melee Damage", "Hemorrhagic Damage");
        damageType(CoreDamageTypes.RangedDamage, "Ranged Damage", "Ablative Damage");
    }
}
