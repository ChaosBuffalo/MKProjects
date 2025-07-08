package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PartyInviteResponsePacket;
import com.chaosbuffalo.mkcore.network.PlayerLearnAbilityRequestPacket;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class PartyInvitePopup extends MKScreen {
    private final Player invitingPlayer;
    protected final int POPUP_WIDTH = 180;
    protected final int POPUP_HEIGHT = 200;

    public PartyInvitePopup(Player invitingPlayer) {
        super(Component.translatable("mk.core.gui.party_invite.title"));
        this.invitingPlayer = invitingPlayer;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int xPos = width / 2 - POPUP_WIDTH / 2;
        int yPos = height / 2 - POPUP_HEIGHT / 2;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        GuiTextures.CORE_TEXTURES.bind(getMinecraft());
        GuiTextures.CORE_TEXTURES.drawRegionAtPos(graphics, GuiTextures.BACKGROUND_180_200, xPos, yPos);
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClose() {
        PacketHandler.sendMessageToServer(new PartyInviteResponsePacket(invitingPlayer, false));
        super.onClose();
    }

    protected MKLayout getRootLayout(int xPos, int yPos) {
        MKLayout root = new MKLayout(xPos, yPos, POPUP_WIDTH, POPUP_HEIGHT);
        root.setMargins(5, 5, 5, 5);
        root.setPaddingTop(5).setPaddingBot(5);
        MKStackLayoutVertical stackLayout = new MKStackLayoutVertical(xPos, yPos, POPUP_WIDTH);
        stackLayout.setMargins(8, 8, 50, 4);
        stackLayout.setPaddings(2, 2, 2, 2);
        stackLayout.doSetChildWidth(true);
        MKText text = new MKText(font, Component.translatable("mk.core.gui.party_invite.desc", invitingPlayer.getName()));
        text.setMultiline(true);
        stackLayout.addWidget(text);
        MKStackLayoutHorizontal buttonTray = new MKStackLayoutHorizontal(0, 0, 20);
        buttonTray.setMargins(20, 20, 4, 4);
        buttonTray.setPaddings(2, 2, 2, 2);
        MKButton acceptButton = new MKButton(Component.translatable("mk.core.gui.party_invite.accept"), 60, 20);
        MKButton rejectButton = new MKButton(Component.translatable("mk.core.gui.party_invite.reject"), 60, 20);
        acceptButton.setPressedCallback((button, id) -> {
            PacketHandler.sendMessageToServer(new PartyInviteResponsePacket(invitingPlayer, true));
            super.onClose();
            return true;
        });
        rejectButton.setPressedCallback((button, id) -> {
            this.onClose();
            return true;
        });
        buttonTray.addWidget(acceptButton);
        buttonTray.addWidget(rejectButton);
        stackLayout.addWidget(buttonTray);
        root.addWidget(stackLayout);
        return root;
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        int xPos = width / 2 - POPUP_WIDTH / 2;
        int yPos = height / 2 - POPUP_HEIGHT / 2;
        addWidget(getRootLayout(xPos, yPos));
    }
}
