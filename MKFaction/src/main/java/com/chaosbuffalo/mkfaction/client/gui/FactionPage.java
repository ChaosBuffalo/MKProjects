package com.chaosbuffalo.mkfaction.client.gui;

import com.chaosbuffalo.mkcore.client.gui.PlayerPageBase;
import com.chaosbuffalo.mkcore.client.gui.PlayerPageRegistry;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkfaction.MKFactionMod;
import com.chaosbuffalo.mkfaction.capabilities.FactionCapabilities;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.chaosbuffalo.mkfaction.faction.PlayerFactionEntry;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fml.InterModComms;

import java.util.Comparator;
import java.util.List;

public class FactionPage extends PlayerPageBase {

    protected MKScrollView scrollView;

    public FactionPage(MKPlayerData playerData) {
        super(playerData, new TextComponent("Factions"));
    }

    @Override
    public ResourceLocation getPageId() {
        return new ResourceLocation(MKFactionMod.MODID, "factions");
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        addWidget(createScrollingPanelWithContent(this::createFactionEntryList, this::setupFactionHeader, v -> scrollView = v));
    }

    @Override
    protected void persistState(boolean wasResized) {
        super.persistState(wasResized);
        persistScrollView(() -> scrollView, wasResized);
    }

    public MKLayout getFactionEntryLayout(PlayerFactionEntry entry, int width) {
        MKFaction faction = entry.getFaction();
        MKLayout entryLayout = new MKLayout(0, 0, width, font.lineHeight + 10);
        entryLayout.setMargins(5, 5, 5, 5);

        Component nameText = faction.getDisplayName();
        MKText factionName = new MKText(font, nameText);
        factionName.setColor(0xffffffff);
        factionName.setWidth(font.width(nameText));
        entryLayout.addWidget(factionName, MarginConstraint.TOP, MarginConstraint.LEFT);

        Component valueText = entry.getStatusDisplayName()
                .append(String.format(" (%d)", entry.getFactionScore()))
                .withStyle(entry.getFactionStatus().getColor());
        MKText factionValue = new MKText(font, valueText);
        factionValue.setWidth(font.width(valueText));
        entryLayout.addWidget(factionValue, MarginConstraint.TOP, MarginConstraint.RIGHT);
        return entryLayout;
    }

    private MKWidget createFactionEntryList(MKPlayerData pData, int panelWidth) {
        MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(0, 0, panelWidth);
        stackLayout.setMargins(4, 4, 4, 4);
        stackLayout.setPaddingTop(2).setPaddingBot(2);
        stackLayout.doSetChildWidth(true);

        pData.getEntity().getCapability(FactionCapabilities.PLAYER_FACTION_CAPABILITY).ifPresent(playerFaction -> {
            List<PlayerFactionEntry> factions = ImmutableList.copyOf(playerFaction.getFactionMap().values());
            factions.stream()
                    .sorted(Comparator.comparing(entry -> entry.getFaction().getDisplayName().getString()))
                    .map(entry -> getFactionEntryLayout(entry, panelWidth - 10))
                    .forEach(stackLayout::addWidget);
        });
        return stackLayout;
    }

    public void setupFactionHeader(MKPlayerData playerData, MKLayout layout) {

    }

    static class PageFactory implements PlayerPageRegistry.Extension {

        @Override
        public ResourceLocation getId() {
            return new ResourceLocation(MKFactionMod.MODID, "factions");
        }

        @Override
        public Component getDisplayName() {
            return new TextComponent("Factions");
        }

        @Override
        public MKScreen createPage(MKPlayerData playerData) {
            return new FactionPage(playerData);
        }
    }

    public static void registerPlayerPage() {
        PlayerPageRegistry.ExtensionProvider provider = PageFactory::new;
        InterModComms.sendTo("mkcore", "register_player_page", () -> {
            MKFactionMod.LOGGER.debug("Faction register player page");
            return provider;
        });
    }
}
