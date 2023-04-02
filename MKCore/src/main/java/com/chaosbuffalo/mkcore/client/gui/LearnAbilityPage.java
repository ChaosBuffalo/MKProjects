package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.training.AbilityTrainingEvaluation;
import com.chaosbuffalo.mkcore.client.gui.widgets.IconText;
import com.chaosbuffalo.mkcore.client.gui.widgets.LearnAbilityTray;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerLearnAbilityRequestPacket;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.OffsetConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.instructions.HoveringTextInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import com.chaosbuffalo.mkwidgets.utils.TextureRegion;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LearnAbilityPage extends AbilityPageBase {
    private final List<AbilityTrainingEvaluation> offeredAbilities;
    private final int entityId;
    private LearnAbilityTray requirementsTray;
    private MKLayout footer;
    private MKLayout root;

    public LearnAbilityPage(MKPlayerData playerData, List<AbilityTrainingEvaluation> offeredAbilities, int entityId) {
        super(playerData, new TextComponent("Learn Abilities"));
        this.offeredAbilities = ImmutableList.copyOf(offeredAbilities);
        this.entityId = entityId;
    }

    @Override
    public ResourceLocation getPageId() {
        return MKCore.makeRL("learn_ability");
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        addWidget(createAbilitiesPage());
    }

    @Override
    protected Collection<MKAbility> getSortedAbilityList() {
        return offeredAbilities.stream().map(AbilityTrainingEvaluation::getAbility).collect(Collectors.toList());
    }

    private Optional<AbilityTrainingEvaluation> findEvaluation(MKAbility ability) {
        return offeredAbilities.stream().filter(evaluation -> evaluation.getAbility() == ability).findFirst();
    }

    @Override
    protected void restoreSelectedAbility(MKAbility ability) {
        super.restoreSelectedAbility(ability);
        if (requirementsTray != null) {
            findEvaluation(ability).ifPresent(eval -> {
                requirementsTray.setAbility(ability, eval);
                resetFooter();
            });
        }
    }

    private MKLayout createPoolManagementFooter() {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(getDataBoxTexture(), GuiTextures.BACKGROUND_320_240);
        int yStart = yPos + DATA_BOX_OFFSET + 136;
        MKStackLayoutHorizontal layout = new MKStackLayoutHorizontal(xPos + xOffset, yStart, 20);
        layout.setPaddingLeft(16);
        layout.setPaddingRight(16);
        layout.setMarginLeft(24);
        MKButton manage = createManageButton();

        MKButton learnButton = createLearnButton();
        layout.addWidget(learnButton);

        IconText poolText = createPoolUsageText();
        layout.addWidget(poolText, new OffsetConstraint(0, 2, false, true));
        layout.addWidget(manage);
        this.footer = layout;
        return layout;

    }

    @Nonnull
    private MKButton createLearnButton() {
        String learnButtonText = I18n.get("mkcore.gui.character.learn");
        MKButton learnButton = new MKButton(0, 0, learnButtonText) {

            @Override
            public boolean checkHovered(int mouseX, int mouseY) {
                return this.isVisible() && this.isInBounds(mouseX, mouseY);
            }

            @Override
            public void onMouseHover(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
                super.onMouseHover(mc, mouseX, mouseY, partialTicks);
                if (requirementsTray != null && requirementsTray.getEvaluation() != null && requirementsTray.getEvaluation().getRequirements().size() > 0) {
                    if (getScreen() != null) {
                        getScreen().addPostRenderInstruction(new HoveringTextInstruction(
                                I18n.get("mkcore.gui.character.unmet_req_tooltip"),
                                getParentCoords(new Vec2i(mouseX, mouseY))));
                    }
                }
            }
        };
        learnButton.setWidth(60);
        learnButton.setEnabled(canLearnCurrentAbility());
        learnButton.setPressedCallback((button, buttonType) -> {
            if (requirementsTray.getEvaluation().usesAbilityPool() && playerData.getAbilities().isAbilityPoolFull()) {
                addModal(getChoosePoolSlotWidget(requirementsTray.getAbility(), requirementsTray.getTrainerEntityId()));
            } else {
                PacketHandler.sendMessageToServer(new PlayerLearnAbilityRequestPacket(
                        requirementsTray.getAbility().getAbilityId(), requirementsTray.getTrainerEntityId()));
            }
            return true;
        });
        return learnButton;
    }

    private boolean canLearnCurrentAbility() {
        if (requirementsTray.getAbility() != null && requirementsTray.getEvaluation() != null) {
            boolean isKnown = playerData.getAbilities().knowsAbility(requirementsTray.getAbility().getAbilityId());
            boolean canLearn = requirementsTray.getEvaluation().canLearn();
            return !isKnown && canLearn;
        } else {
            return false;
        }
    }

    private MKWidget createAbilitiesPage() {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        TextureRegion dataBoxRegion = GuiTextures.CORE_TEXTURES.getRegion(GuiTextures.DATA_BOX_SHORT);
        if (minecraft == null || minecraft.player == null || dataBoxRegion == null) {
            return new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        }
        int xOffset = GuiTextures.CORE_TEXTURES.getCenterXOffset(GuiTextures.DATA_BOX, GuiTextures.BACKGROUND_320_240);
        MKLayout root = getRootLayout(xPos, yPos, xOffset, dataBoxRegion.width, false);
        int contentX = xPos + xOffset;
        int contentY = yPos + DATA_BOX_OFFSET;
        int contentWidth = dataBoxRegion.width;
        int contentHeight = dataBoxRegion.height;

        requirementsTray = new LearnAbilityTray(contentX, yPos + 3, contentWidth, playerData, font, entityId);
        root.addWidget(requirementsTray);

        abilitiesScrollPanel = getAbilityScrollPanel(contentX, contentY, contentWidth, contentHeight);
        root.addWidget(abilitiesScrollPanel);

        MKLayout footer = createPoolManagementFooter();
        root.addWidget(footer);

        this.root = root;
        return root;
    }

    public void resetFooter() {
        if (minecraft != null && minecraft.player != null && root != null && footer != null) {
            this.root.removeWidget(footer);
            root.addWidget(createPoolManagementFooter());
        }
    }
}
