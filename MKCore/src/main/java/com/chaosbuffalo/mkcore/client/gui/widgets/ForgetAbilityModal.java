package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.GuiTextures;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.network.ForgetAbilitiesRequestPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerLearnAbilityRequestPacket;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.*;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ForgetAbilityModal extends MKModal {

    private final List<MKAbility> forgetting = new ArrayList<>();
    private final int numberToForget;
    private final MKButton forgetButton;
    private final MKAbility tryingToLearn;
    private final int trainerEntityId;
    private final boolean isLearning;

    public ForgetAbilityModal(MKAbility tryingToLearn, MKPlayerData playerData, int xPos, int yPos, int width, int height, Font font, int trainerEntityId) {
        MKImage background = GuiTextures.CORE_TEXTURES.getImageForRegion(
                GuiTextures.BACKGROUND_180_200, xPos, yPos, width, height);
        addWidget(background);
        isLearning = tryingToLearn != null && trainerEntityId != -1;
        this.tryingToLearn = tryingToLearn;
        this.trainerEntityId = trainerEntityId;
        int count = playerData.getAbilities().getSlotDeficitToLearnAnAbility();
        numberToForget = count;

        Component promptText;
        if (isLearning) {
            promptText = new TranslatableComponent("mkcore.gui.character.forget_ability", count, tryingToLearn.getAbilityName());
        } else {
            promptText = new TranslatableComponent("mkcore.gui.character.forget");
        }
        MKText prompt = new MKText(font, promptText, xPos + 6, yPos + 6);
        prompt.setWidth(width - 10);
        prompt.setMultiline(true);
        addWidget(prompt);
        addWidget(new MKRectangle(xPos + 10, yPos + 34, width - 20, height - 65, 0x44000000));
        MKScrollView scrollview = new MKScrollView(xPos + 15, yPos + 39, width - 30,
                height - 75, true);

        addWidget(scrollview);
        TranslatableComponent text = new TranslatableComponent("mkcore.gui.character.forget_confirm");
        forgetButton = new MKButton(scrollview.getX(), scrollview.getY() + scrollview.getHeight() + 10, text);
        forgetButton.setWidth(font.width(text) + 20);
        forgetButton.setX(scrollview.getX() + (scrollview.getWidth() - forgetButton.getWidth()) / 2);
        forgetButton.setEnabled(ready());
        forgetButton.setPressedCallback(this::forgetCallback);
        addWidget(forgetButton);
        MKStackLayoutVertical abilities = new MKStackLayoutVertical(0, 0, scrollview.getWidth());
        abilities.setPaddingBot(2);
        abilities.setPaddingTop(2);
        abilities.setMargins(2, 2, 0, 0);
        abilities.doSetChildWidth(true);
        playerData.getAbilities().getPoolAbilities().forEach(abilityId -> {
            if (abilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
                return;
            }
            MKAbility ability = MKCoreRegistry.getAbility(abilityId);
            if (ability != null) {
                AbilityForgetOption abilityIcon = new AbilityForgetOption(ability, this, font);
                abilities.addWidget(abilityIcon);
            }
        });
        scrollview.addWidget(abilities);
        abilities.manualRecompute();
        scrollview.resetView();
    }

    private boolean forgetCallback(MKButton button, int click) {
        if (isLearning) {
            PacketHandler.sendMessageToServer(new PlayerLearnAbilityRequestPacket(
                    forgetting.stream().map(MKAbility::getAbilityId).collect(Collectors.toList()),
                    tryingToLearn.getAbilityId(), trainerEntityId));
        } else {
            PacketHandler.sendMessageToServer(new ForgetAbilitiesRequestPacket(forgetting.stream().map(MKAbility::getAbilityId).collect(Collectors.toList())));
        }

        if (getScreen() != null) {
            getScreen().closeModal(this);
        }
        return true;
    }

    private void checkStatus() {
        forgetButton.setEnabled(ready());
    }

    public void forget(MKAbility ability) {
        forgetting.add(ability);
        checkStatus();
    }

    public void cancelForget(MKAbility ability) {
        forgetting.remove(ability);
        checkStatus();
    }

    public boolean isForgetting(MKAbility ability) {
        return forgetting.contains(ability);
    }

    public boolean ready() {
        return isLearning ? forgetting.size() == numberToForget : forgetting.size() > 0;
    }

}
