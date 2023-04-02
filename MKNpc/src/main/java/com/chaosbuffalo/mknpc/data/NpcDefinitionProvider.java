package com.chaosbuffalo.mknpc.data;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mkfaction.init.Factions;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcAttributeEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcItemChoice;
import com.chaosbuffalo.mknpc.npc.options.*;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.randomization.LootTier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;

public class NpcDefinitionProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator generator;

    public NpcDefinitionProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void run(@Nonnull HashCache cache) {
//        writeDefinition(generateTestLady(), cache);
//        writeDefinition(generateTestLady2(), cache);
        writeDefinition(generateTestSkeleton(), cache);

    }

    private NpcDefinition generateTestSkeleton(){
        NpcDefinition def = new NpcDefinition(new ResourceLocation(MKNpc.MODID, "test_skeleton"),
                new ResourceLocation(MKNpc.MODID, "skeleton"), null);
        def.addOption(new FactionOption().setValue(Factions.UNDEAD_FACTION_NAME));
        def.addOption(new MKSizeOption().setValue(0.25f));
        def.addOption(new RenderGroupOption().setValue("wither_king"));
        return def;
    }

    private NpcDefinition generateTestLady(){
        NpcDefinition def = new NpcDefinition(new ResourceLocation(MKNpc.MODID, "test"),
                new ResourceLocation(MKNpc.MODID, "green_lady"), null);
        def.addOption(new NameOption().setValue("Test Lady"));
        def.addOption(new AttributesOption().addAttributeEntry(new NpcAttributeEntry(Attributes.MAX_HEALTH, 100)));
        def.addOption(new FactionOption().setValue(Factions.VILLAGER_FACTION_NAME));
        def.addOption(new DialogueOption().setValue(new ResourceLocation(MKChat.MODID, "test")));
        def.addOption(new EquipmentOption()
                .addItemChoice(EquipmentSlot.MAINHAND, new NpcItemChoice(new ItemStack(ForgeRegistries.ITEMS.getValue(
                        new ResourceLocation(MKWeapons.MODID, "katana_iron"))), 5, 1.1f)
                ).addItemChoice(EquipmentSlot.MAINHAND, new NpcItemChoice(new ItemStack(ForgeRegistries.ITEMS.getValue(
                        new ResourceLocation(MKWeapons.MODID, "dagger_iron"))), 10, 1.1f))
        );
        return def;
    }

    private NpcDefinition generateTestLady2(){
        NpcDefinition def = new NpcDefinition(new ResourceLocation(MKNpc.MODID, "test2"), null,
                new ResourceLocation(MKNpc.MODID, "test"));
        def.addOption(new FactionOption().setValue(Factions.UNDEAD_FACTION_NAME));
        def.addOption(new NotableOption().setValue(true));
        def.addOption(new FactionNameOption().setHasLastName(true).setTitle("Chief"));
        return def;
    }

    public void writeDefinition(NpcDefinition definition, @Nonnull HashCache cache){
        Path outputFolder = this.generator.getOutputFolder();
        ResourceLocation key = definition.getDefinitionName();
        Path path = outputFolder.resolve("data/" + key.getNamespace() + "/mknpcs/" + key.getPath() + ".json");
        try {
            JsonElement element = definition.serialize(JsonOps.INSTANCE);
            DataProvider.save(GSON, cache, element, path);
        } catch (IOException e){
            MKNpc.LOGGER.error("Couldn't write npc definition {}", path, e);
        }
    }

    @Override
    public String getName() {
        return "MKNpc Npc Definitions";
    }
}
