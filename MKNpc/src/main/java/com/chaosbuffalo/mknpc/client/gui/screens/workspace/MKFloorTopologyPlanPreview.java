package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHallwayLeadInMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mkwidgets.client.gui.instructions.HoveringTextInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MKFloorTopologyPlanPreview extends MKWidget {
    private static final int MAX_FOOTPRINT = 45;
    private static final int BACKGROUND = 0x99101010;
    private static final int PANEL = 0x55000000;
    private static final int TEXT = 0xFFE0E0E0;
    private static final int MUTED_TEXT = 0xFFB8B8B8;
    private static final int CONTROL = 0xFF3A3F46;
    private static final int CONTROL_ACTIVE = 0xFF6B7682;
    private static final int TRACK = 0xFF4D5661;
    private static final int SELECTED_OUTLINE = 0xFFFFD166;
    private static final int EXIT_REQUIRED = 0xFFB8860B;
    private static final int EXIT_ACTIVE = 0xFF9CA3AF;
    private static final int EXIT_INACTIVE = 0xFF4B5563;
    private static final int FLOOR_ROOT = 0xCC6EA46D;
    private static final int FLOOR_MAIN = 0xCC4F8FB8;
    private static final int FLOOR_BRANCH = 0xCCB88A4F;
    private static final int FLOOR_ROOM = 0xCC8A73A8;
    private static final int FLOOR_CAP = 0xCCD18A50;
    private static final int PREVIEW_HEIGHT = 140;
    private static final int PATH_CONTROLS_HEIGHT = 92;
    private static final int ROOM_ROW_HEIGHT = 104;
    private static final int ROOM_SECTION_HEADER = 22;
    private static final int MASK_SIZE = 66;
    private static final int MASK_ROOM_SIZE = 18;
    private static final int MASK_ARM = 15;
    private static final int MASK_ARM_THICKNESS = 11;

    private final String stackId;
    private final String sectionKey;
    private final Controls controls;
    private String draggingSlider = "";

    public MKFloorTopologyPlanPreview(int width, String stackId, String sectionKey, Controls controls) {
        super(0, 0, width, heightFor(controls, sectionKey));
        this.stackId = stackId;
        this.sectionKey = sectionKey;
        this.controls = controls;
    }

    public static int heightFor(Controls controls, String sectionKey) {
        if (!controls.hasFloorTopology(sectionKey)) {
            return 0;
        }
        int mainRows = Math.max(1, controls.roomProfiles(sectionKey, MKWorkspaceFloorRoomKind.MAIN_ROOM).size());
        int branchRows = Math.max(1, controls.roomProfiles(sectionKey, MKWorkspaceFloorRoomKind.BRANCH_ROOM).size());
        return 12 + PREVIEW_HEIGHT + PATH_CONTROLS_HEIGHT + (ROOM_SECTION_HEADER * 2) +
                ((mainRows + branchRows) * ROOM_ROW_HEIGHT) + 18;
    }

    @Override
    public void draw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height,
                     int mouseX, int mouseY, float partialTicks) {
        if (!controls.hasFloorTopology(sectionKey)) {
            return;
        }
        graphics.fill(x, y, x + width, y + height, BACKGROUND);
        graphics.drawString(mc.font, "Floor Plan - " + WorkspaceTopologyUiSupport.formatTopologyLabel(sectionKey),
                x + 8, y + 8, TEXT, false);
        ButtonBounds preview = previewBounds(x, y, width);
        drawPlan(graphics, mc, preview, mouseX, mouseY);
        int cursorY = preview.y() + preview.height() + 8;
        drawPathControls(graphics, mc, x + 8, cursorY, width - 16, mouseX, mouseY);
        cursorY += PATH_CONTROLS_HEIGHT;
        cursorY = drawRoomSection(graphics, mc, x + 8, cursorY, width - 16,
                MKWorkspaceFloorRoomKind.MAIN_ROOM, mouseX, mouseY);
        drawRoomSection(graphics, mc, x + 8, cursorY, width - 16,
                MKWorkspaceFloorRoomKind.BRANCH_ROOM, mouseX, mouseY);
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (!controls.hasFloorTopology(sectionKey)) {
            return false;
        }
        if (handlePathControlPress(mouseX, mouseY, mouseButton)) {
            return true;
        }
        return handleRoomPress(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean onMouseDragged(Minecraft minecraft, double mouseX, double mouseY, int mouseButton,
                                  double dX, double dY) {
        if (draggingSlider.isBlank()) {
            return false;
        }
        applySliderValue(mouseX);
        return true;
    }

    @Override
    public boolean onMouseRelease(double mouseX, double mouseY, int mouseButton) {
        if (!draggingSlider.isBlank()) {
            draggingSlider = "";
            return true;
        }
        return false;
    }

    @Override
    public void longHoverDraw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height,
                              int mouseX, int mouseY, float partialTicks) {
        Optional<String> tooltip = hoveredTooltip(x, y, width, mouseX, mouseY);
        if (tooltip.isEmpty()) {
            return;
        }
        IMKScreen screen = getScreen();
        if (screen != null) {
            Vec2i parentPos = getParentCoords(new Vec2i(mouseX, mouseY));
            screen.addPostRenderInstruction(new HoveringTextInstruction(Component.literal(tooltip.get()), parentPos));
        }
    }

    private void drawPlan(GuiGraphics graphics, Minecraft mc, ButtonBounds panel, int mouseX, int mouseY) {
        graphics.fill(panel.x(), panel.y(), panel.x() + panel.width(), panel.y() + panel.height(), PANEL);
        drawOutline(graphics, panel.x(), panel.y(), panel.width(), panel.y() + panel.height(), CONTROL_ACTIVE);
        List<PlanSegment> segments = planSegments(panel);
        for (PlanSegment segment : segments) {
            boolean hovered = isInRect(mouseX, mouseY, segment.bounds().x(), segment.bounds().y(),
                    segment.bounds().width(), segment.bounds().height());
            graphics.fill(segment.bounds().x(), segment.bounds().y(),
                    segment.bounds().x() + segment.bounds().width(),
                    segment.bounds().y() + segment.bounds().height(),
                    hovered ? brighten(segment.color()) : segment.color());
            drawOutline(graphics, segment.bounds().x(), segment.bounds().y(), segment.bounds().width(),
                    segment.bounds().y() + segment.bounds().height(), hovered ? SELECTED_OUTLINE : 0x553A3F46);
            if (segment.bounds().width() > 20 && segment.bounds().height() > 9) {
                graphics.drawCenteredString(mc.font, Component.literal(segment.label()),
                        segment.bounds().x() + segment.bounds().width() / 2,
                        segment.bounds().y() + Math.max(2, segment.bounds().height() / 2 - 4), TEXT);
            }
        }
        if (segments.size() <= 1) {
            graphics.drawCenteredString(mc.font, Component.literal("No horizontal floor paths"),
                    panel.x() + panel.width() / 2, panel.y() + panel.height() / 2 + 20, MUTED_TEXT);
        }
        graphics.drawString(mc.font, "main " + controls.floorMinMainPathPieces(sectionKey) + "-" +
                        controls.floorMaxMainPathPieces(sectionKey) + "  branch cap " +
                        controls.floorMaxBranchPiecesBeforeCap(sectionKey) + "  lead " +
                        controls.effectiveHallwayLeadInPieces(sectionKey),
                panel.x() + 5, panel.y() + panel.height() - 13, MUTED_TEXT, false);
    }

    private void drawPathControls(GuiGraphics graphics, Minecraft mc, int x, int y, int width,
                                  int mouseX, int mouseY) {
        graphics.drawString(mc.font, "Path Settings", x, y + 3, TEXT, false);
        drawSlider(graphics, mc, "Main Min", controls.floorMinMainPathPieces(sectionKey), 0, 10,
                x, y + 16, width, mouseX, mouseY, "floorMinMain");
        drawSlider(graphics, mc, "Main Max", controls.floorMaxMainPathPieces(sectionKey), 0, 10,
                x, y + 38, width, mouseX, mouseY, "floorMaxMain");
        drawSlider(graphics, mc, "Branch Cap", controls.floorMaxBranchPiecesBeforeCap(sectionKey), 0,
                MKWorkspaceFloorTopologySettings.MAX_BRANCH_PIECES_BEFORE_CAP,
                x, y + 60, width, mouseX, mouseY, "floorBranchCap");
        ButtonBounds modeBounds = new ButtonBounds(x + width - 128, y + 4, 128, 16);
        graphics.fill(modeBounds.x(), modeBounds.y(), modeBounds.x() + modeBounds.width(),
                modeBounds.y() + modeBounds.height(),
                isInRect(mouseX, mouseY, modeBounds.x(), modeBounds.y(), modeBounds.width(), modeBounds.height()) ?
                        CONTROL_ACTIVE : CONTROL);
        graphics.drawString(mc.font, fit("Lead " +
                        WorkspaceTopologyUiSupport.formatTopologyLabel(
                                controls.floorHallwayLeadInMode(sectionKey).getSerializedName()),
                modeBounds.width() - 4), modeBounds.x() + 2, modeBounds.y() + 3, TEXT, false);
        if (controls.floorHallwayLeadInMode(sectionKey) == MKWorkspaceHallwayLeadInMode.MANUAL) {
            drawSlider(graphics, mc, "Lead In", controls.floorManualHallwayLeadInPieces(sectionKey), 0, 10,
                    x + width - 128, y + 26, 128, mouseX, mouseY, "floorLeadIn");
        }
    }

    private int drawRoomSection(GuiGraphics graphics, Minecraft mc, int x, int y, int width,
                                MKWorkspaceFloorRoomKind kind, int mouseX, int mouseY) {
        String label = kind == MKWorkspaceFloorRoomKind.MAIN_ROOM ? "Main Rooms" : "Branch Rooms";
        int color = kind == MKWorkspaceFloorRoomKind.MAIN_ROOM ? FLOOR_MAIN : FLOOR_BRANCH;
        graphics.drawString(mc.font, label, x, y + 4, color, false);
        ButtonBounds add = roomAddButton(x, y, width, kind);
        drawButton(graphics, mc, add, "Add", mouseX, mouseY, false);
        int cursorY = y + ROOM_SECTION_HEADER;
        List<MKWorkspaceFloorRoomProfile> profiles = controls.roomProfiles(sectionKey, kind);
        for (int index = 0; index < profiles.size(); index++) {
            cursorY = drawRoomProfile(graphics, mc, x, cursorY, width, kind, index, profiles.get(index),
                    mouseX, mouseY);
        }
        return cursorY;
    }

    private int drawRoomProfile(GuiGraphics graphics, Minecraft mc, int x, int y, int width,
                                MKWorkspaceFloorRoomKind kind, int index, MKWorkspaceFloorRoomProfile profile,
                                int mouseX, int mouseY) {
        graphics.fill(x, y, x + width, y + ROOM_ROW_HEIGHT - 8, 0x33000000);
        drawOutline(graphics, x, y, width, y + ROOM_ROW_HEIGHT - 8, 0x553A3F46);
        graphics.drawString(mc.font, profile.label(), x + 5, y + 5, TEXT, false);
        ButtonBounds remove = roomRemoveButton(x, y, width, kind, index);
        drawButton(graphics, mc, remove, "Remove", mouseX, mouseY,
                controls.roomProfiles(sectionKey, kind).size() <= 1);

        int sliderWidth = Math.max(90, width - MASK_SIZE - 26);
        drawSlider(graphics, mc, "W", profile.width(), 3, MAX_FOOTPRINT,
                x + 5, y + 20, sliderWidth, mouseX, mouseY, roomSliderId(kind, index, "width"));
        drawSlider(graphics, mc, "L", profile.length(), 3, MAX_FOOTPRINT,
                x + 5, y + 42, sliderWidth, mouseX, mouseY, roomSliderId(kind, index, "length"));
        drawSlider(graphics, mc, "H", profile.height(), controls.floorRoomHeightMin(sectionKey),
                controls.floorRoomHeightMax(sectionKey),
                x + 5, y + 64, sliderWidth, mouseX, mouseY, roomSliderId(kind, index, "height"));
        drawRoomExitMask(graphics, mc, profile, x + width - MASK_SIZE - 8, y + 22, mouseX, mouseY);
        return y + ROOM_ROW_HEIGHT;
    }

    private void drawRoomExitMask(GuiGraphics graphics, Minecraft mc, MKWorkspaceFloorRoomProfile profile,
                                  int x, int y, int mouseX, int mouseY) {
        graphics.fill(x, y, x + MASK_SIZE, y + MASK_SIZE, 0xFF1B1B1F);
        drawOutline(graphics, x, y, MASK_SIZE, y + MASK_SIZE, CONTROL_ACTIVE);
        int centerX = x + MASK_SIZE / 2;
        int centerY = y + MASK_SIZE / 2;
        int roomLeft = centerX - MASK_ROOM_SIZE / 2;
        int roomTop = centerY - MASK_ROOM_SIZE / 2;
        int roomRight = roomLeft + MASK_ROOM_SIZE;
        int roomBottom = roomTop + MASK_ROOM_SIZE;
        for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            drawRoomExitArm(graphics, profile, direction, roomLeft, roomTop, roomRight, roomBottom, centerX, centerY,
                    mouseX, mouseY);
        }
        graphics.fill(roomLeft, roomTop, roomRight, roomBottom, 0xFF2A3440);
        drawOutline(graphics, roomLeft, roomTop, MASK_ROOM_SIZE, roomBottom, CONTROL_ACTIVE);
        graphics.drawCenteredString(mc.font, Component.literal("R"), centerX, centerY - 4, TEXT);
        drawRoomExitLabel(graphics, mc, profile, Direction.NORTH, centerX, roomTop - MASK_ARM - 10);
        drawRoomExitLabel(graphics, mc, profile, Direction.EAST, roomRight + MASK_ARM, centerY - 4);
        drawRoomExitLabel(graphics, mc, profile, Direction.SOUTH, centerX, roomBottom + MASK_ARM + 1);
        drawRoomExitLabel(graphics, mc, profile, Direction.WEST, roomLeft - MASK_ARM, centerY - 4);
    }

    private void drawRoomExitArm(GuiGraphics graphics, MKWorkspaceFloorRoomProfile profile, Direction direction,
                                 int roomLeft, int roomTop, int roomRight, int roomBottom, int centerX, int centerY,
                                 int mouseX, int mouseY) {
        int color = profile.horizontalExits().stream().anyMatch(exit -> exit.direction() == direction) ?
                (profile.requiredExitDirection(direction) ? EXIT_REQUIRED : EXIT_ACTIVE) : EXIT_INACTIVE;
        if (hitRoomExitDirection(centerX - MASK_SIZE / 2, centerY - MASK_SIZE / 2, mouseX, mouseY) == direction &&
                (profile.optionalBranchExitDirection(direction) || profile.mainExitDirection(direction))) {
            color = CONTROL_ACTIVE;
        }
        switch (direction) {
            case NORTH -> graphics.fill(centerX - MASK_ARM_THICKNESS / 2, roomTop - MASK_ARM,
                    centerX + MASK_ARM_THICKNESS / 2, roomTop, color);
            case EAST -> graphics.fill(roomRight, centerY - MASK_ARM_THICKNESS / 2,
                    roomRight + MASK_ARM, centerY + MASK_ARM_THICKNESS / 2, color);
            case SOUTH -> graphics.fill(centerX - MASK_ARM_THICKNESS / 2, roomBottom,
                    centerX + MASK_ARM_THICKNESS / 2, roomBottom + MASK_ARM, color);
            case WEST -> graphics.fill(roomLeft - MASK_ARM, centerY - MASK_ARM_THICKNESS / 2,
                    roomLeft, centerY + MASK_ARM_THICKNESS / 2, color);
            default -> {
            }
        }
    }

    private void drawRoomExitLabel(GuiGraphics graphics, Minecraft mc, MKWorkspaceFloorRoomProfile profile,
                                   Direction direction, int x, int y) {
        Optional<MKWorkspaceFamilyHorizontalExitDefinition> exit = profile.horizontalExits().stream()
                .filter(candidate -> candidate.direction() == direction)
                .findFirst();
        String label = direction.getName().substring(0, 1).toUpperCase();
        int color = MUTED_TEXT;
        if (exit.isPresent()) {
            label += switch (exit.get().pathKind()) {
                case INGRESS -> "G";
                case MAIN_ENTRY -> "I";
                case MAIN_EXIT -> "O";
                case MAIN_ENDING_ENTRY -> "E";
                case BRANCH -> "B";
                case BRANCH_CAP_ENTRY -> "C";
                case VERTICAL_ACCESS -> "";
            };
            color = profile.requiredExitDirection(direction) ? EXIT_REQUIRED : TEXT;
        }
        graphics.drawCenteredString(mc.font, Component.literal(label), x, y, color);
    }

    private List<PlanSegment> planSegments(ButtonBounds panel) {
        ArrayList<PlanSegment> segments = new ArrayList<>();
        int centerX = panel.x() + panel.width() / 2;
        int centerY = panel.y() + panel.height() / 2;
        int rootW = Math.max(18, Math.min(36, controls.stackWidth(sectionKey) * 3));
        int rootH = Math.max(18, Math.min(36, controls.stackLength(sectionKey) * 3));
        ButtonBounds root = new ButtonBounds(centerX - rootW / 2, centerY - rootH / 2, rootW, rootH);
        segments.add(new PlanSegment(root, FLOOR_ROOT, null, "Root",
                "Floor Root\n" + controls.stackWidth(sectionKey) + " x " + controls.stackLength(sectionKey)));
        for (MKWorkspaceFamilyHorizontalExitDefinition exit : controls.rootExits(sectionKey)) {
            if (exit.direction().getAxis().isHorizontal() && !exit.pathKind().isPlannerIngress()) {
                addExitPlanSegments(segments, panel, root, exit);
            }
        }
        return List.copyOf(segments);
    }

    private void addExitPlanSegments(List<PlanSegment> segments, ButtonBounds panel, ButtonBounds root,
                                     MKWorkspaceFamilyHorizontalExitDefinition exit) {
        boolean main = exit.pathKind().usesMainPath();
        int pathPieces = main ? controls.floorMaxMainPathPieces(sectionKey) :
                controls.floorMaxBranchPiecesBeforeCap(sectionKey);
        int leadIn = controls.effectiveHallwayLeadInPieces(sectionKey);
        boolean vertical = exit.direction() == Direction.NORTH || exit.direction() == Direction.SOUTH;
        int requestedSpan = Math.max(vertical ? 6 : 8, (leadIn + pathPieces) * (main ? 10 : 6));
        int terminalBudget = vertical ? (main ? 28 : 24) : (main ? 44 : 38);
        int span = Math.min(requestedSpan, maxSpan(panel, root, exit.direction()) - terminalBudget);
        if (span <= 0) {
            return;
        }
        int thickness = main ? 7 : 5;
        ButtonBounds path = pathRect(root, exit.direction(), span, thickness);
        segments.add(new PlanSegment(path, main ? FLOOR_MAIN : FLOOR_BRANCH, exit.direction(),
                main ? "Main" : "Branch",
                (main ? "Main Path" : "Branch Path") + "\n" + formatDirection(exit.direction()) +
                        "\nlead-in " + leadIn + "\nmax pieces " + pathPieces));
        MKWorkspaceFloorRoomKind roomKind = main ? MKWorkspaceFloorRoomKind.MAIN_ROOM :
                MKWorkspaceFloorRoomKind.BRANCH_ROOM;
        MKWorkspaceFloorRoomProfile profile = controls.roomProfiles(sectionKey, roomKind).getFirst();
        int roomWidth = vertical ? Math.max(10, Math.min(24, profile.width() * 2)) :
                Math.max(12, Math.min(32, profile.width() * 2));
        int roomHeight = vertical ? Math.max(8, Math.min(22, profile.length() * 2)) :
                Math.max(10, Math.min(28, profile.length() * 2));
        ButtonBounds room = terminalRect(path, exit.direction(), roomWidth, roomHeight);
        segments.add(new PlanSegment(room, FLOOR_ROOM, exit.direction(), main ? "Main Room" : "Branch Room",
                profile.label() + "\n" + profile.width() + " x " + profile.length() + "\nheight " +
                        profile.height()));
        ButtonBounds cap = terminalRect(room, exit.direction(),
                vertical ? (main ? 12 : 10) : (main ? 16 : 14),
                vertical ? (main ? 10 : 8) : (main ? 14 : 12));
        segments.add(new PlanSegment(cap, FLOOR_CAP, exit.direction(), main ? "Main Cap" : "Branch Cap",
                (main ? "Main Cap" : "Branch Cap") + "\nterminal content"));
    }

    private boolean handlePathControlPress(double mouseX, double mouseY, int mouseButton) {
        ButtonBounds preview = previewBounds(getX(), getY(), getWidth());
        int y = preview.y() + preview.height() + 8;
        int x = getX() + 8;
        int width = getWidth() - 16;
        if (isInSlider(mouseX, mouseY, sliderBounds(x, y + 16, width, "floorMinMain"))) {
            draggingSlider = "floorMinMain";
            applySliderValue(mouseX);
            return true;
        }
        if (isInSlider(mouseX, mouseY, sliderBounds(x, y + 38, width, "floorMaxMain"))) {
            draggingSlider = "floorMaxMain";
            applySliderValue(mouseX);
            return true;
        }
        if (isInSlider(mouseX, mouseY, sliderBounds(x, y + 60, width, "floorBranchCap"))) {
            draggingSlider = "floorBranchCap";
            applySliderValue(mouseX);
            return true;
        }
        ButtonBounds modeBounds = new ButtonBounds(x + width - 128, y + 4, 128, 16);
        if (isInRect(mouseX, mouseY, modeBounds.x(), modeBounds.y(), modeBounds.width(), modeBounds.height())) {
            controls.cycleFloorHallwayLeadInMode(sectionKey, WorkspaceTopologyUiSupport.isReverseClick(mouseButton));
            return true;
        }
        if (controls.floorHallwayLeadInMode(sectionKey) == MKWorkspaceHallwayLeadInMode.MANUAL &&
                isInSlider(mouseX, mouseY, sliderBounds(x + width - 128, y + 26, 128, "floorLeadIn"))) {
            draggingSlider = "floorLeadIn";
            applySliderValue(mouseX);
            return true;
        }
        return false;
    }

    private boolean handleRoomPress(double mouseX, double mouseY, int mouseButton) {
        int cursorY = previewBounds(getX(), getY(), getWidth()).y() + PREVIEW_HEIGHT + 8 + PATH_CONTROLS_HEIGHT;
        for (MKWorkspaceFloorRoomKind kind : List.of(MKWorkspaceFloorRoomKind.MAIN_ROOM,
                MKWorkspaceFloorRoomKind.BRANCH_ROOM)) {
            int x = getX() + 8;
            int width = getWidth() - 16;
            if (isInRect(mouseX, mouseY, roomAddButton(x, cursorY, width, kind))) {
                controls.addRoomProfile(sectionKey, kind);
                return true;
            }
            cursorY += ROOM_SECTION_HEADER;
            List<MKWorkspaceFloorRoomProfile> profiles = controls.roomProfiles(sectionKey, kind);
            for (int index = 0; index < profiles.size(); index++) {
                if (handleRoomProfilePress(x, cursorY, width, kind, index, profiles.get(index), mouseX, mouseY,
                        mouseButton)) {
                    return true;
                }
                cursorY += ROOM_ROW_HEIGHT;
            }
        }
        return false;
    }

    private boolean handleRoomProfilePress(int x, int y, int width, MKWorkspaceFloorRoomKind kind, int index,
                                           MKWorkspaceFloorRoomProfile profile, double mouseX, double mouseY,
                                           int mouseButton) {
        ButtonBounds remove = roomRemoveButton(x, y, width, kind, index);
        if (isInRect(mouseX, mouseY, remove) && controls.roomProfiles(sectionKey, kind).size() > 1) {
            controls.removeRoomProfile(sectionKey, kind, index);
            return true;
        }
        int sliderWidth = Math.max(90, width - MASK_SIZE - 26);
        for (String field : List.of("width", "length", "height")) {
            int sliderY = switch (field) {
                case "width" -> y + 20;
                case "length" -> y + 42;
                default -> y + 64;
            };
            String id = roomSliderId(kind, index, field);
            if (isInSlider(mouseX, mouseY, sliderBounds(x + 5, sliderY, sliderWidth, id))) {
                draggingSlider = id;
                applySliderValue(mouseX);
                return true;
            }
        }
        Direction direction = hitRoomExitDirection(x + width - MASK_SIZE - 8, y + 22, (int) mouseX, (int) mouseY);
        if (direction != null) {
            if (kind == MKWorkspaceFloorRoomKind.MAIN_ROOM &&
                    mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT &&
                    profile.mainExitDirection(direction)) {
                controls.setRoomMainExitDirection(sectionKey, index, direction);
            } else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT ||
                    (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT && kind == MKWorkspaceFloorRoomKind.BRANCH_ROOM)) {
                controls.toggleRoomBranchExit(sectionKey, kind, index, direction);
            }
            return true;
        }
        return false;
    }

    private void applySliderValue(double mouseX) {
        SliderBounds bounds = sliderBoundsFor(draggingSlider);
        if (bounds == null) {
            return;
        }
        if ("floorMinMain".equals(draggingSlider)) {
            controls.floorMinMainPathPieces(sectionKey, sliderValue(mouseX, bounds, 0, 10));
        } else if ("floorMaxMain".equals(draggingSlider)) {
            controls.floorMaxMainPathPieces(sectionKey, sliderValue(mouseX, bounds, 0, 10));
        } else if ("floorBranchCap".equals(draggingSlider)) {
            controls.floorMaxBranchPiecesBeforeCap(sectionKey, sliderValue(mouseX, bounds, 0,
                    MKWorkspaceFloorTopologySettings.MAX_BRANCH_PIECES_BEFORE_CAP));
        } else if ("floorLeadIn".equals(draggingSlider)) {
            controls.floorManualHallwayLeadInPieces(sectionKey, sliderValue(mouseX, bounds, 0, 10));
        } else if (draggingSlider.startsWith("room:")) {
            String[] parts = draggingSlider.split(":");
            MKWorkspaceFloorRoomKind kind = "main".equals(parts[1]) ? MKWorkspaceFloorRoomKind.MAIN_ROOM :
                    MKWorkspaceFloorRoomKind.BRANCH_ROOM;
            int index = Integer.parseInt(parts[2]);
            String field = parts[3];
            if ("width".equals(field)) {
                controls.floorRoomWidth(sectionKey, kind, index,
                        makeOdd(sliderValue(mouseX, bounds, 3, MAX_FOOTPRINT)));
            } else if ("length".equals(field)) {
                controls.floorRoomLength(sectionKey, kind, index,
                        makeOdd(sliderValue(mouseX, bounds, 3, MAX_FOOTPRINT)));
            } else if ("height".equals(field)) {
                controls.floorRoomHeight(sectionKey, kind, index,
                        sliderValue(mouseX, bounds, controls.floorRoomHeightMin(sectionKey),
                                controls.floorRoomHeightMax(sectionKey)));
            }
        }
    }

    private Optional<String> hoveredTooltip(int x, int y, int width, int mouseX, int mouseY) {
        ButtonBounds preview = previewBounds(x, y, width);
        if (isInRect(mouseX, mouseY, preview.x(), preview.y(), preview.width(), preview.height())) {
            return planSegments(preview).stream()
                    .filter(segment -> isInRect(mouseX, mouseY, segment.bounds().x(), segment.bounds().y(),
                            segment.bounds().width(), segment.bounds().height()))
                    .findFirst()
                    .map(PlanSegment::tooltip)
                    .or(() -> Optional.of("Floor Plan\nTop-down generation preview"));
        }
        int cursorY = preview.y() + PREVIEW_HEIGHT + 8 + PATH_CONTROLS_HEIGHT;
        for (MKWorkspaceFloorRoomKind kind : List.of(MKWorkspaceFloorRoomKind.MAIN_ROOM,
                MKWorkspaceFloorRoomKind.BRANCH_ROOM)) {
            cursorY += ROOM_SECTION_HEADER;
            List<MKWorkspaceFloorRoomProfile> profiles = controls.roomProfiles(sectionKey, kind);
            for (int index = 0; index < profiles.size(); index++) {
                Direction direction = hitRoomExitDirection(x + width - 16 - MASK_SIZE, cursorY + 22, mouseX, mouseY);
                if (direction != null) {
                    MKWorkspaceFloorRoomProfile profile = profiles.get(index);
                    if (profile.requiredExitDirection(direction)) {
                        return Optional.of(formatDirection(direction) + "\nRequired " +
                                requiredExitLabel(profile, direction));
                    }
                    if (profile.kind() == MKWorkspaceFloorRoomKind.MAIN_ROOM &&
                            profile.mainExitDirection(direction)) {
                        boolean current = profile.mainExitDirection()
                                .map(direction::equals)
                                .orElse(false);
                        return Optional.of(formatDirection(direction) +
                                (current ? "\nMain path exit" : "\nLeft click: set main path exit") +
                                (profile.optionalBranchExitDirection(direction) ? "\nRight click: toggle branch exit" : ""));
                    }
                    if (profile.optionalBranchExitDirection(direction)) {
                        return Optional.of(formatDirection(direction) +
                                "\nOptional branch exit\nClick to toggle");
                    }
                    return Optional.of(formatDirection(direction));
                }
                cursorY += ROOM_ROW_HEIGHT;
            }
        }
        return Optional.empty();
    }

    private String requiredExitLabel(MKWorkspaceFloorRoomProfile profile, Direction direction) {
        return profile.horizontalExits().stream()
                .filter(exit -> exit.direction() == direction)
                .findFirst()
                .map(exit -> WorkspaceTopologyUiSupport.formatTopologyLabel(exit.pathKind().getSerializedName()))
                .orElse("exit");
    }

    private SliderBounds sliderBoundsFor(String id) {
        ButtonBounds preview = previewBounds(getX(), getY(), getWidth());
        int x = getX() + 8;
        int width = getWidth() - 16;
        int pathY = preview.y() + preview.height() + 8;
        if ("floorMinMain".equals(id)) {
            return sliderBounds(x, pathY + 16, width, id);
        }
        if ("floorMaxMain".equals(id)) {
            return sliderBounds(x, pathY + 38, width, id);
        }
        if ("floorBranchCap".equals(id)) {
            return sliderBounds(x, pathY + 60, width, id);
        }
        if ("floorLeadIn".equals(id)) {
            return sliderBounds(x + width - 128, pathY + 26, 128, id);
        }
        if (!id.startsWith("room:")) {
            return null;
        }
        String[] parts = id.split(":");
        MKWorkspaceFloorRoomKind targetKind = "main".equals(parts[1]) ? MKWorkspaceFloorRoomKind.MAIN_ROOM :
                MKWorkspaceFloorRoomKind.BRANCH_ROOM;
        int targetIndex = Integer.parseInt(parts[2]);
        String field = parts[3];
        int cursorY = preview.y() + PREVIEW_HEIGHT + 8 + PATH_CONTROLS_HEIGHT;
        for (MKWorkspaceFloorRoomKind kind : List.of(MKWorkspaceFloorRoomKind.MAIN_ROOM,
                MKWorkspaceFloorRoomKind.BRANCH_ROOM)) {
            cursorY += ROOM_SECTION_HEADER;
            int count = controls.roomProfiles(sectionKey, kind).size();
            if (kind == targetKind) {
                int rowY = cursorY + targetIndex * ROOM_ROW_HEIGHT;
                int sliderY = switch (field) {
                    case "width" -> rowY + 20;
                    case "length" -> rowY + 42;
                    default -> rowY + 64;
                };
                return sliderBounds(getX() + 13, sliderY, Math.max(90, getWidth() - 16 - MASK_SIZE - 26), id);
            }
            cursorY += count * ROOM_ROW_HEIGHT;
        }
        return null;
    }

    private ButtonBounds previewBounds(int x, int y, int width) {
        return new ButtonBounds(x + 8, y + 24, width - 16, PREVIEW_HEIGHT);
    }

    private ButtonBounds roomAddButton(int x, int y, int width, MKWorkspaceFloorRoomKind kind) {
        return new ButtonBounds(x + width - 46, y + 2, 46, 16);
    }

    private ButtonBounds roomRemoveButton(int x, int y, int width, MKWorkspaceFloorRoomKind kind, int index) {
        return new ButtonBounds(x + width - 58, y + 4, 54, 16);
    }

    private String roomSliderId(MKWorkspaceFloorRoomKind kind, int index, String field) {
        return "room:" + (kind == MKWorkspaceFloorRoomKind.MAIN_ROOM ? "main" : "branch") + ":" + index + ":" +
                field;
    }

    private int maxSpan(ButtonBounds panel, ButtonBounds root, Direction direction) {
        int padding = 10;
        return switch (direction) {
            case EAST -> panel.x() + panel.width() - padding - (root.x() + root.width());
            case WEST -> root.x() - (panel.x() + padding);
            case SOUTH -> panel.y() + panel.height() - 20 - (root.y() + root.height());
            case NORTH -> root.y() - (panel.y() + 22);
            default -> 0;
        };
    }

    private ButtonBounds pathRect(ButtonBounds root, Direction direction, int span, int thickness) {
        int centerX = root.x() + root.width() / 2;
        int centerY = root.y() + root.height() / 2;
        return switch (direction) {
            case EAST -> new ButtonBounds(root.x() + root.width(), centerY - thickness / 2, span, thickness);
            case WEST -> new ButtonBounds(root.x() - span, centerY - thickness / 2, span, thickness);
            case SOUTH -> new ButtonBounds(centerX - thickness / 2, root.y() + root.height(), thickness, span);
            case NORTH -> new ButtonBounds(centerX - thickness / 2, root.y() - span, thickness, span);
            default -> new ButtonBounds(root.x(), root.y(), 1, 1);
        };
    }

    private ButtonBounds terminalRect(ButtonBounds previous, Direction direction, int width, int height) {
        int centerX = previous.x() + previous.width() / 2;
        int centerY = previous.y() + previous.height() / 2;
        return switch (direction) {
            case EAST -> new ButtonBounds(previous.x() + previous.width() + 2, centerY - height / 2, width, height);
            case WEST -> new ButtonBounds(previous.x() - width - 2, centerY - height / 2, width, height);
            case SOUTH -> new ButtonBounds(centerX - width / 2, previous.y() + previous.height() + 2, width, height);
            case NORTH -> new ButtonBounds(centerX - width / 2, previous.y() - height - 2, width, height);
            default -> new ButtonBounds(previous.x(), previous.y(), width, height);
        };
    }

    private Direction hitRoomExitDirection(int x, int y, int mouseX, int mouseY) {
        int centerX = x + MASK_SIZE / 2;
        int centerY = y + MASK_SIZE / 2;
        int roomLeft = centerX - MASK_ROOM_SIZE / 2;
        int roomTop = centerY - MASK_ROOM_SIZE / 2;
        int roomRight = roomLeft + MASK_ROOM_SIZE;
        int roomBottom = roomTop + MASK_ROOM_SIZE;
        if (mouseX >= centerX - MASK_ARM_THICKNESS / 2 && mouseX <= centerX + MASK_ARM_THICKNESS / 2) {
            if (mouseY >= roomTop - MASK_ARM && mouseY <= roomTop) {
                return Direction.NORTH;
            }
            if (mouseY >= roomBottom && mouseY <= roomBottom + MASK_ARM) {
                return Direction.SOUTH;
            }
        }
        if (mouseY >= centerY - MASK_ARM_THICKNESS / 2 && mouseY <= centerY + MASK_ARM_THICKNESS / 2) {
            if (mouseX >= roomLeft - MASK_ARM && mouseX <= roomLeft) {
                return Direction.WEST;
            }
            if (mouseX >= roomRight && mouseX <= roomRight + MASK_ARM) {
                return Direction.EAST;
            }
        }
        return null;
    }

    private void drawSlider(GuiGraphics graphics, Minecraft mc, String label, int value, int min, int max,
                            int x, int y, int width, int mouseX, int mouseY, String id) {
        graphics.drawString(mc.font, label + " " + value, x, y, TEXT, false);
        SliderBounds bounds = sliderBounds(x, y, width, id);
        boolean hovered = isInSlider(mouseX, mouseY, bounds);
        graphics.fill(bounds.trackX(), bounds.trackY(), bounds.trackX() + bounds.trackWidth(),
                bounds.trackY() + 3, hovered ? CONTROL_ACTIVE : TRACK);
        int knobX = bounds.trackX() + Math.round(((value - min) / (float) Math.max(1, max - min)) *
                bounds.trackWidth());
        graphics.fill(knobX - 2, bounds.trackY() - 3, knobX + 3, bounds.trackY() + 7, SELECTED_OUTLINE);
    }

    private void drawButton(GuiGraphics graphics, Minecraft mc, ButtonBounds bounds, String label,
                            int mouseX, int mouseY, boolean disabled) {
        boolean hovered = !disabled && isInRect(mouseX, mouseY, bounds.x(), bounds.y(), bounds.width(),
                bounds.height());
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(),
                disabled ? EXIT_INACTIVE : hovered ? CONTROL_ACTIVE : CONTROL);
        graphics.drawCenteredString(mc.font, Component.literal(label),
                bounds.x() + bounds.width() / 2, bounds.y() + 4, disabled ? MUTED_TEXT : TEXT);
    }

    private SliderBounds sliderBounds(int x, int y, int width, String id) {
        int trackX = x + 52;
        return new SliderBounds(id, trackX, y + 6, Math.max(24, width - 58));
    }

    private boolean isInSlider(double mouseX, double mouseY, SliderBounds bounds) {
        return mouseX >= bounds.trackX() && mouseX <= bounds.trackX() + bounds.trackWidth() &&
                mouseY >= bounds.trackY() - 4 && mouseY <= bounds.trackY() + 8;
    }

    private boolean isInRect(double mouseX, double mouseY, ButtonBounds bounds) {
        return isInRect(mouseX, mouseY, bounds.x(), bounds.y(), bounds.width(), bounds.height());
    }

    private boolean isInRect(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private int sliderValue(double mouseX, SliderBounds bounds, int min, int max) {
        float percent = (float) ((mouseX - bounds.trackX()) / Math.max(1.0, bounds.trackWidth()));
        percent = Math.max(0.0f, Math.min(1.0f, percent));
        return min + Math.round(percent * (max - min));
    }

    private int makeOdd(int value) {
        return value % 2 == 0 ? value + 1 : value;
    }

    private int brighten(int color) {
        return (color & 0xFF000000) | Math.min(0xFF, ((color >> 16) & 0xFF) + 30) << 16 |
                Math.min(0xFF, ((color >> 8) & 0xFF) + 30) << 8 |
                Math.min(0xFF, (color & 0xFF) + 30);
    }

    private void drawOutline(GuiGraphics graphics, int left, int top, int width, int bottom, int color) {
        graphics.fill(left, top, left + width, top + 1, color);
        graphics.fill(left, bottom - 1, left + width, bottom, color);
        graphics.fill(left, top, left + 1, bottom, color);
        graphics.fill(left + width - 1, top, left + width, bottom, color);
    }

    private String formatDirection(Direction direction) {
        return WorkspaceTopologyUiSupport.formatTopologyLabel(direction.getSerializedName());
    }

    private String fit(String value, int width) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.font.width(value) <= width) {
            return value;
        }
        String ellipsis = "...";
        int maxWidth = Math.max(0, width - mc.font.width(ellipsis));
        String trimmed = value;
        while (!trimmed.isEmpty() && mc.font.width(trimmed) > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + ellipsis;
    }

    public interface Controls {
        boolean hasFloorTopology(String sectionKey);

        int stackWidth(String sectionKey);

        int stackLength(String sectionKey);

        List<MKWorkspaceFamilyHorizontalExitDefinition> rootExits(String sectionKey);

        int floorMinMainPathPieces(String sectionKey);

        void floorMinMainPathPieces(String sectionKey, int value);

        int floorMaxMainPathPieces(String sectionKey);

        void floorMaxMainPathPieces(String sectionKey, int value);

        int floorMaxBranchPiecesBeforeCap(String sectionKey);

        void floorMaxBranchPiecesBeforeCap(String sectionKey, int value);

        MKWorkspaceHallwayLeadInMode floorHallwayLeadInMode(String sectionKey);

        void cycleFloorHallwayLeadInMode(String sectionKey, boolean reverse);

        int floorManualHallwayLeadInPieces(String sectionKey);

        void floorManualHallwayLeadInPieces(String sectionKey, int value);

        int recommendedHallwayLeadInPieces(String sectionKey);

        default int effectiveHallwayLeadInPieces(String sectionKey) {
            return floorHallwayLeadInMode(sectionKey) == MKWorkspaceHallwayLeadInMode.MANUAL ?
                    floorManualHallwayLeadInPieces(sectionKey) :
                    recommendedHallwayLeadInPieces(sectionKey);
        }

        int floorRoomHeightMin(String sectionKey);

        int floorRoomHeightMax(String sectionKey);

        List<MKWorkspaceFloorRoomProfile> roomProfiles(String sectionKey, MKWorkspaceFloorRoomKind kind);

        void floorRoomWidth(String sectionKey, MKWorkspaceFloorRoomKind kind, int index, int value);

        void floorRoomLength(String sectionKey, MKWorkspaceFloorRoomKind kind, int index, int value);

        void floorRoomHeight(String sectionKey, MKWorkspaceFloorRoomKind kind, int index, int value);

        void addRoomProfile(String sectionKey, MKWorkspaceFloorRoomKind kind);

        void removeRoomProfile(String sectionKey, MKWorkspaceFloorRoomKind kind, int index);

        void setRoomMainExitDirection(String sectionKey, int index, Direction direction);

        void toggleRoomBranchExit(String sectionKey, MKWorkspaceFloorRoomKind kind, int index, Direction direction);
    }

    private record ButtonBounds(int x, int y, int width, int height) {
    }

    private record SliderBounds(String id, int trackX, int trackY, int trackWidth) {
    }

    private record PlanSegment(ButtonBounds bounds, int color, Direction direction, String label, String tooltip) {
    }
}
