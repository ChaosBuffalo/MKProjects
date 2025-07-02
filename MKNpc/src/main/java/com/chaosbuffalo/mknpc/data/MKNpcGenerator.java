package com.chaosbuffalo.mknpc.data;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkfaction.init.MKFactions;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.data.generators.NpcRegistrySets;
import com.chaosbuffalo.mknpc.data.generators.tags.NpcBiomeTagsProvider;
import com.chaosbuffalo.mknpc.data.generators.tags.NpcEntityTypeTagsProvider;
import com.chaosbuffalo.mknpc.data.generators.tags.NpcStructureTagsProvider;
import com.chaosbuffalo.mknpc.data.providers.NpcDefinitionProvider;
import com.chaosbuffalo.mknpc.init.MKNpcEntityTypes;
import com.chaosbuffalo.mknpc.npc.NpcAttributeEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcItemChoice;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.chaosbuffalo.mkweapons.MKWeapons;
import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber
public class MKNpcGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        PackOutput packOutput = generator.getPackOutput();

        NpcRegistrySets datapackRegistrySets = new NpcRegistrySets(packOutput, lookupProvider);
        generator.addProvider(event.includeServer(), datapackRegistrySets);
        var datapackLookup = datapackRegistrySets.getRegistryProvider();
        generator.addProvider(event.includeServer(), new NpcBiomeTagsProvider(packOutput, datapackLookup, fileHelper));
        generator.addProvider(event.includeServer(), new NpcStructureTagsProvider(packOutput, datapackLookup, fileHelper));
        generator.addProvider(event.includeServer(), new MKNpcDefinitionProvider(generator, datapackLookup));
        generator.addProvider(event.includeServer(), new NpcEntityTypeTagsProvider(packOutput, datapackLookup, fileHelper));


        // pack.mcmeta
        generator.addProvider(true, new PackMetadataGenerator(packOutput)
                .add(PackMetadataSection.TYPE, new PackMetadataSection(
                        Component.literal("MKNpc resources"),
                        DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA),
                        Optional.of(new InclusiveRange<>(0, Integer.MAX_VALUE))
                ))
        );
    }

    public static class MKNpcDefinitionProvider extends NpcDefinitionProvider {

        public MKNpcDefinitionProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(generator, lookupProvider, MKNpc.MODID);
        }

        @Override
        public CompletableFuture<?> run(CachedOutput pOutput) {
            return CompletableFuture.allOf(
                    writeDefinition(generateTestSkeleton(), pOutput),
                    writeDefinition(generateTestLady(), pOutput),
                    writeDefinition(generateTestLady2(), pOutput),
                    writeDefinition(generateTestGhostSkeleton(), pOutput)
            );
        }

        private NpcDefinition generateTestSkeleton() {
            NpcDefinition def = new NpcDefinition(MKNpc.id("test_skeleton"), MKNpcEntityTypes.SKELETON_TYPE);
            def.addOption(new FactionOption(MKFactions.UNDEAD));
            def.addOption(new MKSizeOption(0.25f));
            def.addOption(new RenderGroupOption("wither_king"));
            return def;
        }

        private NpcDefinition generateTestGhostSkeleton() {
            NpcDefinition def = new NpcDefinition(MKNpc.id("test_ghost"), MKNpcEntityTypes.SKELETON_TYPE);
            def.addOption(new FactionOption(MKFactions.UNDEAD));
            def.addOption(new EquipmentOption()
                    .addItemChoice(EquipmentSlot.CHEST, new NpcItemChoice(new ItemStack(Items.IRON_CHESTPLATE), 5, 1.1f))
            );
            def.addOption(new GhostOption());
            return def;
        }

        private static final ResourceLocation TEST_LADY_ID = MKNpc.id("test");

        private NpcDefinition generateTestLady() {
            NpcDefinition def = new NpcDefinition(TEST_LADY_ID, EntityType.VILLAGER);
            def.addOption(new NameOption("Test Lady"));
            def.addOption(new AttributesOption().addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 100)));
            def.addOption(new FactionOption(MKFactions.VILLAGERS));
            def.addOption(new DialogueOption(ResourceLocation.fromNamespaceAndPath(MKChat.MODID, "test")));
            def.addOption(new EquipmentOption()
                    .addItemChoice(EquipmentSlot.MAINHAND, new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                            ResourceLocation.fromNamespaceAndPath(MKWeapons.MODID, "katana_iron"))), 5, 1.1f))
                    .addItemChoice(EquipmentSlot.MAINHAND, new NpcItemChoice(new ItemStack(BuiltInRegistries.ITEM.get(
                            ResourceLocation.fromNamespaceAndPath(MKWeapons.MODID, "dagger_iron"))), 10, 1.1f))
            );
            return def;
        }

        private NpcDefinition generateTestLady2() {
            NpcDefinition def = NpcDefinition.derived(MKNpc.id("test2"), TEST_LADY_ID);
            def.addOption(new FactionOption(MKFactions.UNDEAD));
            def.addOption(new NotableOption());
            def.addOption(new FactionNameOption().setHasLastName(true).setTitle("Chief"));
            return def;
        }
    }
}
