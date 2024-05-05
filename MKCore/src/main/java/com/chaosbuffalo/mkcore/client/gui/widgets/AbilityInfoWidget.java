package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.client.gui.IAbilityScreen;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.utils.text.IconTextComponent;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

public class AbilityInfoWidget extends MKStackLayoutVertical {

    private final MKPlayerData playerData;
    private final Font font;
    private final IAbilityScreen screen;

    public AbilityInfoWidget(int x, int y, int width, MKPlayerData playerData, Font font, IAbilityScreen screen) {
        super(x, y, width);
        this.screen = screen;
        this.playerData = playerData;
        this.font = font;
        setMargins(6, 6, 6, 6);
        setPaddings(0, 0, 2, 2);
        doSetChildWidth(true);
        setup();
    }

    private void addDescriptionLine(Component component) {
        if (component instanceof IconTextComponent iconText) {
            IconText icon = new IconText(0, 0, 16, component, iconText.getIcon(), font, 16, 1);
            icon.getText().setColor(0xaaffffff);
            addWidget(icon);
        } else {
            MKText text = new MKText(font, component);
            text.setColor(0xaaffffff);
            text.setMultiline(true);
            addWidget(text);
        }
    }

    public void setup() {
        MKAbilityInfo selected = screen.getSelectedAbility();
        if (selected == null) {
            MKText noSelectPrompt = new MKText(font, Component.translatable("mkcore.gui.select_ability"));
            noSelectPrompt.setColor(0xffffffff);
            addWidget(noSelectPrompt);
        } else {
            IconText abilityIcon = new AbilityIconText(0, 0, 16, font, 16, screen, selected);
            addWidget(abilityIcon);
            selected.getAbility().buildDescription(playerData, AbilityContext.forCaster(playerData, selected) , this::addDescriptionLine);
        }
    }

    public void refresh() {
        clearWidgets();
        setup();
    }
}
