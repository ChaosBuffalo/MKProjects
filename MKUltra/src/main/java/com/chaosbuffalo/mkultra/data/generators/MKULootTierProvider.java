package com.chaosbuffalo.mkultra.data.generators;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUItems;
import com.chaosbuffalo.mkweapons.data.LootTierProvider;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.effects.melee.OnHitAbilityEffect;
import com.chaosbuffalo.mkweapons.items.effects.melee.UndeadDamageMeleeWeaponEffect;
import com.chaosbuffalo.mkweapons.items.randomization.LootItemTemplate;
import com.chaosbuffalo.mkweapons.items.randomization.LootTier;
import com.chaosbuffalo.mkweapons.items.randomization.options.*;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.chaosbuffalo.mkweapons.items.randomization.templates.RandomizationTemplate;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.CompletableFuture;

public class MKULootTierProvider extends LootTierProvider {

    public MKULootTierProvider(DataGenerator generator) {
        super(generator, MKUltra.MODID);
    }


    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.allOf(
                writeLootTier(trooperKnightLootTier(), cache),
                writeLootTier(zombieTrooperTier(), cache),
                writeLootTier(trooperCaptain(), cache),
                writeLootTier(trooperExecutioner(), cache),
                writeLootTier(trooperMagus(), cache),
                writeLootTier(burningSkeletonLoot(), cache),
                writeLootTier(burningStaff(), cache),
                writeLootTier(seafuryWeapon(), cache),
                writeLootTier(seawovenSkeletonTier(), cache),
                writeLootTier(ancientKingTier(), cache),
                writeLootTier(hyboreanSorcQueenTier(), cache)
        );
    }

    private LootTier seafuryWeapon() {
        LootTier tier = new LootTier(new ResourceLocation(MKUltra.MODID, "seafury"));
        LootItemTemplate weaponTemplate = new LootItemTemplate(LootSlotManager.MAIN_HAND);
        weaponTemplate.addItem(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.KATANA_TYPE));
        weaponTemplate.addItem(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.LONGSWORD_TYPE));
        var meleeEffects = new MeleeEffectOption();
        var weaponEffect = new OnHitAbilityEffect(0.5, 10.0f, MKUAbilities.SEAFURY);;
        meleeEffects.addEffect(weaponEffect);
        weaponTemplate.addRandomizationOption(meleeEffects);
        NameOption name = new NameOption(Component.literal("Seafury Blade"));
        weaponTemplate.addRandomizationOption(name);
        weaponTemplate.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "seafury_blade"),
                RandomizationSlotManager.EFFECT_SLOT, RandomizationSlotManager.NAME_SLOT), 10);
        tier.addItemTemplate(weaponTemplate, 10);
        return tier;
    }

    private LootTier burningSkeletonLoot() {
        LootTier tier = new LootTier(new ResourceLocation(MKUltra.MODID, "burning_skeleton"));
        addBloodyRing(tier, 10);
        addEarringOfFireDamage(tier, 10);
        addSacrificialDagger(tier, 10);
        return tier;

    }

    private void addSacrificialDagger(LootTier tier, double weight) {
        LootItemTemplate template = new LootItemTemplate(LootSlotManager.MAIN_HAND);
        template.addItem(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.DAGGER_TYPE));

        for (int i = 0; i < 3; i++) {
            for (int x = 0; x < 3; x++) {
                AttributeOption option = new AttributeOption();
                option.addAttributeModifier(MKAttributes.BLEED_DAMAGE, tier.getName().toString(), i + 1.0, 3 * (i + 1.0), AttributeModifier.Operation.ADDITION);
                option.addAttributeModifier(MKAttributes.FIRE_DAMAGE, tier.getName().toString(), x + 1.0, 3 * (x + 1.0), AttributeModifier.Operation.ADDITION);
                option.setWeight(10 - ((x + 1) * (i + 1)));
                template.addRandomizationOption(option);
            }
        }
        NameOption name = new NameOption(Component.literal("Sacrificial Dagger"));
        template.addRandomizationOption(name);
        template.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "blade"),
                RandomizationSlotManager.ATTRIBUTE_SLOT, RandomizationSlotManager.NAME_SLOT), 10);
        tier.addItemTemplate(template, weight);
    }

    private void addEarringOfFireDamage(LootTier tier, double weight) {

        LootItemTemplate template = new LootItemTemplate(LootSlotManager.EARRINGS);
        template.addItem(MKWeaponsItems.SilverEarring.get());

        AttributeOption option = new AttributeOption();
        option.addAttributeModifier(MKAttributes.FIRE_DAMAGE, tier.getName().toString(),
                2, 8, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(option);
        NameOption name = new NameOption(Component.literal("Earring of Minor Firepower"));
        template.addRandomizationOption(name);
        template.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "earring"),
                RandomizationSlotManager.ATTRIBUTE_SLOT, RandomizationSlotManager.NAME_SLOT), 15);
        tier.addItemTemplate(template, weight);
    }

    private void addBloodyRing(LootTier tier, double weight) {

        LootItemTemplate template = new LootItemTemplate(LootSlotManager.RINGS);
        template.addItem(MKWeaponsItems.RoseGoldRing.get());
        AttributeOption option = new AttributeOption();
        option.addAttributeModifier(MKAttributes.BLEED_DAMAGE, tier.getName().toString(),
                3, 9, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(option);
        NameOption name = new NameOption(Component.literal("Bloody Ring"));
        template.addRandomizationOption(name);
        template.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "ring"),
                RandomizationSlotManager.ATTRIBUTE_SLOT, RandomizationSlotManager.NAME_SLOT), 15);
        tier.addItemTemplate(template, weight);
    }

    private LootTier trooperKnightLootTier() {
        LootTier tier = new LootTier(new ResourceLocation(MKUltra.MODID, "trooper_knight_armor"));
        LootItemTemplate headTemp = new LootItemTemplate(LootSlotManager.HEAD);
        headTemp.addItem(MKUItems.trooperKnightHelmet.get());
        LootItemTemplate chestTemp = new LootItemTemplate(LootSlotManager.CHEST);
        chestTemp.addItem(MKUItems.trooperKnightChestplate.get());
        LootItemTemplate feetTemp = new LootItemTemplate(LootSlotManager.FEET);
        feetTemp.addItem(MKUItems.trooperKnightBoots.get());
        LootItemTemplate legsTemp = new LootItemTemplate(LootSlotManager.LEGS);
        legsTemp.addItem(MKUItems.trooperKnightLeggings.get());
        introCastleAttrs(tier, headTemp);
        introCastleAttrs(tier, feetTemp);
        introCastleAttrs(tier, chestTemp);
        introCastleAttrs(tier, legsTemp);
        addTemplateTrooperKnight(headTemp);
        addTemplateTrooperKnight(chestTemp);
        addTemplateTrooperKnight(legsTemp);
        addTemplateTrooperKnight(feetTemp);
        tier.addItemTemplate(headTemp, 1.0);
        tier.addItemTemplate(chestTemp, 1.0);
        tier.addItemTemplate(legsTemp, 1.0);
        tier.addItemTemplate(feetTemp, 1.0);
        return tier;
    }

    private void addTemplateTrooperKnight(LootItemTemplate template) {
        template.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "one_effect"),
                RandomizationSlotManager.ATTRIBUTE_SLOT), 90);
        template.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "two_effect"),
                RandomizationSlotManager.ATTRIBUTE_SLOT, RandomizationSlotManager.ATTRIBUTE_SLOT), 10);
    }

    private void addEarringOfMinorHealth(LootTier tier, double weight) {
        LootItemTemplate template = new LootItemTemplate(LootSlotManager.EARRINGS);
        template.addItem(MKWeaponsItems.GoldEarring.get());
        AttributeOption option = new AttributeOption();
        option.addAttributeModifier(Attributes.MAX_HEALTH, tier.getName().toString(),
                4, 20.0, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(option);
        NameOption name = new NameOption(Component.literal("Earring of Minor Health"));
        template.addRandomizationOption(name);
        template.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "earring"),
                RandomizationSlotManager.ATTRIBUTE_SLOT, RandomizationSlotManager.NAME_SLOT), 15);
        tier.addItemTemplate(template, weight);

    }

    private void addEarringOfMinorManaRegen(LootTier tier, double weight) {
        LootItemTemplate template = new LootItemTemplate(LootSlotManager.EARRINGS);
        template.addItem(MKWeaponsItems.SilverEarring.get());
        AttributeOption option = new AttributeOption();
        option.addAttributeModifier(MKAttributes.MANA_REGEN, tier.getName().toString(),
                0.25, 2.5, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(option);
        NameOption name = new NameOption(Component.literal("Earring of Quickening Thoughts"));
        template.addRandomizationOption(name);
        template.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "earring"),
                RandomizationSlotManager.ATTRIBUTE_SLOT, RandomizationSlotManager.NAME_SLOT), 15);
        tier.addItemTemplate(template, weight);
    }

    private void addRingOfMinorMana(LootTier tier, double weight) {
        LootItemTemplate template = new LootItemTemplate(LootSlotManager.RINGS);
        template.addItem(MKWeaponsItems.SilverRing.get());
        AttributeOption option = new AttributeOption();
        option.addAttributeModifier(MKAttributes.MAX_MANA, tier.getName().toString(),
                4, 20, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(option);
        NameOption name = new NameOption(Component.literal("Ring of Minor Mana"));
        template.addRandomizationOption(name);
        template.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "ring"),
                RandomizationSlotManager.ATTRIBUTE_SLOT, RandomizationSlotManager.NAME_SLOT), 15);
        tier.addItemTemplate(template, weight);

    }

    private void addRingOfKeenness(LootTier tier, double weight) {
        LootItemTemplate template = new LootItemTemplate(LootSlotManager.RINGS);
        template.addItem(MKWeaponsItems.CopperRing.get());
        AttributeOption option = new AttributeOption();
        option.addAttributeModifier(MKAttributes.MELEE_CRIT, tier.getName().toString(),
                0.02, 0.1, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(option);
        NameOption name = new NameOption(Component.literal("Ring of Keen Edges"));
        template.addRandomizationOption(name);
        template.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "ring"),
                RandomizationSlotManager.ATTRIBUTE_SLOT, RandomizationSlotManager.NAME_SLOT), 15);
        tier.addItemTemplate(template, weight);
    }

    private void addRingOfSpellCrit(LootTier tier, double weight) {
        LootItemTemplate template = new LootItemTemplate(LootSlotManager.RINGS);
        template.addItem(MKWeaponsItems.SilverRing.get());
        AttributeOption option = new AttributeOption();
        option.addAttributeModifier(MKAttributes.SPELL_CRIT, tier.getName().toString(),
                0.02, 0.1, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(option);
        NameOption name = new NameOption(Component.literal("Ring of Destruction"));
        template.addRandomizationOption(name);
        template.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "ring"),
                RandomizationSlotManager.ATTRIBUTE_SLOT, RandomizationSlotManager.NAME_SLOT), 15);
        tier.addItemTemplate(template, weight);
    }

    private LootTier hyboreanSorcQueenTier() {
        LootTier tier = new LootTier(new ResourceLocation(MKUltra.MODID, "hyborean_sorcerer_queen"));
        addEarringOfSpellDamage(tier, 10);
        addFlameWaveStaff(tier, 10);
        return tier;
    }

    private LootTier ancientKingTier() {
        LootTier tier = new LootTier(new ResourceLocation(MKUltra.MODID, "ancient_king"));
        addRingOfSpellCrit(tier, 10);
        addEarringOfCritDamage(tier, 10);
        addRingOfKeenness(tier, 10);
        return tier;
    }

    private void addEarringOfSpellDamage(LootTier tier, double weight) {
        LootItemTemplate template = new LootItemTemplate(LootSlotManager.EARRINGS);
        template.addItem(MKWeaponsItems.SilverEarring.get());
        AttributeOption option = new AttributeOption();
        option.addAttributeModifier(MKAttributes.SPELL_CRIT_MULTIPLIER, tier.getName().toString(),
                0.05, 0.2, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(option);
        NameOption name = new NameOption(Component.literal("Earring of Power"));
        template.addRandomizationOption(name);
        template.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "earring"),
                RandomizationSlotManager.ATTRIBUTE_SLOT, RandomizationSlotManager.NAME_SLOT), 15);
        tier.addItemTemplate(template, weight);
    }

    private void addEarringOfCritDamage(LootTier tier, double weight) {
        LootItemTemplate template = new LootItemTemplate(LootSlotManager.EARRINGS);
        template.addItem(MKWeaponsItems.CopperEarring.get());
        AttributeOption option = new AttributeOption();
        option.addAttributeModifier(MKAttributes.MELEE_CRIT_MULTIPLIER, tier.getName().toString(),
                0.05, 0.2, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(option);
        NameOption name = new NameOption(Component.literal("Earring of Might"));
        template.addRandomizationOption(name);
        template.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "earring"),
                RandomizationSlotManager.ATTRIBUTE_SLOT, RandomizationSlotManager.NAME_SLOT), 15);
        tier.addItemTemplate(template, weight);
    }

    private void cryptAttrs(LootTier tier, LootItemTemplate template) {
        AttributeOption healthAttribute = new AttributeOption();
        healthAttribute.addAttributeModifier(Attributes.MAX_HEALTH, tier.getName().toString(),
                5, 20.0, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(healthAttribute);
        AttributeOption manaAttribute = new AttributeOption();
        manaAttribute.addAttributeModifier(MKAttributes.MAX_MANA, tier.getName().toString(),
                5, 20.0, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(manaAttribute);
        AttributeOption manaRegen = new AttributeOption();
        manaRegen.addAttributeModifier(MKAttributes.MANA_REGEN, tier.getName().toString(),
                0.5, 4.0, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(manaRegen);
        AttributeOption atkDamage = new AttributeOption();
        atkDamage.addAttributeModifier(Attributes.ATTACK_DAMAGE, tier.getName().toString(),
                2.0, 6.0, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(atkDamage);
        AttributeOption armor = new AttributeOption();
        armor.addAttributeModifier(Attributes.ARMOR, tier.getName().toString(),
                2.0, 8.0, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(armor);
        AttributeOption eleDamage = new AttributeOption();
        eleDamage.addAttributeModifier(MKAttributes.FIRE_DAMAGE, tier.getName().toString(),
                2.0, 6.0, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(eleDamage);
        AttributeOption eleResistance = new AttributeOption();
        eleResistance.addAttributeModifier(MKAttributes.FIRE_RESISTANCE, tier.getName().toString(),
                0.05, 0.15, AttributeModifier.Operation.ADDITION);
    }

    private void addFlameWaveStaff(LootTier tier, double weight) {
        LootItemTemplate staff = new LootItemTemplate(LootSlotManager.MAIN_HAND);
        staff.addItem(MKUItems.lookupWeapon(MKUItems.BRONZE_TIER, MeleeWeaponTypes.STAFF_TYPE));
        AddAbilityOption abilityOption = new AddAbilityOption(MKUAbilities.FLAME_WAVE.get(), RandomizationSlotManager.ABILITY_SLOT);
        staff.addRandomizationOption(abilityOption);
        NameOption name = new NameOption(Component.literal("Staff of Flames"));
        staff.addRandomizationOption(name);
        cryptAttrs(tier, staff);
        staff.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "staff"),
                RandomizationSlotManager.ABILITY_SLOT, RandomizationSlotManager.NAME_SLOT), 10);
        staff.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "staff_crit"),
                RandomizationSlotManager.ABILITY_SLOT, RandomizationSlotManager.NAME_SLOT, RandomizationSlotManager.ATTRIBUTE_SLOT), 5);
        tier.addItemTemplate(staff, weight);
    }

    private void addRingOfMinorHealth(LootTier tier, double weight) {
        LootItemTemplate template = new LootItemTemplate(LootSlotManager.RINGS);
        template.addItem(MKWeaponsItems.GoldRing.get());
        AttributeOption option = new AttributeOption();
        option.addAttributeModifier(Attributes.MAX_HEALTH, tier.getName().toString(),
                4, 20.0, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(option);
        NameOption name = new NameOption(Component.literal("Ring of Minor Health"));
        template.addRandomizationOption(name);
        template.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "ring"),
                RandomizationSlotManager.ATTRIBUTE_SLOT, RandomizationSlotManager.NAME_SLOT), 15);
        tier.addItemTemplate(template, weight);
    }

    private LootTier trooperCaptain() {
        LootTier tier = new LootTier(new ResourceLocation(MKUltra.MODID, "trooper_captain"));
        LootItemTemplate katana = new LootItemTemplate(LootSlotManager.MAIN_HAND);
        katana.addItem(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.KATANA_TYPE));
        MeleeEffectOption meleeEffect = new MeleeEffectOption();
        meleeEffect.addEffect(new UndeadDamageMeleeWeaponEffect(1.25f));
        katana.addRandomizationOption(meleeEffect);
        NameOption name = new NameOption(Component.literal("Stinging Blade"));
        katana.addRandomizationOption(name);
        introCastleAttrs(tier, katana);
        katana.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "blade"),
                RandomizationSlotManager.EFFECT_SLOT, RandomizationSlotManager.NAME_SLOT), 10);
        katana.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "blade_crit"),
                RandomizationSlotManager.EFFECT_SLOT, RandomizationSlotManager.NAME_SLOT, RandomizationSlotManager.ATTRIBUTE_SLOT), 1);
        tier.addItemTemplate(katana, 5);
        addEarringOfMinorHealth(tier, 10);
        return tier;
    }

    private LootTier burningStaff() {
        LootTier tier = new LootTier(new ResourceLocation(MKUltra.MODID, "burning_staff"));
        LootItemTemplate staff = new LootItemTemplate(LootSlotManager.MAIN_HAND);
        staff.addItem(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.STAFF_TYPE));
        AddAbilityOption abilityOption = new AddAbilityOption(MKUAbilities.FIREBALL.get(), RandomizationSlotManager.ABILITY_SLOT);
        staff.addRandomizationOption(abilityOption);
        NameOption name = new NameOption(Component.literal("Burning Staff"));
        staff.addRandomizationOption(name);
        introCastleAttrs(tier, staff);
        staff.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "blade"),
                RandomizationSlotManager.ABILITY_SLOT, RandomizationSlotManager.NAME_SLOT), 10);
        staff.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "blade_crit"),
                RandomizationSlotManager.ABILITY_SLOT, RandomizationSlotManager.NAME_SLOT, RandomizationSlotManager.ATTRIBUTE_SLOT), 1);
        tier.addItemTemplate(staff, 10);
        return tier;
    }

    private LootTier trooperMagus() {
        LootTier tier = new LootTier(new ResourceLocation(MKUltra.MODID, "trooper_magus"));
        addRingOfMinorMana(tier, 10);
        addEarringOfMinorManaRegen(tier, 10);
        return tier;
    }

    private LootTier trooperExecutioner() {
        LootTier tier = new LootTier(new ResourceLocation(MKUltra.MODID, "trooper_executioner"));
        LootItemTemplate executionersBlade = new LootItemTemplate(LootSlotManager.MAIN_HAND);
        executionersBlade.addItem(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.GREATSWORD_TYPE));
        executionersBlade.addItem(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.WARHAMMER_TYPE));
        executionersBlade.addItem(MKWeaponsItems.lookupWeapon(MKWeaponsItems.IRON_TIER, MeleeWeaponTypes.BATTLEAXE_TYPE));
        AddAbilityOption abilityOption = new AddAbilityOption(MKUAbilities.SEVER_TENDON.get(), RandomizationSlotManager.ABILITY_SLOT);
        executionersBlade.addRandomizationOption(abilityOption);
        PrefixNameOption name = new PrefixNameOption(Component.literal("Executioner's"));
        executionersBlade.addRandomizationOption(name);
        introCastleAttrs(tier, executionersBlade);
        executionersBlade.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "blade"),
                RandomizationSlotManager.ABILITY_SLOT, RandomizationSlotManager.NAME_SLOT), 10);
        executionersBlade.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "blade_crit"),
                RandomizationSlotManager.ABILITY_SLOT, RandomizationSlotManager.NAME_SLOT, RandomizationSlotManager.ATTRIBUTE_SLOT), 1);
        addRingOfMinorHealth(tier, 10);
        tier.addItemTemplate(executionersBlade, 5);
        return tier;
    }

    private LootTier seawovenSkeletonTier() {
        LootTier tier = new LootTier(new ResourceLocation(MKUltra.MODID, "seawoven_skeleton"));
        LootItemTemplate pigLoot = new LootItemTemplate(LootSlotManager.ITEMS);
        pigLoot.addItemStack(new ItemStack(MKUItems.seawovenScrap.get()), 1.0);
        pigLoot.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "empty")), 1.0);
        tier.addItemTemplate(pigLoot, 10);
        return tier;

    }

    private LootTier zombieTrooperTier() {
        LootTier tier = new LootTier(new ResourceLocation(MKUltra.MODID, "zombie_trooper"));
        LootItemTemplate pigLoot = new LootItemTemplate(LootSlotManager.ITEMS);
        pigLoot.addItemStack(new ItemStack(MKUItems.corruptedPigIronPlate.get()), 10.0);
        pigLoot.addItemStack(new ItemStack(MKUItems.destroyedTrooperBoots.get()), 1.0);
        pigLoot.addItemStack(new ItemStack(MKUItems.destroyedTrooperChestplate.get()), 1.0);
        pigLoot.addItemStack(new ItemStack(MKUItems.destroyedTrooperLeggings.get()), 1.0);
        pigLoot.addItemStack(new ItemStack(MKUItems.destroyedTrooperHelmet.get()), 1.0);
        pigLoot.addTemplate(new RandomizationTemplate(new ResourceLocation(MKUltra.MODID, "empty")), 1.0);
        tier.addItemTemplate(pigLoot, 10);
        return tier;

    }

    private void introCastleAttrs(LootTier tier, LootItemTemplate template) {
        AttributeOption healthAttribute = new AttributeOption();
        healthAttribute.addAttributeModifier(Attributes.MAX_HEALTH, tier.getName().toString(),
                2, 10.0, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(healthAttribute);
        AttributeOption manaAttribute = new AttributeOption();
        manaAttribute.addAttributeModifier(MKAttributes.MAX_MANA, tier.getName().toString(),
                2, 10.0, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(manaAttribute);
        AttributeOption manaRegen = new AttributeOption();
        manaRegen.addAttributeModifier(MKAttributes.MANA_REGEN, tier.getName().toString(),
                0.25, 2.0, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(manaRegen);
        AttributeOption runSpeed = new AttributeOption();
        runSpeed.addAttributeModifier(Attributes.MOVEMENT_SPEED, tier.getName().toString(),
                0.05, 0.15, AttributeModifier.Operation.MULTIPLY_TOTAL);
        template.addRandomizationOption(runSpeed);
        AttributeOption atkDamage = new AttributeOption();
        atkDamage.addAttributeModifier(Attributes.ATTACK_DAMAGE, tier.getName().toString(),
                1.0, 4.0, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(atkDamage);
        AttributeOption armor = new AttributeOption();
        armor.addAttributeModifier(Attributes.ARMOR, tier.getName().toString(),
                1.0, 4.0, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(armor);
        AttributeOption natureDamage = new AttributeOption();
        natureDamage.addAttributeModifier(MKAttributes.NATURE_DAMAGE, tier.getName().toString(),
                1, 4.0, AttributeModifier.Operation.ADDITION);
        template.addRandomizationOption(natureDamage);
    }
}
