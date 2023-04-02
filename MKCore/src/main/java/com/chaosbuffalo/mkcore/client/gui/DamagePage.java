package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.client.gui.widgets.IconText;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeWidthConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKRectangle;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DamagePage extends StatPageBase {
    protected MKScrollView scrollView;

    public DamagePage(MKPlayerData playerData) {
        super(playerData, new TextComponent("Damage Types"));
    }

    @Override
    public ResourceLocation getPageId() {
        return MKCore.makeRL("damages");
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        addWidget(createScrollingPanelWithContent(this::createDamageTypeList, this::setupDamageHeader, v -> scrollView = v));
    }

    @Override
    protected void persistState(boolean wasResized) {
        super.persistState(wasResized);
        persistScrollView(() -> scrollView, wasResized);
    }

    private MKWidget createDamageTypeList(MKPlayerData pData, int panelWidth) {
        MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(0, 0, panelWidth);
        stackLayout.setMarginTop(4).setMarginBot(4).setPaddingTop(2).setMarginLeft(4)
                .setMarginRight(4).setPaddingBot(2);
        stackLayout.doSetChildWidth(false);
        List<MKDamageType> damageTypes = new ArrayList<>(MKCoreRegistry.DAMAGE_TYPES.getValues());
        damageTypes.sort(Comparator.comparing(d -> d.getDisplayName().getString()));
        for (MKDamageType damageType : damageTypes) {
            if (damageType.shouldDisplay()) {
                IconText iconText = new IconText(0, 0, 16,
                        damageType.getDisplayName(), damageType.getIcon(), font, 16, 2);
                iconText.getText().setColor(0xffffffff);
                stackLayout.addConstraintToWidget(new LayoutRelativeWidthConstraint(1.0f), iconText);
                stackLayout.addWidget(iconText);
                MKRectangle rect = MKRectangle.GetHorizontalBar(1, 0xffffffff);
                stackLayout.addConstraintToWidget(new LayoutRelativeWidthConstraint(.75f), rect);
                stackLayout.addWidget(rect);
                MKText damageText = getTextForAttribute(pData, damageType.getDamageAttribute());
                stackLayout.addConstraintToWidget(new LayoutRelativeWidthConstraint(1.0f), damageText);
                stackLayout.addWidget(damageText);
                MKText resistanceText = getTextForAttribute(pData, damageType.getResistanceAttribute());
                stackLayout.addConstraintToWidget(new LayoutRelativeWidthConstraint(1.0f), resistanceText);
                stackLayout.addWidget(resistanceText);
                MKRectangle rect2 = MKRectangle.GetHorizontalBar(1, 0xffffffff);
                stackLayout.addConstraintToWidget(new LayoutRelativeWidthConstraint(.75f), rect2);
                stackLayout.addWidget(rect2);
            }
        }
        return stackLayout;
    }

    public void setupDamageHeader(MKPlayerData playerData, MKLayout layout) {
        MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(0, 0, layout.getWidth());
        layout.addConstraintToWidget(MarginConstraint.LEFT, stackLayout);
        layout.addConstraintToWidget(MarginConstraint.TOP, stackLayout);
        layout.addConstraintToWidget(new LayoutRelativeWidthConstraint(1.0f), stackLayout);
        stackLayout.setMargins(4, 4, 4, 4);
        stackLayout.setPaddingTop(2);
        stackLayout.setPaddingBot(2);
        layout.addWidget(stackLayout);
        LocalPlayer clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer != null) {
            addStatTextToLayout(stackLayout, Stats.DAMAGE_DEALT, clientPlayer);
            addStatTextToLayout(stackLayout, Stats.DAMAGE_TAKEN, clientPlayer);
            addStatTextToLayout(stackLayout, Stats.DAMAGE_RESISTED, clientPlayer);
        }
    }

    private void addStatTextToLayout(MKLayout layout, ResourceLocation statName,
                                     LocalPlayer clientPlayer) {
        Stat<ResourceLocation> statType = Stats.CUSTOM.get(statName);
        String formattedValue = statType.format(clientPlayer.getStats().getValue(Stats.CUSTOM, statName));
        TranslatableComponent statNameTranslated = new TranslatableComponent("stat." +
                statType.getValue().toString().replace(':', '.'));
        MKText statText = new MKText(font, String.format("%s: %s", statNameTranslated.getString(), formattedValue));
        layout.addWidget(statText);
        addPreDrawRunnable(() -> {
            String val = statType.format(clientPlayer.getStats().getValue(Stats.CUSTOM, statName));
            statText.setText(String.format("%s: %s", statNameTranslated.getString(), val));
        });
    }
}
