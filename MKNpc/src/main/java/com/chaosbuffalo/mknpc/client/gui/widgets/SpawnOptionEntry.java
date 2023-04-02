package com.chaosbuffalo.mknpc.client.gui.widgets;

import com.chaosbuffalo.mkcore.client.gui.GuiTextures;
import com.chaosbuffalo.mknpc.spawn.SpawnOption;
import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKModal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

import java.util.function.Consumer;

public class SpawnOptionEntry extends CenteringHorizontalLayout {
    private final MKButton button;
    private final SpawnOption option;
    private final MKText text;
    protected final int POPUP_WIDTH = 180;
    protected final int POPUP_HEIGHT = 201;


    public SpawnOptionEntry(int x, int y, int height, SpawnOption option,
                            Font fontRenderer, Consumer<SpawnOption> removeCallback) {
        super(x, y, height, fontRenderer);
        this.option = option;
        String name = option.getDefinitionClient().getName() != null ? option.getDefinitionClient().getName() :
                option.getDefinitionClient().getDefinitionName().toString();
        button = new MKButton(Math.max(fontRenderer.width(name), 100), name);
        button.setPressedCallback(this::handleOpenNpcDefinitionList);
        addWidget(button);
        String weightText = String.format("Weight: %.2f", option.getWeight());
        text = new MKText(fontRenderer, weightText);
        text.setWidth(fontRenderer.width(weightText));
        setPaddingLeft(2);
        setPaddingRight(2);
        addWidget(text);
        HoverTextButton plusButton = new HoverTextButton(fontRenderer, "+", () -> {
            if (Screen.hasShiftDown()){
                option.setWeight(option.getWeight() + 10.0);
                updateText();
            } else {
                option.setWeight(option.getWeight() + 1.0);
                updateText();
            }
        });
        addWidget(plusButton);
        HoverTextButton minusButton = new HoverTextButton(fontRenderer, "-", () -> {
            if (Screen.hasShiftDown()){
                option.setWeight(Math.max(option.getWeight() - 10.0, 1.0));
                updateText();
            } else {
                option.setWeight(Math.max(option.getWeight() - 1.0, 1.0));
                updateText();
            }
        });
        addWidget(minusButton);
        HoverTextButton delButton = new HoverTextButton(fontRenderer, "X", () -> {
            removeCallback.accept(option);
        });
        delButton.setColor(0xffff0000);
        addWidget(delButton);
    }

    public void updateText(){
        String weightText = String.format("Weight: %.2f", option.getWeight());
        text.setText(weightText);
    }

    public boolean handleOpenNpcDefinitionList(MKButton button, int mouseButton){
        IMKScreen screen = getScreen();
        if (screen != null){
            MKModal popup = new MKModal();
            int screenWidth = screen.getWidth();
            int screenHeight = screen.getHeight();
            int xPos = (screenWidth - POPUP_WIDTH) / 2;
            int yPos = (screenHeight - POPUP_HEIGHT) / 2;
            MKImage background = GuiTextures.CORE_TEXTURES.getImageForRegion(
                    GuiTextures.BACKGROUND_180_200, xPos, yPos, POPUP_WIDTH, POPUP_HEIGHT);
            popup.addWidget(background);
            NpcDefinitionList definitions = new NpcDefinitionList(xPos, yPos, POPUP_WIDTH, POPUP_HEIGHT,
                    fontRenderer, (client) -> {
                        option.setDefinition(client.getDefinitionName());
                        String name = option.getDefinitionClient().getName() != null ? option.getDefinitionClient().getName() :
                            option.getDefinitionClient().toString();
                        updateButtonText(name);
                        screen.closeModal(popup);

            });
            popup.addWidget(definitions);
            screen.addModal(popup);
        }
        return true;
    }

    public void updateButtonText(String newText){
        button.setWidth(Math.max(fontRenderer.width(newText), 100));
        button.buttonText = new TextComponent(newText);
    }

    public MKButton getButton() {
        return button;
    }

}
