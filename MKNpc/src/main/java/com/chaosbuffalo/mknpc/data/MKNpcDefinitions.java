package com.chaosbuffalo.mknpc.data;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.data.MKChatGenerator;
import com.chaosbuffalo.mkfaction.init.MKFactions;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.client.render.renderers.GolemStyles;
import com.chaosbuffalo.mknpc.client.render.renderers.SkeletonStyles;
import com.chaosbuffalo.mknpc.init.MKNpcEntityTypes;
import com.chaosbuffalo.mknpc.npc.NpcAttributeEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcItemChoice;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.chaosbuffalo.mkweapons.MKWeapons;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MKNpcDefinitions {

    static ResourceKey<NpcDefinition> key(String name) {
        return ResourceKey.create(NpcRegistries.NPC_DEFINITIONS, MKNpc.id(name));
    }

    public static final ResourceKey<NpcDefinition> TEST_SKELETON = key("test_skeleton");
    public static final ResourceKey<NpcDefinition> TEST_GHOST = key("test_ghost");
    public static final ResourceKey<NpcDefinition> TEST_LADY = key("test_lady");
    public static final ResourceKey<NpcDefinition> TEST_LADY2 = key("test_lady2");
    public static final ResourceKey<NpcDefinition> TEST_GOLEM = key("test_golem");
    public static final ResourceKey<NpcDefinition> TEST_MELEE_EMPTY_HAND = key("test_melee_empty_hand");
    public static final ResourceKey<NpcDefinition> TEST_MELEE_LONGSWORD = key("test_melee_longsword");
    public static final ResourceKey<NpcDefinition> TEST_MELEE_DAGGER = key("test_melee_dagger");
    public static final ResourceKey<NpcDefinition> TEST_MELEE_SPEAR = key("test_melee_spear");
    public static final ResourceKey<NpcDefinition> TEST_MELEE_BATTLEAXE = key("test_melee_battleaxe");
    public static final ResourceKey<NpcDefinition> TEST_MELEE_GREATSWORD_WITH_DAGGER = key("test_melee_greatsword_with_dagger");
    public static final ResourceKey<NpcDefinition> TEST_MELEE_EMPTY_WITH_DAGGER_OFFHAND = key("test_melee_empty_with_dagger_offhand");
    public static final ResourceKey<NpcDefinition> TEST_MELEE_DUAL_LONGSWORDS = key("test_melee_dual_longswords");
    public static final ResourceKey<NpcDefinition> TEST_MELEE_DUAL_LONGSWORD_DAGGER = key("test_melee_dual_longsword_dagger");
    public static final ResourceKey<NpcDefinition> TEST_MELEE_MULTI_LONGSWORD = key("test_melee_multi_longsword");
    public static final ResourceKey<NpcDefinition> TEST_MELEE_MULTI_DUAL_LONGSWORD_DAGGER = key("test_melee_multi_dual_longsword_dagger");
    public static final ResourceKey<NpcDefinition> TEST_GOLEM_MULTI_EMPTY = key("test_golem_multi_empty");
    public static final ResourceKey<NpcDefinition> TEST_GOLEM_LONGSWORD = key("test_golem_longsword");
    public static final ResourceKey<NpcDefinition> TEST_GOLEM_DAGGER = key("test_golem_dagger");
    public static final ResourceKey<NpcDefinition> TEST_GOLEM_MACE = key("test_golem_mace");
    public static final ResourceKey<NpcDefinition> TEST_GOLEM_KATANA = key("test_golem_katana");
    public static final ResourceKey<NpcDefinition> TEST_GOLEM_SPEAR = key("test_golem_spear");
    public static final ResourceKey<NpcDefinition> TEST_GOLEM_STAFF = key("test_golem_staff");
    public static final ResourceKey<NpcDefinition> TEST_GOLEM_BATTLEAXE = key("test_golem_battleaxe");
    public static final ResourceKey<NpcDefinition> TEST_GOLEM_GREATSWORD = key("test_golem_greatsword");
    public static final ResourceKey<NpcDefinition> TEST_GOLEM_DUAL_DAGGERS = key("test_golem_dual_daggers");
    public static final ResourceKey<NpcDefinition> TEST_GOLEM_DUAL_LONGSWORD_DAGGER = key("test_golem_dual_longsword_dagger");
    public static final ResourceKey<NpcDefinition> TEST_GOLEM_DUAL_MACE_DAGGER = key("test_golem_dual_mace_dagger");
    public static final ResourceKey<NpcDefinition> TEST_GOLEM_GREATSWORD_WITH_DAGGER = key("test_golem_greatsword_with_dagger");
    public static final ResourceKey<NpcDefinition> TEST_GOLEM_MULTI_LONGSWORD = key("test_golem_multi_longsword");
    public static final ResourceKey<NpcDefinition> TEST_GOLEM_MULTI_DUAL_LONGSWORD_DAGGER = key("test_golem_multi_dual_longsword_dagger");
    public static final ResourceKey<NpcDefinition> TEST_SKULL_MULTI = key("test_skull_multi");


    public static void bootstrap(BootstrapContext<NpcDefinition> context) {
        context.register(TEST_SKELETON, generateTestSkeleton(TEST_SKELETON));
        context.register(TEST_GHOST, generateTestGhostSkeleton(TEST_GHOST));
        context.register(TEST_LADY, generateTestLady(TEST_LADY));
        context.register(TEST_LADY2, generateTestLady2(TEST_LADY2));
        context.register(TEST_GOLEM, generateTestGolem(TEST_GOLEM));
        context.register(TEST_MELEE_EMPTY_HAND, generateTestMeleeEmptyHand(TEST_MELEE_EMPTY_HAND));
        context.register(TEST_MELEE_LONGSWORD, generateTestMeleeWeapon(TEST_MELEE_LONGSWORD, "Test Melee Longsword",
                "longsword_iron", null, false));
        context.register(TEST_MELEE_DAGGER, generateTestMeleeWeapon(TEST_MELEE_DAGGER, "Test Melee Dagger",
                "dagger_iron", null, false));
        context.register(TEST_MELEE_SPEAR, generateTestMeleeWeapon(TEST_MELEE_SPEAR, "Test Melee Spear",
                "spear_iron", null, false));
        context.register(TEST_MELEE_BATTLEAXE, generateTestMeleeWeapon(TEST_MELEE_BATTLEAXE, "Test Melee Battleaxe",
                "battleaxe_iron", null, false));
        context.register(TEST_MELEE_GREATSWORD_WITH_DAGGER, generateTestMeleeWeapon(TEST_MELEE_GREATSWORD_WITH_DAGGER,
                "Test Melee Greatsword With Dagger", "greatsword_iron", "dagger_iron", false));
        context.register(TEST_MELEE_EMPTY_WITH_DAGGER_OFFHAND, generateTestMeleeWeapon(TEST_MELEE_EMPTY_WITH_DAGGER_OFFHAND,
                "Test Melee Empty With Dagger Offhand", null, "dagger_iron", false));
        context.register(TEST_MELEE_DUAL_LONGSWORDS, generateTestMeleeWeapon(TEST_MELEE_DUAL_LONGSWORDS,
                "Test Melee Dual Longswords", "longsword_iron", "longsword_iron", false));
        context.register(TEST_MELEE_DUAL_LONGSWORD_DAGGER, generateTestMeleeWeapon(TEST_MELEE_DUAL_LONGSWORD_DAGGER,
                "Test Melee Dual Longsword Dagger", "longsword_iron", "dagger_iron", false));
        context.register(TEST_MELEE_MULTI_LONGSWORD, generateTestMeleeWeapon(TEST_MELEE_MULTI_LONGSWORD,
                "Test Melee Multi Longsword", "longsword_iron", null, true));
        context.register(TEST_MELEE_MULTI_DUAL_LONGSWORD_DAGGER, generateTestMeleeWeapon(TEST_MELEE_MULTI_DUAL_LONGSWORD_DAGGER,
                "Test Melee Multi Dual Longsword Dagger", "longsword_iron", "dagger_iron", true));
        context.register(TEST_GOLEM_MULTI_EMPTY, generateTestGolemMultiEmpty(TEST_GOLEM_MULTI_EMPTY));
        context.register(TEST_GOLEM_LONGSWORD, generateTestGolemWeapon(TEST_GOLEM_LONGSWORD,
                "Test Golem Longsword", "longsword_iron", null));
        context.register(TEST_GOLEM_DAGGER, generateTestGolemWeapon(TEST_GOLEM_DAGGER,
                "Test Golem Dagger", "dagger_iron", null));
        context.register(TEST_GOLEM_MACE, generateTestGolemWeapon(TEST_GOLEM_MACE,
                "Test Golem Mace", "mace_iron", null));
        context.register(TEST_GOLEM_KATANA, generateTestGolemWeapon(TEST_GOLEM_KATANA,
                "Test Golem Katana", "katana_iron", null));
        context.register(TEST_GOLEM_SPEAR, generateTestGolemWeapon(TEST_GOLEM_SPEAR,
                "Test Golem Spear", "spear_iron", null));
        context.register(TEST_GOLEM_STAFF, generateTestGolemWeapon(TEST_GOLEM_STAFF,
                "Test Golem Staff", "staff_iron", null));
        context.register(TEST_GOLEM_BATTLEAXE, generateTestGolemWeapon(TEST_GOLEM_BATTLEAXE,
                "Test Golem Battleaxe", "battleaxe_iron", null));
        context.register(TEST_GOLEM_GREATSWORD, generateTestGolemWeapon(TEST_GOLEM_GREATSWORD,
                "Test Golem Greatsword", "greatsword_iron", null));
        context.register(TEST_GOLEM_DUAL_DAGGERS, generateTestGolemWeapon(TEST_GOLEM_DUAL_DAGGERS,
                "Test Golem Dual Daggers", "dagger_iron", "dagger_iron"));
        context.register(TEST_GOLEM_DUAL_LONGSWORD_DAGGER, generateTestGolemWeapon(TEST_GOLEM_DUAL_LONGSWORD_DAGGER,
                "Test Golem Dual Longsword Dagger", "longsword_iron", "dagger_iron"));
        context.register(TEST_GOLEM_DUAL_MACE_DAGGER, generateTestGolemWeapon(TEST_GOLEM_DUAL_MACE_DAGGER,
                "Test Golem Dual Mace Dagger", "mace_iron", "dagger_iron"));
        context.register(TEST_GOLEM_GREATSWORD_WITH_DAGGER, generateTestGolemWeapon(TEST_GOLEM_GREATSWORD_WITH_DAGGER,
                "Test Golem Greatsword With Dagger", "greatsword_iron", "dagger_iron"));
        context.register(TEST_GOLEM_MULTI_LONGSWORD, generateTestGolemWeapon(TEST_GOLEM_MULTI_LONGSWORD,
                "Test Golem Multi Longsword", "longsword_iron", null, true));
        context.register(TEST_GOLEM_MULTI_DUAL_LONGSWORD_DAGGER, generateTestGolemWeapon(TEST_GOLEM_MULTI_DUAL_LONGSWORD_DAGGER,
                "Test Golem Multi Dual Longsword Dagger", "longsword_iron", "dagger_iron", true));
        context.register(TEST_SKULL_MULTI, generateTestSkullMulti(TEST_SKULL_MULTI));
    }

    private static NpcDefinition generateTestSkeleton(ResourceKey<NpcDefinition> key) {
        NpcDefinition def = new NpcDefinition(key, MKNpcEntityTypes.SKELETON_TYPE);
        def.addOption(new FactionOption(MKFactions.UNDEAD));
        def.addOption(new MKSizeOption(0.25f));
        def.addOption(new RenderGroupOption(SkeletonStyles.lookKey(MKNpcEntityTypes.SKELETON_TYPE.get(), "wither_king")));
        return def;
    }

    private static NpcDefinition generateTestGhostSkeleton(ResourceKey<NpcDefinition> key) {
        NpcDefinition def = new NpcDefinition(key, MKNpcEntityTypes.SKELETON_TYPE);
        def.addOption(new FactionOption(MKFactions.UNDEAD));
        def.addOption(new EquipmentOption()
                .addItemChoice(EquipmentSlot.CHEST, new NpcItemChoice(new ItemStack(Items.IRON_CHESTPLATE), 5, 1.1f))
        );
        def.addOption(new GhostOption());
        return def;
    }

    private static NpcDefinition generateTestLady(ResourceKey<NpcDefinition> key) {
        NpcDefinition def = new NpcDefinition(key, EntityType.VILLAGER);
        def.addOption(new NameOption("Test Lady"));
        def.addOption(new AttributesOption().addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 100)));
        def.addOption(new FactionOption(MKFactions.VILLAGERS));
        def.addOption(new DialogueOption(MKChatGenerator.Dialogues.TEST_TREE));
        def.addOption(new EquipmentOption()
                .addItemChoice(EquipmentSlot.MAINHAND, new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.fromNamespaceAndPath(MKWeapons.MODID, "katana_iron"))), 5, 1.1f))
                .addItemChoice(EquipmentSlot.MAINHAND, new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.fromNamespaceAndPath(MKWeapons.MODID, "dagger_iron"))), 10, 1.1f))
        );
        return def;
    }

    private static NpcDefinition generateTestLady2(ResourceKey<NpcDefinition> key) {
        NpcDefinition def = NpcDefinition.derived(key, TEST_LADY);
        def.addOption(new FactionOption(MKFactions.UNDEAD));
        def.addOption(new NotableOption());
        def.addOption(new FactionNameOption().setHasLastName(true).setTitle("Chief"));
        return def;
    }

    private static NpcDefinition generateTestGolem(ResourceKey<NpcDefinition> key) {
        NpcDefinition def = new NpcDefinition(key, MKNpcEntityTypes.GOLEM_TYPE);
        def.addOption(new NameOption("Test Golem"));
        def.addOption(new FactionOption(MKFactions.UNDEAD));
        def.addOption(new RenderGroupOption(GolemStyles.DEFAULT_LOOK));
        def.addOption(new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 200.0))
                .addAttributeEntry(new NpcAttributeEntry(Attributes.ATTACK_DAMAGE, 8.0)));
        return def;
    }

    private static NpcDefinition generateTestMeleeEmptyHand(ResourceKey<NpcDefinition> key) {
        return createMeleeSkeleton(key, "Test Melee Empty Hand", false);
    }

    private static NpcDefinition generateTestMeleeWeapon(ResourceKey<NpcDefinition> key, String name,
                                                        String mainhandWeapon, String offhandWeapon,
                                                        boolean multiAttack) {
        NpcDefinition def = createMeleeSkeleton(key, name, multiAttack);
        EquipmentOption equipment = new EquipmentOption();
        if (mainhandWeapon != null) {
            equipment.addItemChoice(EquipmentSlot.MAINHAND, fixedItemChoice(weaponStack(mainhandWeapon)));
        }
        if (offhandWeapon != null) {
            equipment.addItemChoice(EquipmentSlot.OFFHAND, fixedItemChoice(weaponStack(offhandWeapon)));
        }
        def.addOption(equipment);
        return def;
    }

    private static NpcDefinition generateTestGolemMultiEmpty(ResourceKey<NpcDefinition> key) {
        NpcDefinition def = new NpcDefinition(key, MKNpcEntityTypes.GOLEM_TYPE);
        def.addOption(new NameOption("Test Golem Multi Empty"));
        def.addOption(new FactionOption(MKFactions.UNDEAD));
        def.addOption(new RenderGroupOption(GolemStyles.DEFAULT_LOOK));
        def.addOption(baseMeleeAttributes(200.0, 8.0, true));
        return def;
    }

    private static NpcDefinition generateTestGolemWeapon(ResourceKey<NpcDefinition> key, String name,
                                                        String mainhandWeapon, String offhandWeapon) {
        return generateTestGolemWeapon(key, name, mainhandWeapon, offhandWeapon, false);
    }

    private static NpcDefinition generateTestGolemWeapon(ResourceKey<NpcDefinition> key, String name,
                                                        String mainhandWeapon, String offhandWeapon,
                                                        boolean multiAttack) {
        NpcDefinition def = new NpcDefinition(key, MKNpcEntityTypes.GOLEM_TYPE);
        def.addOption(new NameOption(name));
        def.addOption(new FactionOption(MKFactions.UNDEAD));
        def.addOption(new RenderGroupOption(GolemStyles.DEFAULT_LOOK));
        def.addOption(baseMeleeAttributes(200.0, 8.0, multiAttack));
        EquipmentOption equipment = new EquipmentOption()
                .addItemChoice(EquipmentSlot.MAINHAND, fixedItemChoice(weaponStack(mainhandWeapon)));
        if (offhandWeapon != null) {
            equipment.addItemChoice(EquipmentSlot.OFFHAND, fixedItemChoice(weaponStack(offhandWeapon)));
        }
        def.addOption(equipment);
        return def;
    }

    private static NpcDefinition generateTestSkullMulti(ResourceKey<NpcDefinition> key) {
        NpcDefinition def = new NpcDefinition(key, MKNpcEntityTypes.FLYING_SKULL_TYPE);
        def.addOption(new NameOption("Test Skull Multi"));
        def.addOption(new FactionOption(MKFactions.UNDEAD));
        def.addOption(baseMeleeAttributes(100.0, 4.0, true));
        return def;
    }

    private static NpcDefinition createMeleeSkeleton(ResourceKey<NpcDefinition> key, String name, boolean multiAttack) {
        NpcDefinition def = new NpcDefinition(key, MKNpcEntityTypes.SKELETON_TYPE);
        def.addOption(new NameOption(name));
        def.addOption(new FactionOption(MKFactions.UNDEAD));
        def.addOption(new RenderGroupOption(SkeletonStyles.lookKey(MKNpcEntityTypes.SKELETON_TYPE.get(), "wither_king")));
        def.addOption(baseMeleeAttributes(100.0, 4.0, multiAttack));
        return def;
    }

    private static AttributesOption baseMeleeAttributes(double maxHealth, double attackDamage, boolean multiAttack) {
        AttributesOption attributes = new AttributesOption()
                .addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, maxHealth))
                .addAttributeEntry(new NpcAttributeEntry(Attributes.ATTACK_DAMAGE, attackDamage));
        if (multiAttack) {
            attributes.addAttributeEntry(new NpcAttributeEntry(MKAttributes.MULTI_ATTACK_CHANCE, 4.0));
        }
        return attributes;
    }

    private static NpcItemChoice fixedItemChoice(ItemStack stack) {
        return new NpcItemChoice(stack, 1.0, 0.0f);
    }

    private static ItemStack weaponStack(String weaponName) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(MKWeapons.MODID, weaponName)));
    }
}
