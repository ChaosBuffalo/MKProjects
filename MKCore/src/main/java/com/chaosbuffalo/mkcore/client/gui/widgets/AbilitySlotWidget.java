package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.GuiTextures;
import com.chaosbuffalo.mkcore.client.gui.IAbilityScreen;
import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerSlotAbilityPacket;
import com.chaosbuffalo.mkwidgets.client.gui.UIConstants;
import com.chaosbuffalo.mkwidgets.client.gui.actions.IDragState;
import com.chaosbuffalo.mkwidgets.client.gui.actions.WidgetHoldingDragState;
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
import net.minecraft.world.entity.player.Player;

public class AbilitySlotWidget extends MKLayout {
    private final AbilityGroupId slotGroup;
    private boolean unlocked;
    private final int slotIndex;
    private final IAbilityScreen screen;
    private ResourceLocation abilityId;
    private MKImage background;
    private MKImage icon;

    public AbilitySlotWidget(int x, int y, AbilityGroupId group, int slotIndex, IAbilityScreen screen) {
        super(x, y, 20, 20);
        this.slotGroup = group;
        this.screen = screen;
        this.slotIndex = slotIndex;
        this.setMargins(2, 2, 2, 2);
        this.abilityId = MKCoreRegistry.INVALID_ABILITY;
        this.icon = null;
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
        Player playerEntity = Minecraft.getInstance().player;
        if (playerEntity == null)
            return;
        MKCore.getPlayer(playerEntity).ifPresent(playerData -> {
            abilityId = playerData.getLoadout().getAbilityInSlot(slotGroup, slotIndex);
            setupBackground(playerData);
            setupIcon(abilityId);
        });
    }

    private void setupBackground(MKPlayerData playerData) {
        if (background != null) {
            removeWidget(background);
        }
        AbilityGroup abilityGroup = playerData.getLoadout().getAbilityGroup(slotGroup);
        unlocked = abilityGroup.isSlotUnlocked(slotIndex);
        background = getAbilityGroupSlotImage(slotGroup, unlocked);
        addWidget(background);
        addConstraintToWidget(new FillConstraint(), background);
    }

    private void setupIcon(ResourceLocation newAbilityId) {
        if (icon != null) {
            removeWidget(icon);
        }
        if (!this.abilityId.equals(MKCoreRegistry.INVALID_ABILITY)) {
            MKAbility ability = MKCoreRegistry.getAbility(newAbilityId);
            if (ability != null) {
                icon = new MKImage(0, 0, 16, 16, ability.getAbilityIcon());
                addWidget(icon);
                addConstraintToWidget(MarginConstraint.TOP, icon);
                addConstraintToWidget(MarginConstraint.LEFT, icon);
            }
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

    public ResourceLocation getAbilityId() {
        return abilityId;
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
            if (!(abilityId.equals(MKCoreRegistry.INVALID_ABILITY))) {
                MKAbility ability = MKCoreRegistry.getAbility(getAbilityId());
                if (ability == null) {
                    return false;
                }
                screen.setDragState(new WidgetHoldingDragState(new MKImage(0, 0, icon.getWidth(),
                        icon.getHeight(), icon.getImageLoc())), this);
                screen.startDraggingAbility(ability);
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
            if (unlocked && slotGroup.fitsAbilityType(screen.getDraggingAbility().getType())) {
                ResourceLocation ability = screen.getDraggingAbility().getAbilityId();
                setSlotToAbility(ability);
            }
            screen.stopDraggingAbility();
            screen.clearDragState();
            return true;
        }
        return false;
    }

    @Override
    public void postDraw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        if (isHovered()) {
            if (getScreen() != null) {
                if (!getAbilityId().equals(MKCoreRegistry.INVALID_ABILITY)) {
                    MKAbility ability = MKCoreRegistry.getAbility(getAbilityId());
                    if (ability != null) {
                        getScreen().addPostRenderInstruction(new HoveringTextInstruction(
                                ability.getAbilityName(),
                                getParentCoords(new Vec2i(mouseX, mouseY))));
                    }
                }
            }
        }
    }
}
