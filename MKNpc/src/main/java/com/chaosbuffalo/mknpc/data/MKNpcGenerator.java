package com.chaosbuffalo.mknpc.data;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkfaction.init.MKFactions;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.data.generators.NpcRegistrySets;
import com.chaosbuffalo.mknpc.data.generators.tags.NpcBiomeTagsProvider;
import com.chaosbuffalo.mknpc.data.generators.tags.NpcStructureTagsProvider;
import com.chaosbuffalo.mknpc.npc.NpcAttributeEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcItemChoice;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.chaosbuffalo.mkweapons.MKWeapons;
import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKNpcGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        PackOutput packOutput = generator.getPackOutput();

        generator.addProvider(event.includeServer(), new NpcRegistrySets(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new NpcBiomeTagsProvider(packOutput, lookupProvider, fileHelper));
        generator.addProvider(event.includeServer(), new NpcStructureTagsProvider(packOutput, lookupProvider, fileHelper));
        generator.addProvider(event.includeServer(), new MKNpcDefinitionProvider(generator));


        // pack.mcmeta
        generator.addProvider(true, new PackMetadataGenerator(packOutput)
                .add(PackMetadataSection.TYPE, new PackMetadataSection(
                        Component.literal("MKNpc resources"),
                        DetectedVersion.BUILT_IN.getPackVersion(PackType.CLIENT_RESOURCES),
                        Arrays.stream(PackType.values()).collect(Collectors.toMap(Function.identity(), DetectedVersion.BUILT_IN::getPackVersion))
                ))
        );
    }

    public static class MKNpcDefinitionProvider extends NpcDefinitionProvider {

        public MKNpcDefinitionProvider(DataGenerator generator) {
            super(generator, MKNpc.MODID);
        }

        @Override
        public CompletableFuture<?> run(CachedOutput pOutput) {
            return CompletableFuture.allOf(
                    writeDefinition(generateTestSkeleton(), pOutput),
                    writeDefinition(generateTestLady(), pOutput),
                    writeDefinition(generateTestLady2(), pOutput)
            );
        }

        private NpcDefinition generateTestSkeleton() {
            NpcDefinition def = new NpcDefinition(new ResourceLocation(MKNpc.MODID, "test_skeleton"),
                    new ResourceLocation(MKNpc.MODID, "skeleton"), null);
            def.addOption(new FactionOption(MKFactions.UNDEAD_FACTION_NAME));
            def.addOption(new MKSizeOption(0.25f));
            def.addOption(new RenderGroupOption("wither_king"));
            return def;
        }

        private NpcDefinition generateTestLady() {
            NpcDefinition def = new NpcDefinition(new ResourceLocation(MKNpc.MODID, "test"),
                    new ResourceLocation(MKNpc.MODID, "green_lady"), null);
            def.addOption(new NameOption("Test Lady"));
            def.addOption(new AttributesOption().addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 100)));
            def.addOption(new FactionOption(MKFactions.VILLAGER_FACTION_NAME));
            def.addOption(new DialogueOption(new ResourceLocation(MKChat.MODID, "test")));
            def.addOption(new EquipmentOption()
                    .addItemChoice(EquipmentSlot.MAINHAND, new NpcItemChoice(new ItemStack(ForgeRegistries.ITEMS.getValue(
                            new ResourceLocation(MKWeapons.MODID, "katana_iron"))), 5, 1.1f))
                    .addItemChoice(EquipmentSlot.MAINHAND, new NpcItemChoice(new ItemStack(ForgeRegistries.ITEMS.getValue(
                            new ResourceLocation(MKWeapons.MODID, "dagger_iron"))), 10, 1.1f))
            );
            return def;
        }

        private NpcDefinition generateTestLady2() {
            NpcDefinition def = new NpcDefinition(new ResourceLocation(MKNpc.MODID, "test2"), null,
                    new ResourceLocation(MKNpc.MODID, "test"));
            def.addOption(new FactionOption(MKFactions.UNDEAD_FACTION_NAME));
            def.addOption(new NotableOption());
            def.addOption(new FactionNameOption().setHasLastName(true).setTitle("Chief"));
            return def;
        }
    }
}
