package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.widgets.*;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nonnull;

public abstract class AbilityPageBase extends PlayerPageBase implements IAbilityScreen {
    protected MKAbility draggingAbility;
    protected AbilityInfoWidget infoWidget;
    protected ScrollingListPanelLayout abilitiesScrollPanel;
    private MKAbility selectedAbility;


    public AbilityPageBase(MKPlayerData playerData, Component title) {
        super(playerData, title);
    }

    protected String getDataBoxTexture() {
        return GuiTextures.DATA_BOX_SHORT;
    }

    protected abstract Iterable<MKAbility> getSortedAbilityList();

    public ScrollingListPanelLayout getAbilityScrollPanel(int xPos, int yPos, int width, int height) {
        ScrollingListPanelLayout panel = new ScrollingListPanelLayout(xPos, yPos, width, height);
        infoWidget = new AbilityInfoWidget(0, 0, panel.getContentScrollView().getWidth(), playerData, font, this);
        panel.setContent(infoWidget);

        MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(0, 0, panel.getListScrollView().getWidth());
        stackLayout.setMargins(4, 4, 4, 4);
        stackLayout.setPaddings(0, 2, 2, 2);
        stackLayout.doSetChildWidth(true);
        getSortedAbilityList()
                .forEach(ability -> {
                    MKLayout abilityEntry = new AbilityListEntry(0, 0, 16, font, this, ability);
                    stackLayout.addWidget(abilityEntry);
                    MKRectangle div = new MKRectangle(0, 0, panel.getListScrollView().getWidth() - 8, 1, 0x99ffffff);
                    stackLayout.addWidget(div);
                });
        panel.setList(stackLayout);
        return panel;
    }

    protected ForgetAbilityModal getChoosePoolSlotWidget(MKAbility tryingToLearn, int trainingId) {
        int screenWidth = getWidth();
        int screenHeight = getHeight();
        int xPos = (screenWidth - POPUP_WIDTH) / 2;
        int yPos = (screenHeight - POPUP_HEIGHT) / 2;
        return new ForgetAbilityModal(tryingToLearn, playerData, xPos, yPos, POPUP_WIDTH, POPUP_HEIGHT, font, trainingId);
    }

    protected MKButton createManageButton() {
        TranslatableComponent manageText = new TranslatableComponent("mkcore.gui.manage_memory");
        MKButton manage = new MKButton(0, 0, manageText);
        manage.setWidth(60);

        manage.setPressedCallback((but, click) -> {
            ForgetAbilityModal modal = getChoosePoolSlotWidget(null, -1);
            modal.setOnCloseCallback(() -> {
                setSelectedAbility(null);
                flagNeedSetup();
            });
            addModal(modal);
            return true;
        });
        return manage;
    }

    @Nonnull
    protected IconText createPoolUsageText() {
        TranslatableComponent poolUsageText = new TranslatableComponent("mkcore.gui.memory_pool",
                playerData.getAbilities().getCurrentPoolCount(), playerData.getAbilities().getAbilityPoolSize());
        IconText poolText = new IconText(0, 0, 16, poolUsageText, MKAbility.POOL_SLOT_ICON, font, 16, 2);
        poolText.setTooltip(new TranslatableComponent("mkcore.gui.memory_pool_tooltip"));
        poolText.manualRecompute();
        int margins = 100 - poolText.getWidth();
        poolText.setMarginLeft(margins / 2);
        poolText.setMarginRight(margins / 2);
        poolText.getText().setColor(0xff000000);
        return poolText;
    }

    @Override
    protected void persistState(boolean wasResized) {
        super.persistState(wasResized);
        final MKAbility selected = getSelectedAbility();
        addPostSetupCallback(() -> restoreSelectedAbility(selected));
        persistScrollingListPanelState(() -> abilitiesScrollPanel, wasResized);
    }

    @Override
    public boolean allowsDraggingAbilities() {
        return false;
    }

    public MKAbility getDraggingAbility() {
        return draggingAbility;
    }

    public void startDraggingAbility(MKAbility dragging) {
        this.draggingAbility = dragging;
    }

    protected void restoreSelectedAbility(MKAbility ability) {
        selectedAbility = ability;
        if (infoWidget != null) {
            infoWidget.refresh();
        }
    }

    public void setSelectedAbility(MKAbility ability) {
        restoreSelectedAbility(ability);
        abilitiesScrollPanel.getContentScrollView().resetView();
    }

    public MKAbility getSelectedAbility() {
        return selectedAbility;
    }

    public void stopDraggingAbility() {
        draggingAbility = null;
    }

    public boolean isDraggingAbility() {
        return draggingAbility != null;
    }
}
