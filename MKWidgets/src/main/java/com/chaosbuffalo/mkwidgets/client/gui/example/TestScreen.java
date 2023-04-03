package com.chaosbuffalo.mkwidgets.client.gui.example;

import com.chaosbuffalo.mkwidgets.MKWidgets;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.*;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class TestScreen extends MKScreen {
    private final int PANEL_WIDTH = 320;
    private final int PANEL_HEIGHT = 240;
    private static final ResourceLocation BG_LOC = new ResourceLocation(MKWidgets.MODID,
            "textures/gui/background_320.png");
    private static final ResourceLocation CB_LOGO = new ResourceLocation(MKWidgets.MODID,
            "textures/gui/chaosbuffalologo.png");
    private MKModal testPopup;

    public TestScreen(Component title) {
        super(title);
    }

    public MKLayout getIntro(int xPos, int yPos) {
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(5, 5, 5, 5);
        root.setPaddingTop(5).setPaddingBot(5);
        MKText introText = new MKText(font, "Welcome to the MK Widgets toolkit demo.");
        introText.setIsCentered(true).setMultiline(true).setWidth(PANEL_WIDTH / 2);
        root.addWidget(introText);
        root.addConstraintToWidget(new LayoutRelativeYPosConstraint(.3f), introText);
        root.addConstraintToWidget(new CenterXConstraint(), introText);
        MKButton textListDemo = new MKButton("Text List Demo");
        root.addWidget(textListDemo);
        root.addConstraintToWidget(StackConstraint.VERTICAL, textListDemo);
        root.addConstraintToWidget(new CenterXConstraint(), textListDemo);
        textListDemo.setPressedCallback((button, mouseButton) -> {
            pushState("testList");
            return true;
        });
        MKButton imageBoxDemo = new MKButton("Image Box Demo");
        root.addWidget(imageBoxDemo);
        root.addConstraintToWidget(StackConstraint.VERTICAL, imageBoxDemo);
        root.addConstraintToWidget(new CenterXConstraint(), imageBoxDemo);
        imageBoxDemo.setPressedCallback((button, mouseButton) -> {
            pushState("imageBox");
            return true;
        });
        MKButton popupDemo = new MKButton("Popup Demo");
        root.addWidget(popupDemo);
        root.addConstraintToWidget(StackConstraint.VERTICAL, popupDemo);
        root.addConstraintToWidget(new CenterXConstraint(), popupDemo);
        popupDemo.setPressedCallback((button, mouseButton) -> {
            pushState("popupDemo");
            return true;
        });
        MKButton tooltipDemo = new MKButton("Tooltip Demo");
        root.addWidget(tooltipDemo);
        root.addConstraintToWidget(StackConstraint.VERTICAL, tooltipDemo);
        root.addConstraintToWidget(new CenterXConstraint(), tooltipDemo);
        tooltipDemo.setPressedCallback((button, mouseButton) -> {
            pushState("tooltipDemo");
            return true;
        });
        return root;
    }

    private MKLayout getToolTipTest(int xPos, int yPos) {
        MKText textWithLongHover = new MKText(font, "This text will have tooltip.");
        textWithLongHover.setTooltip("This is a tooltip.");
        MKLayout root = getRootWithTitle(xPos, yPos, "Tooltip Test");
        root.addWidget(textWithLongHover);
        root.addConstraintToWidget(new CenterXConstraint(), textWithLongHover);
        root.addConstraintToWidget(new LayoutRelativeYPosConstraint(.5f), textWithLongHover);
        addBackButton(root);
        return root;

    }

    private MKLayout getPopupTest(int xPos, int yPos) {
        MKLayout root = getRootWithTitle(xPos, yPos, "Popup Test");
        MKButton openPopup = new MKButton("Open Popup");
        root.addWidget(openPopup);
        root.addConstraintToWidget(StackConstraint.VERTICAL, openPopup);
        root.addConstraintToWidget(new CenterXConstraint(), openPopup);
        testPopup = new MKModal();
        MKLayout popupContents = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        testPopup.addWidget(popupContents);
        MKButton closePopup = new MKButton("Close Popup");
        MKText reflectText = new MKText(font, "", 200).setIsCentered(true);
        MKTextFieldWidget textInput = new MKTextFieldWidget(font, xPos, yPos, 200, 20,
                new TextComponent("test input"));

        textInput.setTextChangeCallback((wid, text) -> {
            reflectText.setText(text);
        });
        popupContents.addWidget(textInput);
        popupContents.addConstraintToWidget(new CenterXConstraint(), textInput);
        popupContents.addConstraintToWidget(new LayoutRelativeYPosConstraint(.25f), textInput);
        popupContents.addWidget(reflectText);
        popupContents.addConstraintToWidget(new CenterXConstraint(), reflectText);
        popupContents.addConstraintToWidget(new LayoutRelativeYPosConstraint(.5f), reflectText);
        popupContents.addWidget(closePopup);
        popupContents.addConstraintToWidget(new CenterXConstraint(), closePopup);
        popupContents.addConstraintToWidget(new LayoutRelativeYPosConstraint(.75f), closePopup);
        closePopup.setPressedCallback((button, mouseButton) -> {
            this.closeModal(testPopup);
            return true;
        });
        openPopup.setPressedCallback((button, mouseButton) -> {
            this.addModal(testPopup);
            return true;
        });
        addBackButton(root);
        return root;
    }

    private MKLayout getImageBox(int xPos, int yPos, int width, int height, ResourceLocation imageLoc,
                                 MarginConstraint verticalMargin,
                                 MarginConstraint horizontalMargin) {

        MKLayout root = new MKLayout(xPos, yPos, width, height);
        // we pass in the original image size here
        MKImage image = new MKImage(xPos, yPos, 400, 400, imageLoc);
        root.addWidget(image);
        root.addConstraintToWidget(new LayoutRelativeWidthConstraint(.5f), image);
        root.addConstraintToWidget(new LayoutRelativeHeightConstraint(.5f), image);
        root.addConstraintToWidget(verticalMargin, image);
        root.addConstraintToWidget(horizontalMargin, image);
        root.setDrawDebug(true);
        root.setDebugColor(0xff00ffff);
        return root;
    }

    private MKLayout get2ImageBoxRow(int height, ResourceLocation image1, ResourceLocation image2,
                                     MarginConstraint verticalMargin) {
        MKStackLayoutHorizontal row = new MKStackLayoutHorizontal(0, 0, height);
        row.setPaddings(5, 5, 5, 5);
        MKLayout img1 = getImageBox(0, 0, height, height, image1, verticalMargin, MarginConstraint.LEFT);
        MKLayout img2 = getImageBox(0, 0, height, height, image2, verticalMargin, MarginConstraint.RIGHT);
        row.addWidget(img1);
        row.addWidget(img2);
        return row;
    }

    private MKLayout getImageBoxLayout(int rowHeight, ResourceLocation image) {
        MKStackLayoutVertical layout = new MKStackLayoutVertical(0, 0, rowHeight * 2 + 30);
        layout.setMargins(10, 10, 10, 10);
        layout.setPaddings(5, 5, 5, 5);
        MKLayout row1 = get2ImageBoxRow(rowHeight, image, image, MarginConstraint.TOP);
        MKLayout row2 = get2ImageBoxRow(rowHeight, image, image, MarginConstraint.BOTTOM);
        layout.addWidget(row1);
        layout.addWidget(row2);
        return layout;
    }

    private MKLayout getRootWithTitle(int xPos, int yPos, String title) {
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(5, 5, 5, 5);
        root.setPaddingBot(10);
        root.setPaddingTop(10);
        MKText titleText = new MKText(font, title);
        root.addWidget(titleText);
        root.addConstraintToWidget(MarginConstraint.TOP, titleText);
        root.addConstraintToWidget(new CenterXConstraint(), titleText);
        return root;
    }

    private MKLayout imageBoxDemo(int xPos, int yPos) {
        MKLayout root = getRootWithTitle(xPos, yPos, "Image Box Demo");
        MKLayout imageBox = getImageBoxLayout(50, CB_LOGO);
        imageBox.manualRecompute();
        root.addWidget(imageBox);
        root.addConstraintToWidget(new CenterXConstraint(), imageBox);
        root.addConstraintToWidget(StackConstraint.VERTICAL, imageBox);
        addBackButton(root);
        return root;
    }

    private void addBackButton(MKLayout layout) {
        MKButton back = new MKButton("Back to Main");
        layout.addWidget(back);
        layout.addConstraintToWidget(MarginConstraint.BOTTOM, back);
        layout.addConstraintToWidget(new CenterXConstraint(), back);
        back.setPressedCallback((button, mouseButton) -> {
            popState();
            return true;
        });
    }


    public MKLayout textListDemo(int xPos, int yPos) {
        MKLayout root = getRootWithTitle(xPos, yPos, "Scrollable List Demo");
        MKScrollView scrollView = new MKScrollView(0, 0, 120, 100, false);
        root.addWidget(scrollView);
        scrollView.setScrollVelocity(3.0);
        root.addConstraintToWidget(StackConstraint.VERTICAL, scrollView);
        root.addConstraintToWidget(new CenterXConstraint(), scrollView);
        MKStackLayoutVertical verticalLayout = new MKStackLayoutVertical(0, 0, 120);
        verticalLayout.doSetChildWidth(true).setPaddingBot(5).setMarginTop(5).setMarginRight(5).setMarginLeft(5).setMarginBot(5);
        for (int i = 0; i < 25; i++) {
            String buttonText = String.format("Test Text: %d", i);
            MKText testText = new MKText(this.font, buttonText);
            testText.setTooltip(buttonText);
            testText.setIsCentered(true);
            testText.setDebugColor(0x3f0000ff);
            verticalLayout.addWidget(testText);
        }
        verticalLayout.manualRecompute();
        // we need to resolve constraints so we can center scrollview content properly
        root.manualRecompute();
        scrollView.addWidget(verticalLayout);
        scrollView.centerContentX();
        scrollView.setToTop();
        addBackButton(root);
        return root;
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        addState("intro", () -> getIntro(xPos, yPos));
        addState("testList", () -> textListDemo(xPos, yPos));
        addState("imageBox", () -> imageBoxDemo(xPos, yPos));
        addState("popupDemo", () -> getPopupTest(xPos, yPos));
        addState("tooltipDemo", () -> getToolTipTest(xPos, yPos));
        pushState("intro");
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, BG_LOC);
        MKAbstractGui.mkBlitUVSizeSame(matrixStack, xPos, yPos, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, 512, 512);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
