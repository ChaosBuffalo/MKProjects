package com.chaosbuffalo.mknpc.client.gui.widgets;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

public class MKBranchExitMaskWidget extends MKWidget {
    private static final int WIDGET_SIZE = 92;
    private static final int ROOM_SIZE = 28;
    private static final int ARM_LENGTH = 18;
    private static final int ARM_THICKNESS = 10;
    private static final int BACKGROUND = 0xFF1B1B1F;
    private static final int BORDER = 0xFF72727A;
    private static final int ROOM_FILL = 0xFF2A3440;
    private static final int ACTIVE_EXIT = 0xFF9CA3AF;
    private static final int INACTIVE_EXIT = 0xFF4B5563;
    private static final int SELECTED_MAIN_EXIT = 0xFF60A5FA;
    private static final int SELECTED_BRANCH_EXIT = 0xFF74C69D;
    private static final int LABEL_ACTIVE = 0xFFF8FAFC;
    private static final int LABEL_INACTIVE = 0xFF9CA3AF;

    private List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits;
    private Consumer<Direction> editCallback;
    private Consumer<Direction> toggleCallback;
    private Direction selectedDirection;

    public MKBranchExitMaskWidget(List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits) {
        super(0, 0, WIDGET_SIZE, WIDGET_SIZE);
        this.horizontalExits = List.copyOf(horizontalExits);
        setTooltip(Component.literal("Left click an active exit to edit it. Right click to toggle exits on or off."));
    }

    public MKBranchExitMaskWidget setEditCallback(Consumer<Direction> editCallback) {
        this.editCallback = editCallback;
        return this;
    }

    public MKBranchExitMaskWidget setToggleCallback(Consumer<Direction> toggleCallback) {
        this.toggleCallback = toggleCallback;
        return this;
    }

    public MKBranchExitMaskWidget setSelectedDirection(Direction selectedDirection) {
        this.selectedDirection = selectedDirection;
        return this;
    }

    public void setHorizontalExits(List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits) {
        this.horizontalExits = List.copyOf(horizontalExits);
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        Direction direction = hitDirection((int) mouseX, (int) mouseY);
        if (direction == null) {
            return false;
        }
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT && editCallback != null && exitKind(direction) != ExitKind.NONE) {
            editCallback.accept(direction);
            return true;
        }
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT && toggleCallback != null) {
            toggleCallback.accept(direction);
            return true;
        }
        return false;
    }

    @Override
    public void draw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY,
                     float partialTicks) {
        graphics.fill(x, y, x + width, y + height, BACKGROUND);
        graphics.fill(x, y, x + width, y + 1, BORDER);
        graphics.fill(x, y + height - 1, x + width, y + height, BORDER);
        graphics.fill(x, y, x + 1, y + height, BORDER);
        graphics.fill(x + width - 1, y, x + width, y + height, BORDER);

        int centerX = x + (width / 2);
        int centerY = y + (height / 2);
        int roomLeft = centerX - (ROOM_SIZE / 2);
        int roomTop = centerY - (ROOM_SIZE / 2);
        int roomRight = roomLeft + ROOM_SIZE;
        int roomBottom = roomTop + ROOM_SIZE;

        drawExit(graphics, Direction.NORTH, roomLeft, roomTop, roomRight, roomBottom, centerX, centerY);
        drawExit(graphics, Direction.EAST, roomLeft, roomTop, roomRight, roomBottom, centerX, centerY);
        drawExit(graphics, Direction.SOUTH, roomLeft, roomTop, roomRight, roomBottom, centerX, centerY);
        drawExit(graphics, Direction.WEST, roomLeft, roomTop, roomRight, roomBottom, centerX, centerY);

        graphics.fill(roomLeft, roomTop, roomRight, roomBottom, ROOM_FILL);
        graphics.fill(roomLeft, roomTop, roomRight, roomTop + 1, BORDER);
        graphics.fill(roomLeft, roomBottom - 1, roomRight, roomBottom, BORDER);
        graphics.fill(roomLeft, roomTop, roomLeft + 1, roomBottom, BORDER);
        graphics.fill(roomRight - 1, roomTop, roomRight, roomBottom, BORDER);

        graphics.drawCenteredString(mc.font, Component.literal("ROOM"), centerX, centerY - 4, LABEL_ACTIVE);
        drawDirectionLabel(graphics, mc, Direction.NORTH, centerX, roomTop - ARM_LENGTH - 11);
        drawDirectionLabel(graphics, mc, Direction.EAST, roomRight + ARM_LENGTH - 2, centerY - 4);
        drawDirectionLabel(graphics, mc, Direction.SOUTH, centerX, roomBottom + ARM_LENGTH - 1);
        drawDirectionLabel(graphics, mc, Direction.WEST, roomLeft - ARM_LENGTH + 2, centerY - 4);
    }

    private void drawExit(GuiGraphics graphics, Direction direction, int roomLeft, int roomTop, int roomRight, int roomBottom,
                          int centerX, int centerY) {
        int color = exitColor(direction);
        switch (direction) {
            case NORTH -> {
                graphics.fill(centerX - (ARM_THICKNESS / 2), roomTop - ARM_LENGTH,
                        centerX + (ARM_THICKNESS / 2), roomTop, color);
            }
            case EAST -> {
                graphics.fill(roomRight, centerY - (ARM_THICKNESS / 2),
                        roomRight + ARM_LENGTH, centerY + (ARM_THICKNESS / 2), color);
            }
            case SOUTH -> {
                graphics.fill(centerX - (ARM_THICKNESS / 2), roomBottom,
                        centerX + (ARM_THICKNESS / 2), roomBottom + ARM_LENGTH, color);
            }
            case WEST -> {
                graphics.fill(roomLeft - ARM_LENGTH, centerY - (ARM_THICKNESS / 2),
                        roomLeft, centerY + (ARM_THICKNESS / 2), color);
            }
            default -> {
            }
        }
    }

    private int exitColor(Direction direction) {
        ExitKind kind = exitKind(direction);
        if (selectedDirection == direction) {
            return switch (kind) {
                case MAIN -> SELECTED_MAIN_EXIT;
                case BRANCH -> SELECTED_BRANCH_EXIT;
                case NONE -> INACTIVE_EXIT;
            };
        }
        return kind == ExitKind.NONE ? INACTIVE_EXIT : ACTIVE_EXIT;
    }

    private void drawDirectionLabel(GuiGraphics graphics, Minecraft minecraft, Direction direction, int x, int y) {
        ExitKind exitKind = exitKind(direction);
        int color = exitKind == ExitKind.NONE ? LABEL_INACTIVE : LABEL_ACTIVE;
        String label = switch (exitKind) {
            case MAIN -> direction.getName().substring(0, 1).toUpperCase() + "M";
            case BRANCH -> direction.getName().substring(0, 1).toUpperCase() + "B";
            case NONE -> direction.getName().substring(0, 1).toUpperCase();
        };
        graphics.drawCenteredString(minecraft.font, Component.literal(label), x, y, color);
    }

    private ExitKind exitKind(Direction direction) {
        return horizontalExits.stream()
                .filter(exit -> exit.direction() == direction)
                .findFirst()
                .map(exit -> exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN ? ExitKind.MAIN : ExitKind.BRANCH)
                .orElse(ExitKind.NONE);
    }

    private Direction hitDirection(int mouseX, int mouseY) {
        int centerX = getX() + (getWidth() / 2);
        int centerY = getY() + (getHeight() / 2);
        int roomLeft = centerX - (ROOM_SIZE / 2);
        int roomTop = centerY - (ROOM_SIZE / 2);
        int roomRight = roomLeft + ROOM_SIZE;
        int roomBottom = roomTop + ROOM_SIZE;

        if (mouseX >= centerX - (ARM_THICKNESS / 2) && mouseX <= centerX + (ARM_THICKNESS / 2)) {
            if (mouseY >= roomTop - ARM_LENGTH && mouseY <= roomTop) {
                return Direction.NORTH;
            }
            if (mouseY >= roomBottom && mouseY <= roomBottom + ARM_LENGTH) {
                return Direction.SOUTH;
            }
        }
        if (mouseY >= centerY - (ARM_THICKNESS / 2) && mouseY <= centerY + (ARM_THICKNESS / 2)) {
            if (mouseX >= roomLeft - ARM_LENGTH && mouseX <= roomLeft) {
                return Direction.WEST;
            }
            if (mouseX >= roomRight && mouseX <= roomRight + ARM_LENGTH) {
                return Direction.EAST;
            }
        }
        return null;
    }

    private enum ExitKind {
        NONE,
        MAIN,
        BRANCH
    }
}
