package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeWidthConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayList;

public class StatsPage extends StatPageBase {

    private static final ArrayList<Attribute> STAT_PANEL_ATTRIBUTES = new ArrayList<>();

    static {
        STAT_PANEL_ATTRIBUTES.add(Attributes.MAX_HEALTH);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.MAX_MANA);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.MANA_REGEN);
        STAT_PANEL_ATTRIBUTES.add(Attributes.ARMOR);
        STAT_PANEL_ATTRIBUTES.add(Attributes.ARMOR_TOUGHNESS);
        STAT_PANEL_ATTRIBUTES.add(Attributes.ATTACK_DAMAGE);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.RANGED_DAMAGE);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.HEAL_BONUS);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.HEAL_EFFICIENCY);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.BUFF_DURATION);
        STAT_PANEL_ATTRIBUTES.add(Attributes.ATTACK_SPEED);
        STAT_PANEL_ATTRIBUTES.add(Attributes.MOVEMENT_SPEED);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.COOLDOWN);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.CASTING_SPEED);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.MELEE_CRIT);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.MELEE_CRIT_MULTIPLIER);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.RANGED_CRIT);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.RANGED_CRIT_MULTIPLIER);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.SPELL_CRIT);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.SPELL_CRIT_MULTIPLIER);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.ABJURATION);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.ALTERATON);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.CONJURATION);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.DIVINATION);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.ENCHANTMENT);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.EVOCATION);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.PHANTASM);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.NECROMANCY);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.RESTORATION);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.ARETE);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.PNEUMA);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.PANKRATION);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.MARKSMANSHIP);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.ONE_HAND_SLASH);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.ONE_HAND_BLUNT);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.ONE_HAND_PIERCE);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.TWO_HAND_SLASH);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.TWO_HAND_BLUNT);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.TWO_HAND_PIERCE);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.BLOCK);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.BLOCK_EFFICIENCY);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.MAX_POISE);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.POISE_REGEN);
        STAT_PANEL_ATTRIBUTES.add(MKAttributes.POISE_BREAK_CD);
    }

    protected MKScrollView scrollView;

    public StatsPage(MKPlayerData playerData) {
        super(playerData, new TextComponent("Stats"));
    }

    @Override
    public ResourceLocation getPageId() {
        return MKCore.makeRL("stats");
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        addWidget(createScrollingPanelWithContent(this::createDamageTypeList, this::setupStatsHeader, v -> scrollView = v));
    }

    @Override
    protected void persistState(boolean wasResized) {
        super.persistState(wasResized);
        persistScrollView(() -> scrollView, wasResized);
    }

    @Override
    protected void init() {
        super.init();
        if (minecraft != null && minecraft.getConnection() != null) {
            minecraft.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
        }
    }

    private MKWidget createDamageTypeList(MKPlayerData pData, int panelWidth) {
        MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(0, 0, panelWidth);
        stackLayout.setMargins(4, 4, 4, 4);
        stackLayout.setPaddingTop(2).setPaddingBot(2);
        stackLayout.doSetChildWidth(true);
        for (Attribute attr : STAT_PANEL_ATTRIBUTES) {
            MKText textWidget = getTextForAttribute(pData, attr);
            stackLayout.addWidget(textWidget);
        }
        return stackLayout;
    }

    public void setupStatsHeader(MKPlayerData playerData, MKLayout layout) {
        MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(0, 0, layout.getWidth());
        layout.addConstraintToWidget(MarginConstraint.LEFT, stackLayout);
        layout.addConstraintToWidget(MarginConstraint.TOP, stackLayout);
        layout.addConstraintToWidget(new LayoutRelativeWidthConstraint(1.0f), stackLayout);
        stackLayout.setMargins(4, 4, 4, 4);
        stackLayout.setPaddingTop(2);
        stackLayout.setPaddingBot(2);
        layout.addWidget(stackLayout);
        String personaNameText = I18n.get("mkcore.gui.character.persona_name",
                playerData.getPersonaManager().getActivePersona().getName());
        MKText personaName = new MKText(font, personaNameText);
        stackLayout.addWidget(personaName);
        String healthText = I18n.get("mkcore.gui.character.current_health",
                String.format("%.0f", playerData.getStats().getHealth()),
                String.format("%.0f", playerData.getStats().getMaxHealth()));
        MKText health = new MKText(font, healthText);
        String manaText = I18n.get("mkcore.gui.character.current_mana",
                String.format("%.0f", playerData.getStats().getMana()),
                String.format("%.0f", playerData.getStats().getMaxMana()));
        MKText mana = new MKText(font, manaText);
        addPreDrawRunnable(() -> {
            mana.setText(I18n.get("mkcore.gui.character.current_mana",
                    String.format("%.0f", playerData.getStats().getMana()),
                    String.format("%.0f", playerData.getStats().getMaxMana())));
        });
        addPreDrawRunnable(() -> {
            health.setText(I18n.get("mkcore.gui.character.current_health",
                    String.format("%.0f", playerData.getStats().getHealth()),
                    String.format("%.0f", playerData.getStats().getMaxHealth())));
        });
        stackLayout.addWidget(health);
        stackLayout.addWidget(mana);
    }
}
