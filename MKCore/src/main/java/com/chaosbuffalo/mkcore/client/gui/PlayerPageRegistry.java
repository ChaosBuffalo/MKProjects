package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PlayerPageRegistry {

    public interface PageDefinition {
        ResourceLocation getId();

        Component getDisplayName();

        MKScreen createPage(MKPlayerData playerData);
    }

    private record InternalPageEntry(PageDefinition pageDefinition, int priority) {
    }

    private static final List<InternalPageEntry> extensions = new ArrayList<>(6);

    private static void addExtension(PageDefinition pageDefinition, int priority) {
        extensions.add(new InternalPageEntry(pageDefinition, priority));
    }

    /**
     * Do this from FMLClientSetupEvent using enqueueWork
     * @param pageDefinition page definition
     */
    public static void register(PageDefinition pageDefinition) {
        addExtension(pageDefinition, 10);
    }

    private static void registerInternal(ResourceLocation name, Function<MKPlayerData, MKScreen> factory, int priority) {
        final Component displayName = Component.translatable("mkcore.gui.character." + name.getPath());
        addExtension(new PageDefinition() {
            @Override
            public ResourceLocation getId() {
                return name;
            }

            @Override
            public Component getDisplayName() {
                return displayName;
            }

            @Override
            public MKScreen createPage(MKPlayerData playerData) {
                return factory.apply(playerData);
            }
        }, priority);
    }

    public static void init() {
        registerInternal(MKCore.makeRL("abilities"), PersonalAbilityPage::new, 1);
        registerInternal(MKCore.makeRL("talents"), TalentPage::new, 2);
        registerInternal(MKCore.makeRL("stats"), StatsPage::new, 3);
        registerInternal(MKCore.makeRL("damages"), DamagePage::new, 4);
    }

    @Nullable
    public static MKScreen createPage(MKPlayerData playerData, ResourceLocation name) {
        return extensions.stream()
                .filter(e -> e.pageDefinition.getId().equals(name))
                .findFirst()
                .map(e -> e.pageDefinition.createPage(playerData))
                .orElse(null);
    }

    public static List<PageDefinition> getAllPages() {
        // Sort by priority followed by display name
        Comparator<InternalPageEntry> comp = Comparator.comparing(e -> e.priority);
        comp = comp.thenComparing(e -> e.pageDefinition.getDisplayName().getString());
        return extensions.stream()
                .sorted(comp)
                .map(e -> e.pageDefinition)
                .collect(Collectors.toList());
    }

    public static void openPlayerScreen(MKPlayerData playerData, ResourceLocation name) {
        MKScreen screen = createPage(playerData, name);
        Minecraft.getInstance().setScreen(screen);
    }

    public static void openDefaultPlayerScreen(MKPlayerData playerData) {
        openPlayerScreen(playerData, MKCore.makeRL("abilities"));
    }
}
