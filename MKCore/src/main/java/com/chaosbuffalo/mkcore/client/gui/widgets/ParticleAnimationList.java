package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKTextFieldWidget;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

public class ParticleAnimationList extends MKWidget {
    private final Font font;
    private final BiConsumer<ResourceLocation, ParticleAnimation> selectCallback;
    private final MKScrollView scrollView;
    private final MKStackLayoutVertical stackLayout;
    private String searchString = "";
    private MKTextFieldWidget searchField;

    public ParticleAnimationList(int x, int y, int width, int height, Font font,
                                 BiConsumer<ResourceLocation, ParticleAnimation> selectCallback) {
        super(x, y, width, height);
        this.font = font;
        this.selectCallback = selectCallback;
        scrollView = new MKScrollView(x, y, width, height, true);
        stackLayout = new MKStackLayoutVertical(0, 0, width);
        stackLayout.setMargins(5, 5, 5, 5);
        stackLayout.setPaddingBot(1);
        addWidget(scrollView);
        scrollView.addWidget(stackLayout);
        populate();
    }

    public void populate() {
        stackLayout.clearWidgets();
        populateList();
        stackLayout.manualRecompute();
        scrollView.setToRight();
        scrollView.setToTop();
    }

    public MKTextFieldWidget getSearchField() {
        return searchField;
    }

    private void populateList() {
        searchField = new MKTextFieldWidget(font, getX(), getY(), getWidth() - 20, font.lineHeight + 2,
                Component.translatable("mkcore.ui.search"));
        searchField.setText(searchString);
        searchField.setTextChangeCallback((widget, content) -> {
            searchString = content;
            populate();
        });
        stackLayout.addWidget(searchField);

        String normalizedSearch = searchString.toLowerCase(Locale.ROOT);
        List<Map.Entry<ResourceLocation, ParticleAnimation>> animations = new ArrayList<>(ParticleAnimationManager.ANIMATIONS.entrySet());
        animations.sort(Comparator.comparing(entry -> entry.getKey().toString().toLowerCase(Locale.ROOT)));

        for (Map.Entry<ResourceLocation, ParticleAnimation> anim : animations) {
            String animationName = anim.getKey().toString();
            String normalizedName = animationName.toLowerCase(Locale.ROOT);
            if (!normalizedSearch.isEmpty() && !normalizedName.contains(normalizedSearch)) {
                continue;
            }

            MKButton button = new MKButton(0, 0, animationName);
            button.setWidth(getWidth() - stackLayout.getMarginLeft() - stackLayout.getMarginRight());
            button.setPressedCallback((btn, click) -> {
                selectCallback.accept(anim.getKey(), anim.getValue());
                return true;
            });
            stackLayout.addWidget(button);
        }
    }
}
