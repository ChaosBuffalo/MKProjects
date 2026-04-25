package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingEvaluation;
import com.chaosbuffalo.mkcore.client.gui.AbilityUiEntry;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.player.PlayerAbilityLoadout;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeWidthConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

public class LearnAbilityTray extends MKStackLayoutVertical {
    private AbilityUiEntry ability;
    private AbilityTrainingEvaluation evaluation;
    private final MKPlayerData playerData;
    private final Font font;
    private final int trainerEntityId;

    public LearnAbilityTray(int x, int y, int width, MKPlayerData playerData, Font font, int trainerEntityId) {
        super(x, y, width);
        this.playerData = playerData;
        this.trainerEntityId = trainerEntityId;
        this.font = font;
        this.ability = null;
        setMarginTop(2);
        setMarginBot(2);
        setPaddingTop(2);
        setPaddingBot(2);
        setup();
    }

    public AbilityTrainingEvaluation getEvaluation() {
        return evaluation;
    }

    public int getTrainerEntityId() {
        return trainerEntityId;
    }

    public void setup() {
        clearWidgets();
        if (getAbility() != null) {
            MKStackLayoutHorizontal nameTray = new MKStackLayoutHorizontal(0, 0, 20);
            nameTray.setPaddingRight(4);
            nameTray.setPaddingLeft(4);
            IconText abilityName = new IconText(0, 0, 16, getAbility().getDisplayName(),
                    getAbility().getIconOrFallback(), font, 16, 1);
            nameTray.addWidget(abilityName);
            addWidget(nameTray);

            boolean isKnown = playerData.getAbilities().knowsAbility(getAbility().getAbilityId());
            boolean canLearn = evaluation.canLearn();
            String knowText;
            if (isKnown) {
                knowText = I18n.get("mkcore.gui.character.already_known");
            } else if (!canLearn) {
                knowText = I18n.get("mkcore.gui.character.unmet_req");
            } else {
                knowText = I18n.get("mkcore.gui.character.can_learn");
            }
            MKText doesKnowWid = new MKText(font, knowText);
            doesKnowWid.setWidth(font.width(knowText));
            addWidget(doesKnowWid);
            addStatusDetail(isKnown, canLearn);

            MKScrollView reqScrollView = new MKScrollView(0, 0, getWidth(), 36, true);
            addWidget(reqScrollView);
            manualRecompute();
            MKStackLayoutVertical reqlayout = new MKStackLayoutVertical(0, 0, getWidth());
            reqlayout.setPaddingBot(1);
            reqlayout.setPaddingTop(1);
            reqScrollView.addWidget(reqlayout);
            List<Component> texts = evaluation.requirements().stream()
                    .map(req -> Component.literal("  - ")
                            .append(req.description())
                            .withStyle(req.isMet() ? ChatFormatting.DARK_GREEN : ChatFormatting.BLACK))
                    .collect(Collectors.toList());
            for (Component text : texts) {
                MKText reqText = new MKText(font, text);
                reqText.setMultiline(true);
                reqlayout.addConstraintToWidget(new LayoutRelativeWidthConstraint(1.0f), reqText);
                reqlayout.addWidget(reqText);
            }
            reqlayout.manualRecompute();
        } else {
            MKText prompt = new MKText(font, I18n.get("mkcore.gui.character.learn_ability_prompt"));
            addWidget(prompt);
        }
    }

    public void setAbility(AbilityUiEntry ability, AbilityTrainingEvaluation requirements) {
        this.ability = ability;
        this.evaluation = requirements;
        setup();
    }

    public AbilityUiEntry getAbility() {
        return ability;
    }

    private void addStatusDetail(boolean isKnown, boolean canLearn) {
        ResourceLocation abilityId = getAbility().getAbilityId();
        PlayerAbilityLoadout loadout = playerData.getLoadout();
        PlayerAbilityLoadout.SlottedAbilityLocation equippedLocation = loadout.findEquippedAbilityLocation(abilityId);
        if (equippedLocation != null) {
            addDetailText(Component.translatable(
                    "mkcore.gui.character.learn_slot.current",
                    equippedLocation.groupId().getDisplayName(),
                    equippedLocation.slotIndex() + 1
            ));
            return;
        }

        if (!canLearn) {
            return;
        }

        if (evaluation.usesAbilityPool() && playerData.getAbilities().isAbilityPoolFull()) {
            addDetailText(Component.translatable(
                    "mkcore.gui.character.learn_slot.pool_full",
                    playerData.getAbilities().getSlotDeficitToLearnAnAbility()
            ));
            return;
        }

        AbilityGroupId targetGroup = loadout.resolveLearnedAbilityGroup(abilityId);
        if (targetGroup == null) {
            return;
        }

        PlayerAbilityLoadout.SlottedAbilityLocation previewLocation = loadout.previewAutoEquipLocation(abilityId);
        if (!isKnown && previewLocation != null) {
            addDetailText(Component.translatable(
                    "mkcore.gui.character.learn_slot.auto",
                    previewLocation.groupId().getDisplayName(),
                    previewLocation.slotIndex() + 1
            ));
            return;
        }

        addDetailText(Component.translatable(
                "mkcore.gui.character.learn_slot.manual",
                targetGroup.getDisplayName()
        ));
    }

    private void addDetailText(Component text) {
        MKText detailText = new MKText(font, text);
        detailText.setColor(0xff555555);
        addWidget(detailText);
    }

}
