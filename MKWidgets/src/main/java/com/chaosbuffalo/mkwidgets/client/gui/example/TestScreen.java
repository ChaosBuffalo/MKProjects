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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Demo screen showcasing the primary MKWidgets building blocks.
 * <p>
 * This class is not core infrastructure, but it is a useful reference for users learning how screens, layouts,
 * constraints, modals, scrolling, and tooltips fit together in practice.
 */
public class TestScreen extends MKScreen {
    private final int PANEL_WIDTH = 320;
    private final int PANEL_HEIGHT = 240;
    private static final ResourceLocation BG_LOC = ResourceLocation.fromNamespaceAndPath(MKWidgets.MODID,
            "textures/gui/background_320.png");
    private static final ResourceLocation CB_LOGO = ResourceLocation.fromNamespaceAndPath(MKWidgets.MODID,
            "textures/gui/chaosbuffalologo.png");
    private MKModal testPopup;

    /**
     * @param title screen title
     */
    public TestScreen(Component title) {
        super(title);
    }

    /**
     * Builds the intro state for the demo screen.
     *
     * @param xPos panel x position
     * @param yPos panel y position
     * @return intro layout root
     */
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

    /**
     * Builds the tooltip example state.
     * <p>
     * This example shows the simplest tooltip path in the library: attach tooltip text directly to a widget and
     * let the default long-hover behavior schedule the tooltip during the screen's post-render phase.
     *
     * @param xPos panel x position
     * @param yPos panel y position
     * @return tooltip demo root layout
     */
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

    /**
     * Builds the modal popup example state.
     * <p>
     * This example demonstrates that modals are just widgets attached through the screen's modal stack. The
     * popup content itself is a normal layout tree containing a text field, a live-updating text label, and a
     * close button.
     *
     * @param xPos panel x position
     * @param yPos panel y position
     * @return popup demo root layout
     */
    private MKLayout getPopupTest(int xPos, int yPos) {
        MKLayout root = getRootWithTitle(xPos, yPos, "Popup Test");
        MKButton openPopup = new MKButton("Open Popup");
        root.addWidget(openPopup);
        root.addConstraintToWidget(StackConstraint.VERTICAL, openPopup);
        root.addConstraintToWidget(new CenterXConstraint(), openPopup);

        // The modal itself is a full-screen overlay; the layout inside it holds the actual popup content.
        testPopup = new MKModal();
        MKLayout popupContents = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        testPopup.addWidget(popupContents);
        MKButton closePopup = new MKButton("Close Popup");
        MKText reflectText = new MKText(font, "", 200).setIsCentered(true);
        MKTextFieldWidget textInput = new MKTextFieldWidget(font, xPos, yPos, 200, 20,
                Component.literal("test input"));

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

    /**
     * Builds a small image demo panel that constrains one image inside a boxed layout.
     * <p>
     * This helper shows how image widgets can be resized relative to a parent layout and then anchored using
     * composable constraints. Debug bounds are enabled to make the layout geometry visible while experimenting.
     *
     * @param xPos panel x position
     * @param yPos panel y position
     * @param width box width
     * @param height box height
     * @param imageLoc image resource to render
     * @param verticalMargin vertical edge constraint for the image
     * @param horizontalMargin horizontal edge constraint for the image
     * @return image box layout
     */
    private MKLayout getImageBox(int xPos, int yPos, int width, int height, ResourceLocation imageLoc,
                                 MarginConstraint verticalMargin,
                                 MarginConstraint horizontalMargin) {

        MKLayout root = new MKLayout(xPos, yPos, width, height);
        // The source texture is 400x400, but the widget is then resized relative to the containing layout.
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

    /**
     * Builds a horizontal row containing two image boxes.
     * <p>
     * This helper demonstrates composition of specialized layouts: a horizontal stack lays out two child
     * layouts, and each child layout in turn manages an anchored image of its own.
     *
     * @param height fixed row height
     * @param image1 first image resource
     * @param image2 second image resource
     * @param verticalMargin vertical edge constraint applied inside each child image box
     * @return horizontal row layout
     */
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

    /**
     * Builds the nested image-box demo content.
     * <p>
     * This example stacks two horizontal rows vertically, which makes it a compact reference for how nested
     * stack layouts compute their size from child content.
     *
     * @param rowHeight height for each horizontal row
     * @param image image resource to use in each image box
     * @return vertically stacked image box layout
     */
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

    /**
     * Creates a standard panel root used by multiple demo states.
     * <p>
     * The returned layout establishes a common panel size, margins, padding, and centered title text so the
     * individual examples can focus on the behavior they are demonstrating.
     *
     * @param xPos panel x position
     * @param yPos panel y position
     * @param title panel title text
     * @return base panel layout with a title already attached
     */
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

    /**
     * Builds the image layout demonstration state.
     * <p>
     * The nested image box layout is manually recomputed before being inserted so the parent can immediately use
     * its computed size when centering it.
     *
     * @param xPos panel x position
     * @param yPos panel y position
     * @return image demo root layout
     */
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

    /**
     * Adds a shared "back" button to a demo layout.
     * <p>
     * All example states use the screen state stack, so going "back" simply pops the current state and restores
     * the previous root widget tree.
     *
     * @param layout layout that should receive the back button
     */
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


    /**
     * Builds the scroll view demonstration state.
     *
     * @param xPos panel x position
     * @param yPos panel y position
     * @return scroll demo root layout
     */
    public MKLayout textListDemo(int xPos, int yPos) {
        MKLayout root = getRootWithTitle(xPos, yPos, "Scrollable List Demo");
        MKScrollView scrollView = new MKScrollView(0, 0, 120, 100);
        root.addWidget(scrollView);
        scrollView.setScrollVelocity(3.0);
        root.addConstraintToWidget(StackConstraint.VERTICAL, scrollView);
        root.addConstraintToWidget(new CenterXConstraint(), scrollView);

        // The scroll view holds a single content widget, so we use a vertical stack as the scrollable content.
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

    /**
     * Registers all demo states and activates the intro state.
     * <p>
     * Each state is supplied lazily so the corresponding widget tree is only built when that demo is entered.
     */
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

    /**
     * Draws the example panel background and then lets {@link MKScreen} render the active widget tree.
     */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
//        RenderSystem.setShaderTexture(0, BG_LOC);
        graphics.blit(BG_LOC, xPos, yPos, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, 512, 512);
        super.render(graphics, mouseX, mouseY, partialTicks);
    }
}
