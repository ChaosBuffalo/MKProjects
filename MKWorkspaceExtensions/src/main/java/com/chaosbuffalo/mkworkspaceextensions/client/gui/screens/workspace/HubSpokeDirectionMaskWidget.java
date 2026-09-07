package com.chaosbuffalo.mkworkspaceextensions.client.gui.screens.workspace;

import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public class HubSpokeDirectionMaskWidget extends MKWidget {
    private static final int BACKGROUND = 0xFF1B1B1F;
    private static final int ACTIVE = 0xFF6EA46D;
    private static final int INACTIVE = 0xFF4B5563;
    private static final int HOVER = 0xFF8EC5FF;
    private static final int CENTER = 0xFF2A3440;
    private static final int OUTLINE = 0xFF8A8A8A;
    private static final int TEXT = 0xFFE0E0E0;
    private static final int MUTED_TEXT = 0xFFB8B8B8;

    private static final int ROOM_SIZE = 20;
    private static final int ARM_LENGTH = 14;
    private static final int ARM_THICKNESS = 8;

    private final List<Direction> enabledDirections;
    private final Consumer<Direction> toggleCallback;

    public HubSpokeDirectionMaskWidget(int size, List<Direction> enabledDirections,
                                       Consumer<Direction> toggleCallback) {
        super(0, 0, size, size);
        this.enabledDirections = List.copyOf(enabledDirections);
        this.toggleCallback = toggleCallback;
    }

    @Override
    public void draw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height,
                     int mouseX, int mouseY, float partialTicks) {
        graphics.fill(x, y, x + width, y + height, BACKGROUND);
        drawOutline(graphics, x, y, width, height);
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int roomLeft = centerX - ROOM_SIZE / 2;
        int roomTop = centerY - ROOM_SIZE / 2;
        int roomRight = roomLeft + ROOM_SIZE;
        int roomBottom = roomTop + ROOM_SIZE;

        for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            drawArm(graphics, direction, roomLeft, roomTop, roomRight, roomBottom, centerX, centerY,
                    hitDirection(mouseX, mouseY) == direction);
        }

        graphics.fill(roomLeft, roomTop, roomRight, roomBottom, CENTER);
        drawOutline(graphics, roomLeft, roomTop, ROOM_SIZE, ROOM_SIZE);
        graphics.drawCenteredString(mc.font, Component.literal("S"), centerX, centerY - 4, TEXT);
        drawLabel(graphics, mc, Direction.NORTH, centerX, roomTop - ARM_LENGTH - 10);
        drawLabel(graphics, mc, Direction.EAST, roomRight + ARM_LENGTH, centerY - 4);
        drawLabel(graphics, mc, Direction.SOUTH, centerX, roomBottom + ARM_LENGTH + 1);
        drawLabel(graphics, mc, Direction.WEST, roomLeft - ARM_LENGTH, centerY - 4);
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        Direction direction = hitDirection((int) mouseX, (int) mouseY);
        if (direction == null) {
            return false;
        }
        toggleCallback.accept(direction);
        return true;
    }

    private void drawArm(GuiGraphics graphics, Direction direction, int roomLeft, int roomTop,
                         int roomRight, int roomBottom, int centerX, int centerY, boolean hovered) {
        int color = enabledDirections.contains(direction) ? ACTIVE : INACTIVE;
        if (hovered) {
            color = HOVER;
        }
        switch (direction) {
            case NORTH -> graphics.fill(centerX - ARM_THICKNESS / 2, roomTop - ARM_LENGTH,
                    centerX + ARM_THICKNESS / 2, roomTop, color);
            case EAST -> graphics.fill(roomRight, centerY - ARM_THICKNESS / 2,
                    roomRight + ARM_LENGTH, centerY + ARM_THICKNESS / 2, color);
            case SOUTH -> graphics.fill(centerX - ARM_THICKNESS / 2, roomBottom,
                    centerX + ARM_THICKNESS / 2, roomBottom + ARM_LENGTH, color);
            case WEST -> graphics.fill(roomLeft - ARM_LENGTH, centerY - ARM_THICKNESS / 2,
                    roomLeft, centerY + ARM_THICKNESS / 2, color);
            default -> {
            }
        }
    }

    private void drawLabel(GuiGraphics graphics, Minecraft mc, Direction direction, int x, int y) {
        int color = enabledDirections.contains(direction) ? TEXT : MUTED_TEXT;
        graphics.drawCenteredString(mc.font,
                Component.literal(direction.getSerializedName().substring(0, 1).toUpperCase()), x, y, color);
    }

    private Direction hitDirection(int mouseX, int mouseY) {
        int centerX = getX() + getWidth() / 2;
        int centerY = getY() + getHeight() / 2;
        int roomLeft = centerX - ROOM_SIZE / 2;
        int roomTop = centerY - ROOM_SIZE / 2;
        int roomRight = roomLeft + ROOM_SIZE;
        int roomBottom = roomTop + ROOM_SIZE;
        if (inRect(mouseX, mouseY, centerX - ARM_THICKNESS / 2, roomTop - ARM_LENGTH,
                ARM_THICKNESS, ARM_LENGTH)) {
            return Direction.NORTH;
        }
        if (inRect(mouseX, mouseY, roomRight, centerY - ARM_THICKNESS / 2,
                ARM_LENGTH, ARM_THICKNESS)) {
            return Direction.EAST;
        }
        if (inRect(mouseX, mouseY, centerX - ARM_THICKNESS / 2, roomBottom,
                ARM_THICKNESS, ARM_LENGTH)) {
            return Direction.SOUTH;
        }
        if (inRect(mouseX, mouseY, roomLeft - ARM_LENGTH, centerY - ARM_THICKNESS / 2,
                ARM_LENGTH, ARM_THICKNESS)) {
            return Direction.WEST;
        }
        return null;
    }

    private boolean inRect(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    private void drawOutline(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + 1, OUTLINE);
        graphics.fill(x, y + height - 1, x + width, y + height, OUTLINE);
        graphics.fill(x, y, x + 1, y + height, OUTLINE);
        graphics.fill(x + width - 1, y, x + width, y + height, OUTLINE);
    }
}
