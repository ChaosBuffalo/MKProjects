package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.gui.widgets.*;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeRecord;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.OffsetConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKRectangle;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import com.chaosbuffalo.mkwidgets.utils.TextureRegion;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Comparator;

public class TalentPage extends PlayerPageBase {

    private TalentTreeWidget talentTreeWidget;
    private TalentTreeRecord currentTree;
    private ScrollingListPanelLayout talentScrollPanel;

    public TalentPage(MKPlayerData playerData) {
        super(playerData, new TextComponent("Talents"));
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        addWidget(createTalentsPage());
    }

    @Override
    public ResourceLocation getPageId() {
        return MKCore.makeRL("talents");
    }

    private MKWidget createTalentsPage() {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        TextureRegion dataBoxRegion = GuiTextures.CORE_TEXTURES.getRegion(GuiTextures.DATA_BOX);
        if (dataBoxRegion == null) {
            return new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        }
        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(GuiTextures.DATA_BOX, GuiTextures.BACKGROUND_320_240);
        MKLayout root = getRootLayout(xPos, yPos, xOffset, dataBoxRegion.width, true);

        int contentX = xPos + xOffset;
        int contentY = yPos + DATA_BOX_OFFSET;
        int contentWidth = dataBoxRegion.width;
        int contentHeight = dataBoxRegion.height;
        MKStackLayoutHorizontal xpBarTray = createXpBar(playerData, contentX, contentY);
        root.addWidget(xpBarTray);

        MKStackLayoutHorizontal fieldTray = new MKStackLayoutHorizontal(contentX, contentY - 16, 12);
        fieldTray.setPaddingLeft(10);
        fieldTray.setPaddingRight(10);
        fieldTray.setMargins(10, 10, 0, 0);
        root.addWidget(fieldTray);
        NamedField totalTalents = new NamedField(0, 0, "Total Talents:",
                0xff000000,
                Integer.toString(playerData.getTalents().getTotalTalentPoints()),
                0xff000000, font);
        NamedField unspentTalents = new NamedField(0, 0, "Unspent Talents:",
                0xff000000,
                Integer.toString(playerData.getTalents().getUnspentTalentPoints()),
                0xff000000, font);
        fieldTray.addWidget(unspentTalents);
        fieldTray.addWidget(totalTalents);

        talentScrollPanel = new ScrollingListPanelLayout(contentX, contentY, contentWidth, contentHeight);
        talentTreeWidget = new TalentTreeWidget(0, 0,
                talentScrollPanel.getContentScrollView().getWidth(),
                talentScrollPanel.getContentScrollView().getHeight(), font, this::getCurrentTree);
        talentScrollPanel.setContent(talentTreeWidget);

        MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(0, 0, talentScrollPanel.getListScrollView().getWidth());
        stackLayout.setMargins(4, 4, 4, 4);
        stackLayout.setPaddingTop(2).setPaddingBot(2).setPaddingRight(2);
        stackLayout.doSetChildWidth(true);

        playerData.getTalents().getKnownTrees().stream()
                .map(treeId -> playerData.getTalents().getTree(treeId))
                .sorted(Comparator.comparing(info -> info.getTreeDefinition().getName().getString()))
                .forEach(record -> {
                    MKLayout talentEntry = new TalentListEntry(0, 0, 16, font, this, record);
                    stackLayout.addWidget(talentEntry);
                    MKRectangle div = new MKRectangle(0, 0,
                            talentScrollPanel.getListScrollView().getWidth() - 8, 1, 0x99ffffff);
                    stackLayout.addWidget(div);
                });
        talentScrollPanel.setList(stackLayout);
        root.addWidget(talentScrollPanel);

        return root;
    }

    @Nonnull
    private MKStackLayoutHorizontal createXpBar(MKPlayerData pData, int contentX, int contentY) {
        MKStackLayoutHorizontal xpBarTray = new MKStackLayoutHorizontal(contentX, contentY - 36, 11);
        xpBarTray.setPaddingLeft(10);
        MKText xpBarText = new MKText(font, new TranslatableComponent("mkcore.gui.xp_bar.name"));
        xpBarText.setWidth(font.width(I18n.get("mkcore.gui.xp_bar.name")));
        xpBarTray.setMarginLeft(11);
        xpBarTray.addWidget(xpBarText);
        xpBarTray.addConstraintToWidget(new OffsetConstraint(0, 2, false, true), xpBarText);
        XpBarWidget xpBarWidget = new XpBarWidget(0, 0, 67, 11);
        xpBarWidget.syncPlayerXp(pData);
        xpBarTray.addWidget(xpBarWidget);
        return xpBarTray;
    }

    @Override
    protected void persistState(boolean wasResized) {
        final TalentTreeRecord current = getCurrentTree();
        addPostSetupCallback(() -> restoreCurrentTree(current));
        persistScrollingListPanelState(() -> talentScrollPanel, wasResized);
    }

    public TalentTreeRecord getCurrentTree() {
        return currentTree;
    }

    private void restoreCurrentTree(TalentTreeRecord currentTree) {
        this.currentTree = currentTree;
        if (talentTreeWidget != null) {
            talentTreeWidget.refresh();
        }
    }

    public void setCurrentTree(TalentTreeRecord newTree) {
        restoreCurrentTree(newTree);
        talentScrollPanel.getContentScrollView().resetView();
    }
}
