package com.chaosbuffalo.mknpc.data;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.data.MKChatGenerator;
import com.chaosbuffalo.mkfaction.init.MKFactions;
import com.chaosbuffalo.mknpc.MKNpc;
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


    public static void bootstrap(BootstrapContext<NpcDefinition> context) {
        context.register(TEST_SKELETON, generateTestSkeleton(TEST_SKELETON));
        context.register(TEST_GHOST, generateTestGhostSkeleton(TEST_GHOST));
        context.register(TEST_LADY, generateTestLady(TEST_LADY));
        context.register(TEST_LADY2, generateTestLady2(TEST_LADY2));
    }

    private static NpcDefinition generateTestSkeleton(ResourceKey<NpcDefinition> key) {
        NpcDefinition def = new NpcDefinition(key, MKNpcEntityTypes.SKELETON_TYPE);
        def.addOption(new FactionOption(MKFactions.UNDEAD));
        def.addOption(new MKSizeOption(0.25f));
        def.addOption(new RenderGroupOption("wither_king"));
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
}
