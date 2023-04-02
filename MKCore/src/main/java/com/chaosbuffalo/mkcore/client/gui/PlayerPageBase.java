package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.client.gui.widgets.ScrollingListPanelLayout;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import com.chaosbuffalo.mkwidgets.utils.TextureRegion;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class PlayerPageBase extends MKScreen implements IPlayerDataAwareScreen {
    protected final int PANEL_WIDTH = 320;
    protected final int PANEL_HEIGHT = 240;
    protected final int DATA_BOX_OFFSET = 78;
    protected final int POPUP_WIDTH = 180;
    protected final int POPUP_HEIGHT = 200;
    protected final int STATE_SWITCHER_Y_OFFSET = 8;


    @Nonnull
    protected MKPlayerData playerData;
    private boolean wasResized;

    public PlayerPageBase(MKPlayerData playerData, Component title) {
        super(title);
        this.playerData = Preconditions.checkNotNull(playerData, "Must pass a non-null PlayerData to create a screen");
    }

    public abstract ResourceLocation getPageId();

    public void switchState(ResourceLocation newState) {
        MKScreen next = PlayerPageRegistry.createPage(playerData, newState);
        getMinecraft().setScreen(next);
    }

    protected String getDataBoxTexture() {
        return GuiTextures.DATA_BOX;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        GuiTextures.CORE_TEXTURES.bind(getMinecraft());
        GuiTextures.CORE_TEXTURES.drawRegionAtPos(matrixStack, GuiTextures.BACKGROUND_320_240, xPos, yPos);
        drawDataBox(matrixStack, xPos, yPos);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    protected void drawDataBox(PoseStack matrixStack, int xPos, int yPos) {
        String dataBoxTex = getDataBoxTexture();
        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(dataBoxTex, GuiTextures.BACKGROUND_320_240);
        GuiTextures.CORE_TEXTURES.drawRegionAtPos(matrixStack, dataBoxTex, xPos + xOffset, yPos + DATA_BOX_OFFSET);
    }

    static class StateSwitcher extends MKStackLayoutHorizontal {
        static int ROW_HEIGHT = 24;

        public StateSwitcher(PlayerPageBase currentPage, int x, int y) {
            super(x, y, ROW_HEIGHT);
            setMargins(2, 2, 1, 1);
            setPaddingLeft(2).setPaddingRight(2);
            for (PlayerPageRegistry.Extension otherPage : PlayerPageRegistry.getAllPages()) {
                MKButton button = new MKButton(otherPage.getDisplayName());
                button.setWidth(currentPage.font.width(otherPage.getDisplayName()) + 10);
                button.setEnabled(!otherPage.getId().equals(currentPage.getPageId()));
                button.setPressedCallback((btn, mouseButton) -> {
                    currentPage.switchState(otherPage.getId());
                    return true;
                });
                addWidget(button);
            }
        }
    }

    protected MKLayout getRootLayout(int xPos, int yPos, int xOffset, int width, boolean addStateButtons) {
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(5, 5, 5, 5);
        root.setPaddingTop(5).setPaddingBot(5);
        if (addStateButtons) {
            MKLayout switcher = new StateSwitcher(this, xPos + xOffset, yPos + STATE_SWITCHER_Y_OFFSET);
            root.addWidget(switcher);
        }
        return root;
    }

    protected MKLayout createStateSwitcher(int xPos, int yPos) {
        return new StateSwitcher(this, xPos, yPos);
    }

    @Deprecated
    protected MKLayout createScrollingPanelWithContent(BiFunction<MKPlayerData, Integer, MKWidget> contentCreator,
                                                       BiConsumer<MKPlayerData, MKLayout> headerCreator) {
        return createScrollingPanelWithContent(contentCreator, headerCreator, v -> {
        });
    }

    protected MKLayout createScrollingPanelWithContent(BiFunction<MKPlayerData, Integer, MKWidget> contentCreator,
                                                       BiConsumer<MKPlayerData, MKLayout> headerCreator,
                                                       Consumer<MKScrollView> scrollViewConsumer) {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(5, 5, 5, 5);
        root.setPaddingTop(5).setPaddingBot(5);

        TextureRegion dataBoxRegion = GuiTextures.CORE_TEXTURES.getRegion(getDataBoxTexture());
        if (dataBoxRegion == null) {
            return root;
        }

        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(dataBoxRegion.regionName, GuiTextures.BACKGROUND_320_240);
        MKLayout switcher = createStateSwitcher(xPos + xOffset, yPos + STATE_SWITCHER_Y_OFFSET);
        root.addWidget(switcher);

        // Stat Panel
        MKLayout headerLayout = new MKLayout(xPos, switcher.getY() + switcher.getHeight(), PANEL_WIDTH,
                DATA_BOX_OFFSET - switcher.getHeight() - STATE_SWITCHER_Y_OFFSET);
        headerLayout.setMargins(4, 4, 0, 0);
        headerCreator.accept(playerData, headerLayout);
        root.addWidget(headerLayout);

        MKScrollView scrollView = new MKScrollView(xPos + xOffset + 4, yPos + DATA_BOX_OFFSET + 4,
                dataBoxRegion.width - 8, dataBoxRegion.height - 8, true);
        scrollView.addWidget(contentCreator.apply(playerData, dataBoxRegion.width - 8));
        scrollView.resetView();
        scrollViewConsumer.accept(scrollView);
        root.addWidget(scrollView);
        return root;
    }

    protected void persistScrollView(Supplier<MKScrollView> viewSupplier, boolean wasResized) {
        MKScrollView view = viewSupplier.get();
        if (view != null) {
            double offsetX = view.getOffsetX();
            double offsetY = view.getOffsetY();
            addPostSetupCallback(() -> {
                MKScrollView newView = viewSupplier.get();
                if (newView != null) {
                    if (wasResized) {
                        newView.resetView();
                    } else {
                        newView.setOffsetX(offsetX);
                        newView.setOffsetY(offsetY);
                    }
                }
            });
        }
    }

    protected void persistScrollingListPanelState(Supplier<ScrollingListPanelLayout> scroller, boolean wasResized) {
        ScrollingListPanelLayout panel = scroller.get();
        if (panel != null) {
            persistScrollView(() -> scroller.get().getListScrollView(), wasResized);
            persistScrollView(() -> scroller.get().getContentScrollView(), wasResized);
        }
    }

    @Override
    public void onPlayerDataUpdate() {
        flagNeedSetup();
    }

    protected void persistState(boolean wasResized) {

    }

    @Override
    public void setupScreen() {
        persistState(wasResized);
        super.setupScreen();
        wasResized = false;
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        wasResized = true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
