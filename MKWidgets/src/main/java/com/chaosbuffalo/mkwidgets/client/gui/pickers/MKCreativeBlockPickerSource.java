package com.chaosbuffalo.mkwidgets.client.gui.pickers;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MKCreativeBlockPickerSource implements MKCreativePickerSource {
    public static final String ALL_CATEGORY_ID = "all";

    private List<MKCreativePickerCategory> cachedCategories = List.of();
    private final Map<String, List<MKCreativePickerEntry>> entriesByCategory = new LinkedHashMap<>();

    @Override
    public List<MKCreativePickerCategory> categories(Minecraft minecraft) {
        rebuild(minecraft);
        return cachedCategories;
    }

    @Override
    public List<MKCreativePickerEntry> entries(Minecraft minecraft, MKCreativePickerCategory category, String query) {
        rebuild(minecraft);
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<MKCreativePickerEntry> entries = entriesByCategory.getOrDefault(category.id(), List.of());
        if (normalizedQuery.isEmpty()) {
            return entries;
        }
        return entries.stream()
                .filter(entry -> entry.searchText().contains(normalizedQuery))
                .toList();
    }

    private void rebuild(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.player.connection == null || minecraft.level == null) {
            cachedCategories = List.of();
            entriesByCategory.clear();
            return;
        }

        CreativeModeTabs.tryRebuildTabContents(
                minecraft.player.connection.enabledFeatures(),
                minecraft.player.canUseGameMasterBlocks(),
                minecraft.level.registryAccess()
        );

        LinkedHashMap<ResourceLocation, MKCreativePickerEntry> allEntries = new LinkedHashMap<>();
        LinkedHashMap<String, List<MKCreativePickerEntry>> rebuiltEntries = new LinkedHashMap<>();
        List<MKCreativePickerCategory> rebuiltCategories = new ArrayList<>();

        for (CreativeModeTab tab : CreativeModeTabs.tabs()) {
            LinkedHashMap<ResourceLocation, MKCreativePickerEntry> tabEntries = new LinkedHashMap<>();
            for (ItemStack stack : tab.getDisplayItems()) {
                if (!(stack.getItem() instanceof BlockItem blockItem)) {
                    continue;
                }
                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
                MKCreativePickerEntry entry = new MKCreativePickerEntry(
                        blockId,
                        stack.getHoverName(),
                        stack.copy(),
                        searchableText(blockId, stack)
                );
                tabEntries.putIfAbsent(blockId, entry);
                allEntries.putIfAbsent(blockId, entry);
            }
            if (!tabEntries.isEmpty()) {
                String categoryId = categoryId(tab);
                rebuiltCategories.add(new MKCreativePickerCategory(categoryId, tab.getDisplayName(), tab.getIconItem()));
                rebuiltEntries.put(categoryId, List.copyOf(tabEntries.values()));
            }
        }

        if (!allEntries.isEmpty()) {
            MKCreativePickerCategory allCategory = new MKCreativePickerCategory(
                    ALL_CATEGORY_ID,
                    Component.literal("All"),
                    allEntries.values().iterator().next().displayStack()
            );
            rebuiltCategories.add(0, allCategory);
            rebuiltEntries.put(ALL_CATEGORY_ID, List.copyOf(allEntries.values()));
        }

        cachedCategories = List.copyOf(rebuiltCategories);
        entriesByCategory.clear();
        entriesByCategory.putAll(rebuiltEntries);
    }

    private String categoryId(CreativeModeTab tab) {
        return BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab).toString();
    }

    private String searchableText(ResourceLocation blockId, ItemStack stack) {
        return (blockId + " " + stack.getHoverName().getString()).toLowerCase(Locale.ROOT);
    }
}
