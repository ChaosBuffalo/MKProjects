package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingEvaluation;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeWidthConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.stream.Collectors;

public class LearnAbilityTray extends MKStackLayoutVertical {
    private final MKPlayerData playerData;
    private final Font font;
    private final int trainerEntityId;
    private MKAbilityInfo abilityInfo;
    private AbilityTrainingEvaluation evaluation;

    public LearnAbilityTray(int x, int y, int width, MKPlayerData playerData, Font font, int trainerEntityId) {
        super(x, y, width);
        this.playerData = playerData;
        this.trainerEntityId = trainerEntityId;
        this.font = font;
        this.abilityInfo = null;
        setMarginTop(2);
        setMarginBot(2);
        setPaddingTop(2);
        setPaddingBot(2);
        setup();
    }

    public MKAbilityInfo getAbilityInfo() {
        return abilityInfo;
    }

    public AbilityTrainingEvaluation getEvaluation() {
        return evaluation;
    }

    public int getTrainerEntityId() {
        return trainerEntityId;
    }

    public void setup() {
        clearWidgets();
        if (abilityInfo != null) {
            MKStackLayoutHorizontal nameTray = new MKStackLayoutHorizontal(0, 0, 20);
            nameTray.setPaddingRight(4);
            nameTray.setPaddingLeft(4);
            IconText abilityName = new IconText(0, 0, 16, abilityInfo.getAbility().getAbilityName(),
                    abilityInfo.getAbility().getAbilityIcon(), font, 16, 1);
            nameTray.addWidget(abilityName);
            addWidget(nameTray);

            boolean isKnown = playerData.getAbilities().knowsAbility(abilityInfo.getId());
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

            MKScrollView reqScrollView = new MKScrollView(0, 0, getWidth(), 36, true);
            addWidget(reqScrollView);
            manualRecompute();
            MKStackLayoutVertical reqlayout = new MKStackLayoutVertical(0, 0, getWidth());
            reqlayout.setPaddingBot(1);
            reqlayout.setPaddingTop(1);
            reqScrollView.addWidget(reqlayout);
            List<Component> texts = evaluation.getRequirements().stream()
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

    public void setAbility(MKAbilityInfo ability, AbilityTrainingEvaluation requirements) {
        this.abilityInfo = ability;
        this.evaluation = requirements;
        setup();
    }
}
