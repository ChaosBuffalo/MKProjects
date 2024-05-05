package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.client.gui.GuiTextures;
import com.chaosbuffalo.mkcore.client.gui.IAbilityScreen;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerSlotAbilityPacket;
import com.chaosbuffalo.mkwidgets.client.gui.UIConstants;
import com.chaosbuffalo.mkwidgets.client.gui.actions.IDragState;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.FillConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.instructions.HoveringTextInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.math.IntColor;
import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class AbilitySlotWidget extends MKLayout {
    private final AbilityGroupId slotGroup;
    private boolean unlocked;
    private final int slotIndex;
    private final IAbilityScreen screen;
    private MKAbilityInfo abilityInfo;
    private MKImage background;
    private MKImage icon;
    private final MKPlayerData playerData;

    public AbilitySlotWidget(int x, int y, AbilityGroupId group, int slotIndex, MKPlayerData playerData, IAbilityScreen screen) {
        super(x, y, 20, 20);
        this.slotGroup = group;
        this.screen = screen;
        this.slotIndex = slotIndex;
        this.setMargins(2, 2, 2, 2);
        this.icon = null;
        this.playerData = playerData;
        refreshSlot();
    }

    public void setIconColor(int color) {
        if (icon != null) {
            icon.setColor(new IntColor(color));
        }
    }

    public void setBackgroundColor(int color) {
        background.setColor(new IntColor(color));
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public AbilityGroupId getSlotGroup() {
        return slotGroup;
    }

    private void refreshSlot() {
        abilityInfo = playerData.getLoadout().getAbilityGroup(slotGroup).getAbilityInfo(slotIndex);
        setupBackground();
        setupIcon();
    }

    private void setupBackground() {
        if (background != null) {
            removeWidget(background);
        }
        AbilityGroup abilityGroup = playerData.getLoadout().getAbilityGroup(slotGroup);
        unlocked = abilityGroup.isSlotUnlocked(slotIndex);
        background = getAbilityGroupSlotImage(slotGroup, unlocked);
        addWidget(background);
        addConstraintToWidget(new FillConstraint(), background);
    }

    private void setupIcon() {
        if (icon != null) {
            removeWidget(icon);
        }

        if (abilityInfo != null) {
            icon = new MKImage(0, 0, 16, 16, abilityInfo.getAbilityIcon());
            addWidget(icon);
            addConstraintToWidget(MarginConstraint.TOP, icon);
            addConstraintToWidget(MarginConstraint.LEFT, icon);
        }
    }

    private MKImage getAbilityGroupSlotImage(AbilityGroupId group, boolean unlocked) {
        String texture;
        switch (group) {
            case Ultimate:
                texture = unlocked ? GuiTextures.ABILITY_SLOT_ULT : GuiTextures.ABILITY_SLOT_ULT_LOCKED;
                break;
            case Passive:
                texture = unlocked ? GuiTextures.ABILITY_SLOT_PASSIVE : GuiTextures.ABILITY_SLOT_PASSIVE_LOCKED;
                break;
            default:
                texture = unlocked ? GuiTextures.ABILITY_SLOT_REG : GuiTextures.ABILITY_SLOT_REG_LOCKED;
                break;
        }
        return GuiTextures.CORE_TEXTURES.getImageForRegion(texture, getX(), getY(), getWidth(), getHeight());
    }

    private void setSlotToAbility(ResourceLocation ability) {
        PacketHandler.sendMessageToServer(new PlayerSlotAbilityPacket(slotGroup, slotIndex, ability));
    }

    @Override
    public void onDragEnd(IDragState state) {
        icon.setColor(new IntColor(0xffffffff));
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == UIConstants.MOUSE_BUTTON_LEFT) {
            if (abilityInfo != null) {
                screen.startDraggingAbility(abilityInfo, icon, this);
                icon.setColor(new IntColor(0xff555555));
                return true;
            }
        } else if (mouseButton == UIConstants.MOUSE_BUTTON_RIGHT) {
            setSlotToAbility(MKCoreRegistry.INVALID_ABILITY);
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(double mouseX, double mouseY, int mouseButton) {
        if (screen.isDraggingAbility()) {
            MKAbilityInfo dragging = screen.getDraggingAbility();
            if (unlocked && slotGroup.fitsAbilityType(dragging.getAbilityType())) {
                setSlotToAbility(dragging.getId());
            }
            screen.stopDraggingAbility();
            return true;
        }
        return false;
    }

    @Override
    public void postDraw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        if (isHovered()) {
            if (getScreen() != null) {
                if (abilityInfo != null) {
                    getScreen().addPostRenderInstruction(new HoveringTextInstruction(
                            abilityInfo.getAbilityName(),
                            getParentCoords(new Vec2i(mouseX, mouseY))));
                }
            }
        }
    }
}
