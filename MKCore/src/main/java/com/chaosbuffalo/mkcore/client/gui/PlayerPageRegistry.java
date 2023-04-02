package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.InterModComms;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PlayerPageRegistry {

    public interface ExtensionProvider extends Supplier<Extension> {
    }

    public interface Extension {
        ResourceLocation getId();

        Component getDisplayName();

        MKScreen createPage(MKPlayerData playerData);
    }

    private static class InternalPageEntry {
        public final Extension extension;
        public final int priority;

        public InternalPageEntry(Extension extension, int priority) {
            this.extension = extension;
            this.priority = priority;
        }
    }

    private static final List<InternalPageEntry> extensions = new ArrayList<>(6);

    private static void registerIMC(InterModComms.IMCMessage m) {
        PlayerPageRegistry.ExtensionProvider factory = m.<PlayerPageRegistry.ExtensionProvider>getMessageSupplier().get();
        Extension extension = factory.get();
        MKCore.LOGGER.info("Found IMC player page extension: {}", extension.getId());
        addExtension(extension, 10);
    }

    private static void addExtension(Extension extension, int priority) {
        extensions.add(new InternalPageEntry(extension, priority));
    }

    private static void registerInternal(ResourceLocation name, Function<MKPlayerData, MKScreen> factory, int priority) {
        addExtension(new Extension() {
            @Override
            public ResourceLocation getId() {
                return name;
            }

            @Override
            public Component getDisplayName() {
                return new TranslatableComponent(String.format("mkcore.gui.character.%s", getId().getPath()));
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
                .filter(e -> e.extension.getId().equals(name))
                .findFirst()
                .map(e -> e.extension.createPage(playerData))
                .orElse(null);
    }

    public static List<Extension> getAllPages() {
        // Sort by priority followed by display name
        Comparator<InternalPageEntry> comp = Comparator.comparing(e -> e.priority);
        comp = comp.thenComparing(e -> e.extension.getDisplayName().getString());
        return extensions.stream()
                .sorted(comp)
                .map(e -> e.extension)
                .collect(Collectors.toList());
    }

    public static void openPlayerScreen(MKPlayerData playerData, ResourceLocation name) {
        MKScreen screen = createPage(playerData, name);
        Minecraft.getInstance().setScreen(screen);
    }

    public static void openDefaultPlayerScreen(MKPlayerData playerData) {
        openPlayerScreen(playerData, MKCore.makeRL("abilities"));
    }

    public static void checkClientIMC() {
        InterModComms.getMessages(MKCore.MOD_ID, m -> m.equals(MKCore.REGISTER_PLAYER_PAGE))
                .forEach(PlayerPageRegistry::registerIMC);
    }
}
