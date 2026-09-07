package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.pickers.MKCreativeBlockPickerSource;
import com.chaosbuffalo.mkwidgets.client.gui.pickers.MKCreativePickerCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MKCreativeBlockPickerPanel extends MKLayout {
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BOTTOM_PADDING = 8;

    private final MKCreativeBlockPickerSource source;
    private final Component title;
    private final ResourceLocation currentValue;
    private final Consumer<ResourceLocation> selectionCallback;
    private final Runnable cancelCallback;
    private final boolean allowClear;
    private final Map<String, MKButton> categoryButtons = new LinkedHashMap<>();
    private String selectedCategoryId;
    private String query = "";
    private MKCreativeGridPicker grid;

    public MKCreativeBlockPickerPanel(int x, int y, int width, int height, Component title,
                                      ResourceLocation currentValue,
                                      Consumer<ResourceLocation> selectionCallback,
                                      Runnable cancelCallback,
                                      boolean allowClear) {
        super(x, y, width, height);
        this.source = new MKCreativeBlockPickerSource();
        this.title = title;
        this.currentValue = currentValue;
        this.selectionCallback = selectionCallback;
        this.cancelCallback = cancelCallback;
        this.allowClear = allowClear;
        setMargins(8, 8, 8, 8);
        setPaddingTop(8).setPaddingBot(8);
        buildContent(Minecraft.getInstance());
    }

    private void buildContent(Minecraft minecraft) {
        int contentWidth = getWidth() - 42;
        int xPos = getX();
        int yPos = getY();

        MKText titleText = makeText(title);
        addWidget(titleText);
        addConstraintToWidget(MarginConstraint.TOP, titleText);
        addConstraintToWidget(new CenterXConstraint(), titleText);

        List<MKCreativePickerCategory> categories = source.categories(minecraft);
        MKCreativePickerCategory selectedCategory = selectedCategory(categories);

        MKTextFieldWidget searchField = new MKTextFieldWidget(minecraft.font, xPos, yPos + 34,
                contentWidth, 18, Component.literal("Search"));
        searchField.setText(query);
        searchField.setTextChangeCallback((field, value) -> {
            query = value;
            refreshGrid(minecraft);
        });
        addWidget(searchField);
        addConstraintToWidget(new CenterXConstraint(), searchField);

        int footerY = yPos + getHeight() - BOTTOM_PADDING - BUTTON_HEIGHT;
        int pickerTop = yPos + 64;
        int pickerAreaHeight = footerY - pickerTop - 8;
        int categoryWidth = 116;
        int gridX = xPos + 18 + categoryWidth + 8;
        int gridWidth = getWidth() - categoryWidth - 44;

        grid = new MKCreativeGridPicker(gridX, pickerTop, gridWidth, pickerAreaHeight);
        grid.setSelectedId(currentValue);
        if (selectedCategory != null) {
            grid.setEntries(source.entries(minecraft, selectedCategory, query));
        }
        grid.setSelectionCallback(entry -> selectionCallback.accept(entry.id()));
        addWidget(grid);

        MKScrollView categoryScroll = new MKScrollView(xPos + 12, pickerTop, categoryWidth, pickerAreaHeight);
        categoryScroll.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        addWidget(categoryScroll);

        MKStackLayoutVertical categoryContent = new MKStackLayoutVertical(0, 0, categoryWidth - 4);
        categoryContent.setPaddingTop(0).setPaddingBot(0);
        categoryScroll.addWidget(categoryContent);
        for (MKCreativePickerCategory category : categories) {
            MKButton categoryButton = new MKButton(category.displayName(), categoryWidth - 8, BUTTON_HEIGHT);
            categoryButton.setTooltip(category.displayName());
            categoryButton.setPressedCallback((button, mouseButton) -> {
                selectedCategoryId = category.id();
                refreshCategoryButtons();
                refreshGrid(minecraft);
                return true;
            });
            categoryButtons.put(category.id(), categoryButton);
            categoryContent.addWidget(categoryButton);
        }
        refreshCategoryButtons();

        int cancelX = allowClear ? xPos + (getWidth() / 2) - 104 : xPos + (getWidth() / 2) - 50;
        MKButton cancel = new MKButton(Component.literal("Cancel"), 100, BUTTON_HEIGHT);
        cancel.setX(cancelX);
        cancel.setY(footerY);
        cancel.setPressedCallback((button, mouseButton) -> {
            cancelCallback.run();
            return true;
        });
        addWidget(cancel);

        if (allowClear) {
            MKButton clear = new MKButton(Component.literal("Clear"), 100, BUTTON_HEIGHT);
            clear.setX(xPos + (getWidth() / 2) + 4);
            clear.setY(footerY);
            clear.setPressedCallback((button, mouseButton) -> {
                selectionCallback.accept(ResourceLocation.withDefaultNamespace("air"));
                return true;
            });
            addWidget(clear);
        }
    }

    private MKText makeText(Component text) {
        return new MKText(Minecraft.getInstance().font, text).setColor(TEXT_COLOR);
    }

    private void refreshGrid(Minecraft minecraft) {
        if (grid == null) {
            return;
        }
        grid.resetScroll();
        MKCreativePickerCategory category = selectedCategory(source.categories(minecraft));
        if (category != null) {
            grid.setEntries(source.entries(minecraft, category, query));
        }
    }

    private void refreshCategoryButtons() {
        categoryButtons.forEach((id, button) -> button.setEnabled(!id.equals(selectedCategoryId)));
    }

    private MKCreativePickerCategory selectedCategory(List<MKCreativePickerCategory> categories) {
        if (categories.isEmpty()) {
            return null;
        }
        if (selectedCategoryId != null) {
            for (MKCreativePickerCategory category : categories) {
                if (category.id().equals(selectedCategoryId)) {
                    return category;
                }
            }
        }
        MKCreativePickerCategory selected = categories.getFirst();
        selectedCategoryId = selected.id();
        return selected;
    }

    @Override
    public void draw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY,
                     float partialTicks) {
        graphics.fill(x, y, x + width, y + height, 0xE0202020);
        graphics.fill(x, y, x + width, y + 22, 0xE0303030);
    }
}
