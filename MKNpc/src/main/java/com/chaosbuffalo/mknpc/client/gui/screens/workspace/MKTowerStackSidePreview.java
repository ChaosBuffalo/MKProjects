package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHallwayLeadInMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKTowerStackSizingReport;
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
import java.util.function.Consumer;

public class MKTowerStackSidePreview extends MKWidget {
    private static final int MAX_STACK_FOOTPRINT = 45;
    private static final int BACKGROUND = 0x99101010;
    private static final int PANEL = 0x55000000;
    private static final int ENTRY = 0xDDA6B7C7;
    private static final int MAIN = 0xCC6EA46D;
    private static final int BASEMENT = 0xCC8A73A8;
    private static final int BASEMENT_ENTRY = 0xCC7060B8;
    private static final int BASEMENT_CAP = 0xCCB067A4;
    private static final int APPROACH = 0xDDBC985A;
    private static final int CAP = 0xCCD18A50;
    private static final int DISABLED = 0x775B6370;
    private static final int HOVER_OUTLINE = 0xFFFFFFFF;
    private static final int SELECTED_OUTLINE = 0xFFFFD166;
    private static final int GROUND_LINE = 0xFFE8E0C8;
    private static final int EXIT_MARKER = 0xFFFFD166;
    private static final int EXIT_MARKER_SHADOW = 0xAA000000;
    private static final int TEXT = 0xFFE0E0E0;
    private static final int MUTED_TEXT = 0xFFB8B8B8;
    private static final int CONTROL = 0xFF3A3F46;
    private static final int CONTROL_ACTIVE = 0xFF6B7682;
    private static final int TRACK = 0xFF4D5661;
    private static final int STACK_DIAGRAM_HEIGHT = 150;
    private static final int SMALL_HEIGHT_PREVIEW_BLOCKS = 64;
    private static final int MEDIUM_HEIGHT_PREVIEW_BLOCKS = 128;
    private static final int MAX_HEIGHT_PREVIEW_BLOCKS = 257;
    private static final int TOGGLE_START_OFFSET = 36;
    private static final int TOGGLE_ROW_HEIGHT = 16;
    private static final int TOGGLE_ROW_GAP = 2;
    private static final int GLOBAL_CONTROL_START_OFFSET = 94;
    private static final int REGION_CONTROL_TOP_OFFSET = 166;
    private static final int REGION_FIRST_CONTROL_OFFSET = 51;
    private static final int EXIT_MASK_SIZE = 86;
    private static final int EXIT_ROOM_SIZE = 22;
    private static final int FLOOR_TOPOLOGY_CONTROLS_HEIGHT = 264;
    private static final int EXIT_ARM_LENGTH = 18;
    private static final int EXIT_ARM_THICKNESS = 12;
    private static final int EXIT_VERTICAL_SIZE = 22;
    private static final int EXIT_ACTIVE = 0xFF9CA3AF;
    private static final int EXIT_INACTIVE = 0xFF4B5563;
    private static final int EXIT_REQUIRED = 0xFFB8860B;
    private static final int EXIT_SELECTED = 0xFF74C69D;
    private static final int EXIT_ROOM = 0xFF2A3440;
    private static final int SHAFT_FILL = 0xCC74C69D;
    private static final int SHAFT_BACKGROUND = 0xFF1B1B1F;
    private static final int FLOOR_PREVIEW_HEIGHT = 132;
    private static final int STRUCTURE_RADIUS_LIMIT = 128;
    private static final int FLOOR_ROOT = 0xCC6EA46D;
    private static final int FLOOR_MAIN = 0xCC4F8FB8;
    private static final int FLOOR_BRANCH = 0xCCB88A4F;
    private static final int FLOOR_ROOM = 0xCC8A73A8;
    private static final int FLOOR_CAP = 0xCCD18A50;
    private static final int COLLISION = 0xFFFF4D4D;

    private final MKTowerStackSizingReport report;
    private final String selectedKey;
    private final Consumer<String> selectionCallback;
    private final Controls controls;
    private String draggingSlider = "";

    public MKTowerStackSidePreview(int width, int height, MKTowerStackSizingReport report,
                                   String selectedKey, Consumer<String> selectionCallback, Controls controls) {
        super(0, 0, width, height);
        this.report = report;
        this.selectedKey = selectedKey;
        this.selectionCallback = selectionCallback;
        this.controls = controls;
    }

    @Override
    public void draw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height,
                     int mouseX, int mouseY, float partialTicks) {
        graphics.fill(x, y, x + width, y + height, BACKGROUND);
        StackBounds stackBounds = stackBounds(x, y, width, height);
        drawStack(graphics, stackBounds, mouseX, mouseY);
        drawShaftPreview(graphics, mc, x, y, width, height, stackBounds, mouseX, mouseY);
        drawControlPanel(graphics, mc, x, y, width, height, mouseX, mouseY, stackBounds);
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (handleTogglePress(mouseX, mouseY)) {
            return true;
        }
        if (handleGlobalControlPress(mouseX, mouseY, mouseButton)) {
            return true;
        }
        Optional<MKTowerStackSizingReport.SectionInfo> selected = selectedSection();
        if (selected.isPresent() && handleControlPress(selected.get(), mouseX, mouseY, mouseButton)) {
            return true;
        }
        return hoveredSection(getX(), getY(), getWidth(), getHeight(), (int) mouseX, (int) mouseY)
                .map(section -> {
                    selectionCallback.accept(section.key());
                    return true;
                })
                .orElse(false);
    }

    @Override
    public boolean onMouseDragged(Minecraft minecraft, double mouseX, double mouseY, int mouseButton,
                                  double dX, double dY) {
        if (draggingSlider.isBlank()) {
            return false;
        }
        selectedSection().ifPresent(section -> applySliderValue(section, draggingSlider, mouseX));
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
        Optional<ToggleSpec> toggleSpec = hoveredToggle(x, y, width, height, mouseX, mouseY);
        if (toggleSpec.isPresent()) {
            IMKScreen screen = getScreen();
            if (screen != null) {
                Vec2i parentPos = getParentCoords(new Vec2i(mouseX, mouseY));
                screen.addPostRenderInstruction(new HoveringTextInstruction(Component.literal(toggleSpec.get().tooltip()),
                        parentPos));
            }
            return;
        }
        Optional<String> groundTooltip = hoveredGroundLineTooltip(x, y, width, height, mouseX, mouseY);
        if (groundTooltip.isPresent()) {
            IMKScreen screen = getScreen();
            if (screen != null) {
                Vec2i parentPos = getParentCoords(new Vec2i(mouseX, mouseY));
                screen.addPostRenderInstruction(new HoveringTextInstruction(Component.literal(groundTooltip.get()),
                        parentPos));
            }
            return;
        }
        hoveredSection(x, y, width, height, mouseX, mouseY).ifPresent(section -> {
            IMKScreen screen = getScreen();
            if (screen != null) {
                Vec2i parentPos = getParentCoords(new Vec2i(mouseX, mouseY));
                screen.addPostRenderInstruction(new HoveringTextInstruction(Component.literal(tooltip(section)),
                        parentPos));
            }
        });
        Optional<String> exitTooltip = hoveredExitTooltip(x, y, width, height, mouseX, mouseY);
        if (exitTooltip.isPresent()) {
            IMKScreen screen = getScreen();
            if (screen != null) {
                Vec2i parentPos = getParentCoords(new Vec2i(mouseX, mouseY));
                screen.addPostRenderInstruction(new HoveringTextInstruction(Component.literal(exitTooltip.get()),
                        parentPos));
            }
        }
    }

    private Optional<String> hoveredFloorPlanTooltip(int x, int y, int width, int height, int mouseX, int mouseY) {
        Optional<MKTowerStackSizingReport.SectionInfo> sectionOpt = selectedSection()
                .filter(section -> controls.hasFloorTopology(section.key()));
        if (sectionOpt.isEmpty()) {
            return Optional.empty();
        }
        ButtonBounds panel = floorPlanBounds(x, y, width, height);
        if (!isInRect(mouseX, mouseY, panel.x(), panel.y(), panel.width(), panel.height())) {
            return Optional.empty();
        }
        return floorPlanSegments(panel, sectionOpt.get()).stream()
                .filter(segment -> isInRect(mouseX, mouseY, segment.bounds().x(), segment.bounds().y(),
                        segment.bounds().width(), segment.bounds().height()))
                .findFirst()
                .map(FloorPlanSegment::tooltip)
                .or(() -> Optional.of("Floor Plan\nTop-down generation preview for " +
                        WorkspaceTopologyUiSupport.formatTopologyLabel(sectionOpt.get().key())));
    }

    private boolean handleFloorPlanPress(double mouseX, double mouseY) {
        Optional<MKTowerStackSizingReport.SectionInfo> sectionOpt = selectedSection()
                .filter(section -> controls.hasFloorTopology(section.key()));
        if (sectionOpt.isEmpty()) {
            return false;
        }
        ButtonBounds panel = floorPlanBounds(getX(), getY(), getWidth(), getHeight());
        if (!isInRect(mouseX, mouseY, panel.x(), panel.y(), panel.width(), panel.height())) {
            return false;
        }
        Optional<FloorPlanSegment> segment = floorPlanSegments(panel, sectionOpt.get()).stream()
                .filter(candidate -> candidate.direction() != null)
                .filter(candidate -> isInRect(mouseX, mouseY, candidate.bounds().x(), candidate.bounds().y(),
                        candidate.bounds().width(), candidate.bounds().height()))
                .findFirst();
        segment.ifPresent(value -> controls.selectExit(sectionOpt.get().key(), value.direction()));
        return segment.isPresent();
    }

    private void drawStack(GuiGraphics graphics, StackBounds stackBounds, int mouseX, int mouseY) {
        int totalBlocks = Math.max(1, report.sections().stream().mapToInt(this::displayHeight).sum());
        int cursor = stackBounds.bottom();
        drawOutline(graphics, stackBounds.left(), stackBounds.top(), stackBounds.width(), stackBounds.bottom(),
                CONTROL_ACTIVE);
        Minecraft mc = Minecraft.getInstance();
        graphics.drawCenteredString(mc.font, Component.literal("Scale " + heightPreviewBlocks(totalBlocks)),
                stackBounds.left() + stackBounds.width() / 2, stackBounds.top() + 3, MUTED_TEXT);
        List<MKTowerStackSizingReport.SectionInfo> sections = report.sections();
        for (int index = 0; index < sections.size(); index++) {
            MKTowerStackSizingReport.SectionInfo section = sections.get(index);
            int sectionHeight = scaledSectionHeight(section, index, sections, stackBounds.top(),
                    stackBounds.bottom(), totalBlocks, cursor);
            cursor = drawSection(graphics, stackBounds.left(), stackBounds.width(), cursor, sectionHeight,
                    section.active() ? colorForKey(section.key()) : DISABLED, mouseX, mouseY, section);
        }
        int groundY = groundLineY(stackBounds);
        if (groundY >= stackBounds.top() && groundY <= stackBounds.bottom()) {
            drawGroundLine(graphics, stackBounds, groundY);
        }
    }

    private void drawGroundLine(GuiGraphics graphics, StackBounds stackBounds, int y) {
        int left = stackBounds.left() - 5;
        int right = stackBounds.left() + stackBounds.width() + 5;
        graphics.fill(left, y - 1, right, y + 1, 0xAA000000);
        graphics.fill(left, y, right, y + 1, GROUND_LINE);
    }

    private void drawShaftPreview(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height,
                                  StackBounds stackBounds, int mouseX, int mouseY) {
        int maxWidth = stackBounds.maxWidth();
        int previewTop = stackBounds.bottom() + 18;
        int previewBottom = Math.min(previewTop + maxWidth + mc.font.lineHeight + 4,
                y + height - 6);
        if (previewBottom - previewTop < 32) {
            return;
        }
        graphics.drawCenteredString(mc.font, Component.literal("Shaft"),
                x + 14 + (maxWidth / 2), previewTop, TEXT);
        int boxTop = previewTop + mc.font.lineHeight + 4;
        int boxHeight = previewBottom - boxTop;
        int stackWidth = Math.max(1, controls.stackWidth());
        int stackLength = Math.max(1, controls.stackLength());
        float scale = Math.min(maxWidth / (float) stackWidth, boxHeight / (float) stackLength);
        int boxWidth = Math.max(1, Math.round(stackWidth * scale));
        int footprintHeight = Math.max(1, Math.round(stackLength * scale));
        int boxLeft = x + 14 + ((maxWidth - boxWidth) / 2);
        int footprintTop = boxTop + ((boxHeight - footprintHeight) / 2);
        graphics.fill(boxLeft, footprintTop, boxLeft + boxWidth, footprintTop + footprintHeight, SHAFT_BACKGROUND);
        drawOutline(graphics, boxLeft, footprintTop, boxWidth, footprintTop + footprintHeight, CONTROL_ACTIVE);

        int shaftSize = Math.max(1, Math.min(Math.min(stackWidth, stackLength), controls.shaftSize()));
        float shaftCenterX = shaftCenterX(stackWidth, shaftSize, controls.shaftPlacement());
        float shaftCenterZ = shaftCenterZ(stackLength, shaftSize, controls.shaftPlacement());
        int shaftWidth = Math.max(2, Math.round(shaftSize * scale));
        int shaftHeight = Math.max(2, Math.round(shaftSize * scale));
        int shaftLeft = boxLeft + Math.round((shaftCenterX * scale) - (shaftWidth / 2.0f));
        int shaftTop = footprintTop + Math.round((shaftCenterZ * scale) - (shaftHeight / 2.0f));
        graphics.fill(shaftLeft, shaftTop, shaftLeft + shaftWidth, shaftTop + shaftHeight, SHAFT_FILL);
        drawOutline(graphics, shaftLeft, shaftTop, shaftWidth, shaftTop + shaftHeight, SELECTED_OUTLINE);
    }

    private float shaftCenterX(int stackWidth, int shaftSize, MKVerticalAccessPlacement placement) {
        float halfShaft = shaftSize / 2.0f;
        return switch (placement) {
            case WEST -> halfShaft;
            case EAST -> stackWidth - halfShaft;
            default -> stackWidth / 2.0f;
        };
    }

    private float shaftCenterZ(int stackLength, int shaftSize, MKVerticalAccessPlacement placement) {
        float halfShaft = shaftSize / 2.0f;
        return switch (placement) {
            case NORTH -> halfShaft;
            case SOUTH -> stackLength - halfShaft;
            default -> stackLength / 2.0f;
        };
    }

    private void drawToggleControls(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height,
                                    int mouseX, int mouseY) {
        for (int index = 0; index < toggleSpecs().size(); index++) {
            ToggleSpec spec = toggleSpecs().get(index);
            ToggleBounds bounds = toggleBounds(x, y, width, height, index);
            boolean hovered = isInToggle(mouseX, mouseY, bounds);
            graphics.fill(bounds.cellX(), bounds.cellY(), bounds.cellX() + bounds.cellWidth(),
                    bounds.cellY() + TOGGLE_ROW_HEIGHT, hovered ? 0x443A3F46 : 0x22000000);
            graphics.fill(bounds.boxX(), bounds.boxY(), bounds.boxX() + 10, bounds.boxY() + 10,
                    hovered ? CONTROL_ACTIVE : CONTROL);
            if (spec.enabled()) {
                graphics.drawString(mc.font, "x", bounds.boxX() + 2, bounds.boxY() + 1, SELECTED_OUTLINE, false);
            }
            graphics.drawString(mc.font, spec.label(), bounds.labelX(), bounds.labelY(),
                    spec.enabled() ? TEXT : MUTED_TEXT, false);
        }
    }

    private void drawControlPanel(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height,
                                  int mouseX, int mouseY, StackBounds stackBounds) {
        int controlX = x + 14 + stackBounds.maxWidth() + 12;
        int controlRight = x + width - 8;
        int controlWidth = Math.max(80, controlRight - controlX);
        graphics.fill(controlX - 5, y + 7, controlRight, y + height - 8, PANEL);
        graphics.drawString(mc.font, report.stackId(), controlX, y + 10, TEXT, false);
        graphics.drawString(mc.font, report.width() + "x" + report.length() + "  span " +
                report.maxVerticalSpan(), controlX, y + 22, MUTED_TEXT, false);

        MKTowerStackSizingReport.SectionInfo section = selectedSection().orElseGet(() -> report.sections().getFirst());
        drawToggleControls(graphics, mc, x, y, width, height, mouseX, mouseY);
        int globalY = y + GLOBAL_CONTROL_START_OFFSET;
        drawSlider(graphics, mc, "Width", controls.stackWidth(), 3, MAX_STACK_FOOTPRINT,
                controlX, globalY, controlWidth, mouseX, mouseY, "stackWidth");
        drawSlider(graphics, mc, "Length", controls.stackLength(), 3, MAX_STACK_FOOTPRINT,
                controlX, globalY + 22, controlWidth, mouseX, mouseY, "stackLength");
        drawSlider(graphics, mc, "Shaft", controls.shaftSize(), controls.shaftSizeMin(), controls.shaftSizeMax(),
                controlX, globalY + 44, controlWidth, mouseX, mouseY, "shaftSize");
        drawPlacementButton(graphics, mc, controlX, globalY + 66, controlWidth, mouseX, mouseY);

        ControlLayout regionLayout = new ControlLayout(controlX, y + REGION_CONTROL_TOP_OFFSET, controlWidth);
        graphics.drawString(mc.font, WorkspaceTopologyUiSupport.formatTopologyLabel(section.key()),
                controlX, regionLayout.controlY() + 37, colorForKey(section.key()), false);
        int cursorY = regionLayout.controlY() + REGION_FIRST_CONTROL_OFFSET;
        drawSlider(graphics, mc, "Height", controls.height(section.key()), controls.heightMin(), controls.heightMax(),
                controlX, cursorY, controlWidth, mouseX, mouseY, "height");
        cursorY += 22;
        if (controls.hasFloorCounts(section.key())) {
            drawCounter(graphics, mc, "Max", controls.maxFloors(section.key()), controlX, cursorY, mouseX, mouseY,
                    "max");
            cursorY += 18;
            if (controls.maxFloors(section.key()) > 0) {
                drawCounter(graphics, mc, "Min", controls.minFloors(section.key()), controlX, cursorY, mouseX,
                        mouseY, "min");
                cursorY += 18;
            }
        }
        if (controls.hasMargin(section.key())) {
            drawSlider(graphics, mc, controls.marginLabel(section.key()), controls.margin(section.key()), 0,
                    controls.marginMax(section.key()), controlX, cursorY, controlWidth, mouseX, mouseY, "margin");
            cursorY += 22;
        }
        drawExitEditor(graphics, mc, section, regionLayout, mouseX, mouseY);
    }

    private void drawFloorTopologyControls(GuiGraphics graphics, Minecraft mc,
                                           MKTowerStackSizingReport.SectionInfo section,
                                           int x, int y, int width, int mouseX, int mouseY) {
        graphics.drawString(mc.font, "Floor Paths", x, y + 3, TEXT, false);
        drawSlider(graphics, mc, "Min Main", controls.floorMinMainPathPieces(section.key()), 0, 10,
                x, y + 14, width, mouseX, mouseY, "floorMinMain");
        drawSlider(graphics, mc, "Max Main", controls.floorMaxMainPathPieces(section.key()), 0, 10,
                x, y + 36, width, mouseX, mouseY, "floorMaxMain");
        drawSlider(graphics, mc, "Branches", controls.floorMaxBranchPiecesBeforeCap(section.key()), 0,
                MKWorkspaceFloorTopologySettings.MAX_BRANCH_PIECES_BEFORE_CAP,
                x, y + 58, width, mouseX, mouseY, "floorBranchCap");
        ButtonBounds modeBounds = new ButtonBounds(x, y + 80, Math.min(92, width), 16);
        boolean hovered = isInRect(mouseX, mouseY, modeBounds.x(), modeBounds.y(), modeBounds.width(),
                modeBounds.height());
        graphics.fill(modeBounds.x(), modeBounds.y(), modeBounds.x() + modeBounds.width(),
                modeBounds.y() + modeBounds.height(), hovered ? CONTROL_ACTIVE : CONTROL);
        graphics.drawString(mc.font, fit("Lead " +
                        WorkspaceTopologyUiSupport.formatTopologyLabel(
                                controls.floorHallwayLeadInMode(section.key()).getSerializedName()),
                modeBounds.width() - 4), modeBounds.x() + 2, modeBounds.y() + 3, TEXT, false);
        if (controls.floorHallwayLeadInMode(section.key()) == MKWorkspaceHallwayLeadInMode.MANUAL) {
            drawSlider(graphics, mc, "Lead In", controls.floorManualHallwayLeadInPieces(section.key()), 0,
                    MKWorkspaceFloorTopologySettings.MAX_MANUAL_HALLWAY_LEAD_IN_PIECES,
                    x + modeBounds.width() + 6, y + 80,
                    Math.max(40, width - modeBounds.width() - 6), mouseX, mouseY, "floorLeadIn");
        } else {
            graphics.drawString(mc.font, "auto " + controls.recommendedHallwayLeadInPieces(section.key()),
                    x + modeBounds.width() + 8, y + 83, MUTED_TEXT, false);
        }
        graphics.drawString(mc.font, "Main Room", x, y + 104, FLOOR_MAIN, false);
        drawSlider(graphics, mc, "W", controls.floorRoomWidth(section.key(), MKWorkspaceFloorRoomKind.MAIN_ROOM),
                3, MAX_STACK_FOOTPRINT, x, y + 116, width, mouseX, mouseY, "mainRoomWidth");
        drawSlider(graphics, mc, "L", controls.floorRoomLength(section.key(), MKWorkspaceFloorRoomKind.MAIN_ROOM),
                3, MAX_STACK_FOOTPRINT, x, y + 138, width, mouseX, mouseY, "mainRoomLength");
        drawSlider(graphics, mc, "H", controls.floorRoomHeight(section.key(), MKWorkspaceFloorRoomKind.MAIN_ROOM),
                controls.floorRoomHeightMin(section.key()), controls.floorRoomHeightMax(section.key()),
                x, y + 160, width, mouseX, mouseY, "mainRoomHeight");
        graphics.drawString(mc.font, "Branch Room", x, y + 184, FLOOR_BRANCH, false);
        drawSlider(graphics, mc, "W", controls.floorRoomWidth(section.key(), MKWorkspaceFloorRoomKind.BRANCH_ROOM),
                3, MAX_STACK_FOOTPRINT, x, y + 196, width, mouseX, mouseY, "branchRoomWidth");
        drawSlider(graphics, mc, "L", controls.floorRoomLength(section.key(), MKWorkspaceFloorRoomKind.BRANCH_ROOM),
                3, MAX_STACK_FOOTPRINT, x, y + 218, width, mouseX, mouseY, "branchRoomLength");
        drawSlider(graphics, mc, "H", controls.floorRoomHeight(section.key(), MKWorkspaceFloorRoomKind.BRANCH_ROOM),
                controls.floorRoomHeightMin(section.key()), controls.floorRoomHeightMax(section.key()),
                x, y + 240, width, mouseX, mouseY, "branchRoomHeight");
    }

    private void drawFloorTopologyPreview(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height,
                                          MKTowerStackSizingReport.SectionInfo section, int mouseX, int mouseY) {
        ButtonBounds panel = floorPlanBounds(x, y, width, height);
        int panelX = panel.x();
        int panelY = panel.y();
        int panelWidth = panel.width();
        int panelHeight = panel.height();
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, PANEL);
        graphics.drawString(mc.font, "Floor Plan", panelX + 5, panelY + 5, TEXT, false);
        int centerX = panelX + panelWidth / 2;
        int centerY = panelY + panelHeight / 2;
        float scale = floorPlanScale(panel);
        int rootW = Math.max(2, Math.round(controls.stackWidth(section.key()) * scale));
        int rootH = Math.max(2, Math.round(controls.stackLength(section.key()) * scale));

        List<FloorPlanSegment> segments = floorPlanSegments(panel, section);
        List<FloorPlanSegment> colliding = collidingFloorPlanSegments(section, segments);
        for (FloorPlanSegment segment : segments) {
            ButtonBounds bounds = segment.bounds();
            boolean hovered = isInRect(mouseX, mouseY, bounds.x(), bounds.y(), bounds.width(), bounds.height());
            graphics.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(),
                    hovered ? brighten(segment.color()) : segment.color());
            if (hovered) {
                drawOutline(graphics, bounds.x(), bounds.y(), bounds.width(), bounds.y() + bounds.height(),
                        HOVER_OUTLINE);
            }
            if (colliding.contains(segment)) {
                drawDiagonalHatch(graphics, bounds, COLLISION);
            }
        }
        drawOutline(graphics, centerX - rootW / 2, centerY - rootH / 2, rootW, centerY + rootH / 2,
                SELECTED_OUTLINE);
        for (MKWorkspaceFamilyHorizontalExitDefinition exit : controls.exits(section.key())) {
            if (exit.direction().getAxis().isHorizontal()) {
                drawFloorConnectorMarker(graphics, centerX, centerY, rootW, rootH, exit);
            }
        }
        if (segments.size() <= 1) {
            graphics.drawCenteredString(mc.font, Component.literal("No horizontal floor paths"),
                    centerX, centerY + rootH / 2 + 8, MUTED_TEXT);
        }
        graphics.drawString(mc.font, "main rooms " + controls.floorMinMainPathPieces(section.key()) + "-" +
                        controls.floorMaxMainPathPieces(section.key()) + "  branch rooms max " +
                        controls.floorMaxBranchPiecesBeforeCap(section.key()) + "  lead " +
                        controls.effectiveHallwayLeadInPieces(section.key()),
                panelX + 5, panelY + panelHeight - 13, MUTED_TEXT, false);
    }

    private List<FloorPlanSegment> floorPlanSegments(ButtonBounds panel, MKTowerStackSizingReport.SectionInfo section) {
        ArrayList<FloorPlanSegment> segments = new ArrayList<>();
        int centerX = panel.x() + panel.width() / 2;
        int centerY = panel.y() + panel.height() / 2;
        float scale = floorPlanScale(panel);
        int rootW = Math.max(2, Math.round(controls.stackWidth(section.key()) * scale));
        int rootH = Math.max(2, Math.round(controls.stackLength(section.key()) * scale));
        ButtonBounds root = new ButtonBounds(centerX - rootW / 2, centerY - rootH / 2, rootW, rootH);
        segments.add(new FloorPlanSegment(root, FLOOR_ROOT, null, "Floor Root",
                "Floor Root\n" + controls.stackWidth(section.key()) + " x " +
                        controls.stackLength(section.key()) + "\n" +
                        WorkspaceTopologyUiSupport.formatTopologyLabel(section.key())));
        for (MKWorkspaceFamilyHorizontalExitDefinition exit : controls.exits(section.key())) {
            if (!exit.direction().getAxis().isHorizontal() ||
                    (exit.pathKind() != MKWorkspaceHorizontalExitPathKind.MAIN_EXIT &&
                            exit.pathKind() != MKWorkspaceHorizontalExitPathKind.BRANCH)) {
                continue;
            }
            addExitPlanSegments(segments, panel, root, section, exit, scale);
        }
        return List.copyOf(segments);
    }

    private void addExitPlanSegments(List<FloorPlanSegment> segments, ButtonBounds panel, ButtonBounds root,
                                     MKTowerStackSizingReport.SectionInfo section,
                                     MKWorkspaceFamilyHorizontalExitDefinition exit, float scale) {
        boolean main = exit.pathKind().usesMainPath();
        int roomCount = main ? controls.floorMaxMainPathPieces(section.key()) :
                controls.floorMaxBranchPiecesBeforeCap(section.key());
        int leadIn = controls.effectiveHallwayLeadInPieces(section.key());
        boolean vertical = exit.direction() == Direction.NORTH || exit.direction() == Direction.SOUTH;
        MKWorkspaceFloorRoomKind roomKind = main ? MKWorkspaceFloorRoomKind.MAIN_ROOM :
                MKWorkspaceFloorRoomKind.BRANCH_ROOM;
        int configuredWidth = controls.floorRoomWidth(section.key(), roomKind);
        int configuredLength = controls.floorRoomLength(section.key(), roomKind);
        int roomMajor = vertical ? configuredLength : configuredWidth;
        int roomMinor = vertical ? configuredWidth : configuredLength;
        int hallwayMinor = main ? Math.max(3, Math.min(7, configuredWidth / 2)) :
                Math.max(3, Math.min(5, configuredWidth / 2));
        int capMajor = roomMajor;
        int capMinor = roomMinor;
        boolean hallwaysEnabled = main ? controls.floorMainHallwaysEnabled(section.key()) :
                controls.floorBranchHallwaysEnabled(section.key());
        boolean mainCapApproachEnabled = main && controls.floorMainCapApproachEnabled(section.key());
        List<FloorPlanStep> steps = floorPlanSteps(main, Math.max(0, roomCount), leadIn, roomMajor, roomMinor,
                hallwayMinor, capMajor, capMinor, configuredWidth, configuredLength,
                controls.floorRoomHeight(section.key(), roomKind), exit, hallwaysEnabled, mainCapApproachEnabled);
        ButtonBounds previous = root;
        for (FloorPlanStep step : steps) {
            int major = Math.max(2, Math.round(step.major() * scale));
            int minor = Math.max(2, Math.round(step.minor() * scale));
            previous = orientedRectAfter(previous, exit.direction(), major, minor);
            if (previous.width() <= 0 || previous.height() <= 0) {
                continue;
            }
            clippedRect(previous, panel).ifPresent(bounds ->
                    segments.add(new FloorPlanSegment(bounds, step.color(), exit.direction(), step.label(),
                            step.tooltip())));
        }
    }

    private List<FloorPlanStep> floorPlanSteps(boolean main, int roomCount, int leadIn, int roomMajor, int roomMinor,
                                               int hallwayMinor, int capMajor, int capMinor,
                                               int configuredWidth, int configuredLength, int configuredHeight,
                                               MKWorkspaceFamilyHorizontalExitDefinition exit,
                                               boolean hallwaysEnabled,
                                               boolean mainCapApproachEnabled) {
        ArrayList<FloorPlanStep> steps = new ArrayList<>();
        String pathLabel = main ? "Main Hall" : "Branch Hall";
        int pathColor = main ? FLOOR_MAIN : FLOOR_BRANCH;
        int firstHallway = Math.max(5, Math.min(18, Math.max(1, leadIn) * 4));
        int betweenHallway = Math.max(1, leadIn);
        firstHallway = Math.max(1, leadIn);
        for (int i = 0; i < roomCount; i++) {
            if (hallwaysEnabled) {
                steps.add(new FloorPlanStep(pathLabel, pathColor, i == 0 ? firstHallway : betweenHallway,
                        hallwayMinor,
                        pathLabel + "\n" + formatDirection(exit.direction()) +
                                (i == 0 ? "\nlead-in " + leadIn : "\nbefore room " + (i + 1)) +
                                "\nconnector " + formatPathKind(exit.pathKind())));
            }
            int roomNumber = i + 1;
            steps.add(new FloorPlanStep(main ? "M" + roomNumber : "B" + roomNumber, FLOOR_ROOM,
                    roomMajor, roomMinor,
                    (main ? "Main Room" : "Branch Room") +
                            "\n" + formatDirection(exit.direction()) +
                            "\nconfigured " + configuredWidth + " x " + configuredLength +
                            "\nheight " + configuredHeight +
                            "\nroom " + roomNumber + " of max " + roomCount));
        }
        if (mainCapApproachEnabled) {
            if (hallwaysEnabled) {
                steps.add(new FloorPlanStep(pathLabel, pathColor, roomCount == 0 ? firstHallway : betweenHallway,
                        hallwayMinor,
                        pathLabel + "\n" + formatDirection(exit.direction()) +
                                "\nbefore main approach"));
            }
            steps.add(new FloorPlanStep("Approach", FLOOR_CAP, capMajor, capMinor,
                    "Main cap approach\n" + formatDirection(exit.direction())));
        }
        if (hallwaysEnabled) {
            steps.add(new FloorPlanStep(pathLabel, pathColor,
                    roomCount == 0 && !mainCapApproachEnabled ? firstHallway : betweenHallway,
                    hallwayMinor,
                    pathLabel + "\n" + formatDirection(exit.direction()) +
                            (mainCapApproachEnabled ? "\nbefore cap" : "\nbefore terminal cap")));
        }
        steps.add(new FloorPlanStep(main ? "Main Cap" : "Branch Cap", FLOOR_CAP, capMajor, capMinor,
                (main ? "Main approach/cap" : "Branch cap") +
                        "\n" + formatDirection(exit.direction()) +
                        "\nterminal content\nplaced after max " + roomCount +
                        (main ? " main rooms" : " branch rooms")));
        return List.copyOf(steps);
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

    private float floorPlanScale(ButtonBounds panel) {
        int plotW = Math.max(1, panel.width() - 12);
        int plotH = Math.max(1, panel.height() - 22);
        return Math.min(plotW, plotH) / (float) (STRUCTURE_RADIUS_LIMIT * 2);
    }

    private Optional<ButtonBounds> clippedRect(ButtonBounds bounds, ButtonBounds panel) {
        int left = Math.max(panel.x() + 6, bounds.x());
        int top = Math.max(panel.y() + 6, bounds.y());
        int right = Math.min(panel.x() + panel.width() - 6, bounds.x() + bounds.width());
        int bottom = Math.min(panel.y() + panel.height() - 16, bounds.y() + bounds.height());
        if (right <= left || bottom <= top) {
            return Optional.empty();
        }
        return Optional.of(new ButtonBounds(left, top, right - left, bottom - top));
    }

    private List<FloorPlanSegment> collidingFloorPlanSegments(MKTowerStackSizingReport.SectionInfo section,
                                                              List<FloorPlanSegment> segments) {
        List<FloorPlanSegment> rooms = segments.stream()
                .filter(segment -> segment.color() == FLOOR_ROOM || segment.color() == FLOOR_CAP)
                .toList();
        ArrayList<FloorPlanSegment> colliding = new ArrayList<>();
        for (int i = 0; i < rooms.size(); i++) {
            for (int j = i + 1; j < rooms.size(); j++) {
                if (intersects(rooms.get(i).bounds(), rooms.get(j).bounds())) {
                    if (!colliding.contains(rooms.get(i))) {
                        colliding.add(rooms.get(i));
                    }
                    if (!colliding.contains(rooms.get(j))) {
                        colliding.add(rooms.get(j));
                    }
                }
            }
        }
        return List.copyOf(colliding);
    }

    private List<Integer> scaleMajors(List<Integer> majors, int maxSpan) {
        int gapTotal = majors.size() * 2;
        int available = Math.max(majors.size(), maxSpan - gapTotal);
        int desired = majors.stream().mapToInt(Integer::intValue).sum();
        if (desired <= available) {
            return majors;
        }
        float scale = available / (float) Math.max(1, desired);
        int minMajor = Math.max(1, Math.min(3, available / Math.max(1, majors.size())));
        ArrayList<Integer> scaled = new ArrayList<>();
        for (int major : majors) {
            scaled.add(Math.max(minMajor, Math.round(major * scale)));
        }
        while (scaled.stream().mapToInt(Integer::intValue).sum() > available) {
            int largestIndex = 0;
            for (int i = 1; i < scaled.size(); i++) {
                if (scaled.get(i) > scaled.get(largestIndex)) {
                    largestIndex = i;
                }
            }
            if (scaled.get(largestIndex) <= minMajor) {
                break;
            }
            scaled.set(largestIndex, scaled.get(largestIndex) - 1);
        }
        return List.copyOf(scaled);
    }

    private ButtonBounds orientedRectAfter(ButtonBounds previous, Direction direction, int major, int minor) {
        return switch (direction) {
            case EAST -> terminalRect(previous, direction, major, minor);
            case WEST -> terminalRect(previous, direction, major, minor);
            case SOUTH -> terminalRect(previous, direction, minor, major);
            case NORTH -> terminalRect(previous, direction, minor, major);
            default -> new ButtonBounds(previous.x(), previous.y(), 1, 1);
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

    private void drawFloorConnectorMarker(GuiGraphics graphics, int centerX, int centerY, int rootW, int rootH,
                                          MKWorkspaceFamilyHorizontalExitDefinition exit) {
        int markerX = centerX;
        int markerY = centerY;
        switch (exit.direction()) {
            case EAST -> markerX += rootW / 2;
            case WEST -> markerX -= rootW / 2;
            case SOUTH -> markerY += rootH / 2;
            case NORTH -> markerY -= rootH / 2;
            default -> {
            }
        }
        int color = exit.pathKind().usesMainPath() ? FLOOR_MAIN : FLOOR_BRANCH;
        drawConnectorMarker(graphics, markerX, markerY, color);
    }

    private int brighten(int color) {
        return (color & 0xFF000000) | Math.min(0xFF, ((color >> 16) & 0xFF) + 30) << 16 |
                Math.min(0xFF, ((color >> 8) & 0xFF) + 30) << 8 |
                Math.min(0xFF, (color & 0xFF) + 30);
    }

    private ButtonBounds floorPlanBounds(int x, int y, int width, int height) {
        return new ButtonBounds(x + 8, y + height - FLOOR_PREVIEW_HEIGHT - 8,
                width - 16, FLOOR_PREVIEW_HEIGHT);
    }

    private void drawConnectorMarker(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x - 2, y - 2, x + 3, y + 3, EXIT_MARKER_SHADOW);
        graphics.fill(x - 1, y - 1, x + 2, y + 2, color);
    }

    private void drawPlacementButton(GuiGraphics graphics, Minecraft mc, int x, int y, int width,
                                     int mouseX, int mouseY) {
        ButtonBounds bounds = new ButtonBounds(x, y, width, 16);
        boolean hovered = isInRect(mouseX, mouseY, bounds.x(), bounds.y(), bounds.width(), bounds.height());
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(),
                hovered ? CONTROL_ACTIVE : CONTROL);
        graphics.drawString(mc.font, fit("Location " +
                        WorkspaceTopologyUiSupport.formatTopologyLabel(controls.shaftPlacement().getSerializedName()),
                bounds.width() - 4), bounds.x() + 2, bounds.y() + 3, TEXT, false);
    }

    private void drawExitEditor(GuiGraphics graphics, Minecraft mc, MKTowerStackSizingReport.SectionInfo section,
                                ControlLayout layout, int mouseX, int mouseY) {
        ExitLayout exitLayout = exitLayout(section, layout);
        graphics.drawString(mc.font, "Exits", layout.controlX(), exitLayout.maskY() - 11, TEXT, false);
        drawExitMask(graphics, mc, section, exitLayout.maskX(), exitLayout.maskY(), mouseX, mouseY);
        Optional<MKWorkspaceFamilyHorizontalExitDefinition> selectedExit = controls.selectedExit(section.key());
        if (selectedExit.isEmpty()) {
            graphics.drawString(mc.font, "Left: select", exitLayout.editorX(), exitLayout.maskY() + 4, MUTED_TEXT,
                    false);
            graphics.drawString(mc.font, "Right: branch", exitLayout.editorX(), exitLayout.maskY() + 16, MUTED_TEXT,
                    false);
            return;
        }
        MKWorkspaceFamilyHorizontalExitDefinition exit = selectedExit.get();
        boolean required = controls.exitRequired(section.key(), exit.direction());
        boolean geometryEditable = canEditRequiredExitGeometry(exit);
        int y = exitLayout.editorY();
        graphics.drawString(mc.font, required && geometryEditable ? "Required Geometry" :
                        required ? "Required" : "Editable", exitLayout.editorX(), y - 12,
                required ? EXIT_REQUIRED : EXIT_SELECTED, false);
        if (exit.isVerticalAccess()) {
            graphics.drawString(mc.font, formatDirection(exit.direction()), exitLayout.editorX(), y + 5, TEXT, false);
            graphics.drawString(mc.font, "Vertical access", exitLayout.editorX(), y + 17, MUTED_TEXT, false);
            return;
        }
        drawEditorButton(graphics, mc, "Dir " + formatDirection(exit.direction()),
                editorButtonBounds(exitLayout.editorX(), y, "direction"), mouseX, mouseY, required);
        drawEditorButton(graphics, mc, "Role " + formatPathKind(exit.pathKind()),
                editorButtonBounds(exitLayout.editorX(), y, "role"), mouseX, mouseY, required);
        drawEditorButton(graphics, mc, "Conn " + formatConnection(exit.connectionMode().getSerializedName()),
                editorButtonBounds(exitLayout.editorX(), y, "connection"), mouseX, mouseY, required);
        drawEditorButton(graphics, mc, "Open " + exit.openingProfileId(),
                editorButtonBounds(exitLayout.editorX(), y, "profile"), mouseX, mouseY,
                required && !geometryEditable);
        drawSlider(graphics, mc, "Side Offset", exit.sideOffset(), controls.selectedExitSideMin(section.key()),
                controls.selectedExitSideMax(section.key()), exitLayout.sliderX(), exitLayout.sliderY(),
                exitLayout.sliderWidth(),
                required && !geometryEditable ? -1 : mouseX,
                required && !geometryEditable ? -1 : mouseY, "exitSide");
        drawSlider(graphics, mc, "Vertical Offset", exit.verticalOffset(), 0,
                controls.selectedExitVerticalMax(section.key()), exitLayout.sliderX(), exitLayout.sliderY() + 22,
                exitLayout.sliderWidth(), required && !geometryEditable ? -1 : mouseX,
                required && !geometryEditable ? -1 : mouseY, "exitVertical");
    }

    private void drawEditorButton(GuiGraphics graphics, Minecraft mc, String label, ButtonBounds bounds,
                                  int mouseX, int mouseY, boolean disabled) {
        boolean hovered = !disabled && isInRect(mouseX, mouseY, bounds.x(), bounds.y(), bounds.width(), bounds.height());
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(),
                disabled ? EXIT_INACTIVE : hovered ? CONTROL_ACTIVE : CONTROL);
        graphics.drawString(mc.font, fit(label, bounds.width() - 4), bounds.x() + 2, bounds.y() + 4,
                disabled ? MUTED_TEXT : TEXT, false);
    }

    private void drawExitMask(GuiGraphics graphics, Minecraft mc, MKTowerStackSizingReport.SectionInfo section,
                              int x, int y, int mouseX, int mouseY) {
        graphics.fill(x, y, x + EXIT_MASK_SIZE, y + EXIT_MASK_SIZE, 0xFF1B1B1F);
        drawOutline(graphics, x, y, EXIT_MASK_SIZE, y + EXIT_MASK_SIZE, CONTROL_ACTIVE);
        int centerX = x + (EXIT_MASK_SIZE / 2);
        int centerY = y + (EXIT_MASK_SIZE / 2);
        int roomLeft = centerX - (EXIT_ROOM_SIZE / 2);
        int roomTop = centerY - (EXIT_ROOM_SIZE / 2);
        int roomRight = roomLeft + EXIT_ROOM_SIZE;
        int roomBottom = roomTop + EXIT_ROOM_SIZE;
        drawExitArm(graphics, section, Direction.NORTH, roomLeft, roomTop, roomRight, roomBottom, centerX, centerY,
                mouseX, mouseY);
        drawExitArm(graphics, section, Direction.EAST, roomLeft, roomTop, roomRight, roomBottom, centerX, centerY,
                mouseX, mouseY);
        drawExitArm(graphics, section, Direction.SOUTH, roomLeft, roomTop, roomRight, roomBottom, centerX, centerY,
                mouseX, mouseY);
        drawExitArm(graphics, section, Direction.WEST, roomLeft, roomTop, roomRight, roomBottom, centerX, centerY,
                mouseX, mouseY);
        drawVerticalExitButton(graphics, mc, section, Direction.UP, x, y, x + 6, y + 6, mouseX, mouseY);
        drawVerticalExitButton(graphics, mc, section, Direction.DOWN, x, y,
                x + EXIT_MASK_SIZE - 6 - EXIT_VERTICAL_SIZE, y + 6, mouseX, mouseY);
        graphics.fill(roomLeft, roomTop, roomRight, roomBottom, EXIT_ROOM);
        drawOutline(graphics, roomLeft, roomTop, EXIT_ROOM_SIZE, roomBottom, CONTROL_ACTIVE);
        graphics.drawCenteredString(mc.font, Component.literal("R"), centerX, centerY - 4, TEXT);
        drawExitLabel(graphics, mc, section, Direction.NORTH, centerX, roomTop - EXIT_ARM_LENGTH - 10);
        drawExitLabel(graphics, mc, section, Direction.EAST, roomRight + EXIT_ARM_LENGTH, centerY - 4);
        drawExitLabel(graphics, mc, section, Direction.SOUTH, centerX, roomBottom + EXIT_ARM_LENGTH + 1);
        drawExitLabel(graphics, mc, section, Direction.WEST, roomLeft - EXIT_ARM_LENGTH, centerY - 4);
    }

    private void drawSlider(GuiGraphics graphics, Minecraft mc, String label, int value, int min, int max,
                            int x, int y, int width, int mouseX, int mouseY, String id) {
        graphics.drawString(mc.font, label + " " + value, x, y, TEXT, false);
        SliderBounds bounds = sliderBounds(x, y, width, id);
        boolean hovered = mouseX >= bounds.trackX() && mouseX <= bounds.trackX() + bounds.trackWidth() &&
                mouseY >= bounds.trackY() - 3 && mouseY <= bounds.trackY() + 6;
        graphics.fill(bounds.trackX(), bounds.trackY(), bounds.trackX() + bounds.trackWidth(),
                bounds.trackY() + 3, hovered ? CONTROL_ACTIVE : TRACK);
        int knobX = bounds.trackX() + Math.round(((value - min) / (float) Math.max(1, max - min)) *
                bounds.trackWidth());
        graphics.fill(knobX - 2, bounds.trackY() - 3, knobX + 3, bounds.trackY() + 7, SELECTED_OUTLINE);
    }

    private void drawCounter(GuiGraphics graphics, Minecraft mc, String label, int value, int x, int y,
                             int mouseX, int mouseY, String id) {
        graphics.drawString(mc.font, label, x, y + 3, TEXT, false);
        CounterBounds bounds = counterBounds(x, y, id);
        drawCounterButton(graphics, mc, bounds.minusX(), bounds.buttonY(), "-", mouseX, mouseY);
        graphics.drawString(mc.font, Integer.toString(value), bounds.valueX(), y + 3, TEXT, false);
        drawCounterButton(graphics, mc, bounds.plusX(), bounds.buttonY(), "+", mouseX, mouseY);
    }

    private void drawCounterButton(GuiGraphics graphics, Minecraft mc, int x, int y, String label,
                                   int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + 13 && mouseY >= y && mouseY <= y + 13;
        graphics.fill(x, y, x + 13, y + 13, hovered ? CONTROL_ACTIVE : CONTROL);
        graphics.drawString(mc.font, label, x + 4, y + 2, TEXT, false);
    }

    private boolean handleControlPress(MKTowerStackSizingReport.SectionInfo section, double mouseX, double mouseY,
                                       int mouseButton) {
        ControlLayout layout = controlLayout(getX(), getY(), getWidth(), getHeight());
        int cursorY = layout.controlY() + REGION_FIRST_CONTROL_OFFSET;
        if (isInSlider(mouseX, mouseY, sliderBounds(layout.controlX(), cursorY, layout.controlWidth(), "height"))) {
            draggingSlider = "height";
            applySliderValue(section, "height", mouseX);
            return true;
        }
        cursorY += 22;
        if (controls.hasFloorCounts(section.key())) {
            CounterBounds maxBounds = counterBounds(layout.controlX(), cursorY, "max");
            if (handleCounterPress(section, maxBounds, mouseX, mouseY, "max")) {
                return true;
            }
            cursorY += 18;
            if (controls.maxFloors(section.key()) > 0) {
                CounterBounds minBounds = counterBounds(layout.controlX(), cursorY, "min");
                if (handleCounterPress(section, minBounds, mouseX, mouseY, "min")) {
                    return true;
                }
                cursorY += 18;
            }
        }
        if (controls.hasMargin(section.key())) {
            SliderBounds marginBounds = sliderBounds(layout.controlX(), cursorY, layout.controlWidth(), "margin");
            if (isInSlider(mouseX, mouseY, marginBounds)) {
                draggingSlider = "margin";
                applySliderValue(section, "margin", mouseX);
                return true;
            }
            cursorY += 22;
        }
        if (handleExitEditorPress(section, layout, mouseX, mouseY, mouseButton)) {
            return true;
        }
        return false;
    }

    private boolean handleFloorTopologyControlPress(MKTowerStackSizingReport.SectionInfo section,
                                                    int x, int y, int width,
                                                    double mouseX, double mouseY, int mouseButton) {
        if (isInSlider(mouseX, mouseY, sliderBounds(x, y + 14, width, "floorMinMain"))) {
            draggingSlider = "floorMinMain";
            applySliderValue(section, "floorMinMain", mouseX);
            return true;
        }
        if (isInSlider(mouseX, mouseY, sliderBounds(x, y + 36, width, "floorMaxMain"))) {
            draggingSlider = "floorMaxMain";
            applySliderValue(section, "floorMaxMain", mouseX);
            return true;
        }
        if (isInSlider(mouseX, mouseY, sliderBounds(x, y + 58, width, "floorBranchCap"))) {
            draggingSlider = "floorBranchCap";
            applySliderValue(section, "floorBranchCap", mouseX);
            return true;
        }
        ButtonBounds modeBounds = new ButtonBounds(x, y + 80, Math.min(92, width), 16);
        if (isInRect(mouseX, mouseY, modeBounds.x(), modeBounds.y(), modeBounds.width(), modeBounds.height())) {
            controls.cycleFloorHallwayLeadInMode(section.key(), WorkspaceTopologyUiSupport.isReverseClick(mouseButton));
            return true;
        }
        if (controls.floorHallwayLeadInMode(section.key()) == MKWorkspaceHallwayLeadInMode.MANUAL) {
            int sliderX = x + modeBounds.width() + 6;
            int sliderWidth = Math.max(40, width - modeBounds.width() - 6);
            if (isInSlider(mouseX, mouseY, sliderBounds(sliderX, y + 80, sliderWidth, "floorLeadIn"))) {
                draggingSlider = "floorLeadIn";
                applySliderValue(section, "floorLeadIn", mouseX);
                return true;
            }
        }
        for (String id : List.of("mainRoomWidth", "mainRoomLength", "mainRoomHeight",
                "branchRoomWidth", "branchRoomLength", "branchRoomHeight")) {
            int sliderY = floorRoomSliderY(y, id);
            if (isInSlider(mouseX, mouseY, sliderBounds(x, sliderY, width, id))) {
                draggingSlider = id;
                applySliderValue(section, id, mouseX);
                return true;
            }
        }
        return false;
    }

    private boolean handleGlobalControlPress(double mouseX, double mouseY, int mouseButton) {
        ControlLayout layout = controlLayout(getX(), getY(), getWidth(), getHeight());
        int baseY = getY() + GLOBAL_CONTROL_START_OFFSET;
        if (isInSlider(mouseX, mouseY, sliderBounds(layout.controlX(), baseY, layout.controlWidth(),
                "stackWidth"))) {
            draggingSlider = "stackWidth";
            applyGlobalSliderValue("stackWidth", mouseX);
            return true;
        }
        if (isInSlider(mouseX, mouseY, sliderBounds(layout.controlX(), baseY + 22, layout.controlWidth(),
                "stackLength"))) {
            draggingSlider = "stackLength";
            applyGlobalSliderValue("stackLength", mouseX);
            return true;
        }
        if (isInSlider(mouseX, mouseY, sliderBounds(layout.controlX(), baseY + 44, layout.controlWidth(),
                "shaftSize"))) {
            draggingSlider = "shaftSize";
            applyGlobalSliderValue("shaftSize", mouseX);
            return true;
        }
        if (isInRect(mouseX, mouseY, layout.controlX(), baseY + 66, layout.controlWidth(), 16)) {
            controls.cycleShaftPlacement(WorkspaceTopologyUiSupport.isReverseClick(mouseButton));
            return true;
        }
        return false;
    }

    private boolean handleExitEditorPress(MKTowerStackSizingReport.SectionInfo section, ControlLayout layout,
                                          double mouseX, double mouseY, int mouseButton) {
        ExitLayout exitLayout = exitLayout(section, layout);
        Direction direction = hitExitDirection(exitLayout.maskX(), exitLayout.maskY(), (int) mouseX, (int) mouseY);
        if (direction != null) {
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                controls.selectExit(section.key(), direction);
                return true;
            }
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT && !controls.exitRequired(section.key(), direction)) {
                controls.toggleExit(section.key(), direction);
                return true;
            }
            return true;
        }
        Optional<MKWorkspaceFamilyHorizontalExitDefinition> selectedExit = controls.selectedExit(section.key());
        if (selectedExit.isEmpty() || selectedExit.get().isVerticalAccess()) {
            return false;
        }
        boolean required = controls.exitRequired(section.key(), selectedExit.get().direction());
        boolean geometryEditable = canEditRequiredExitGeometry(selectedExit.get());
        for (String id : List.of("direction", "role", "connection", "profile")) {
            ButtonBounds bounds = editorButtonBounds(exitLayout.editorX(), exitLayout.editorY(), id);
            if (isInRect(mouseX, mouseY, bounds.x(), bounds.y(), bounds.width(), bounds.height())) {
                if (!required || geometryEditable && "profile".equals(id)) {
                    boolean reverse = WorkspaceTopologyUiSupport.isReverseClick(mouseButton);
                    switch (id) {
                        case "direction" -> controls.cycleSelectedExitDirection(section.key(), reverse);
                        case "role" -> controls.cycleSelectedExitPathKind(section.key(), reverse);
                        case "connection" -> controls.cycleSelectedExitConnectionMode(section.key(), reverse);
                        case "profile" -> controls.cycleSelectedExitOpeningProfile(section.key(), reverse);
                        default -> {
                        }
                    }
                }
                return true;
            }
        }
        if (!required || geometryEditable) {
            SliderBounds sideBounds = sliderBounds(exitLayout.sliderX(), exitLayout.sliderY(),
                    exitLayout.sliderWidth(), "exitSide");
            if (isInSlider(mouseX, mouseY, sideBounds)) {
                draggingSlider = "exitSide";
                applySliderValue(section, "exitSide", mouseX);
                return true;
            }
            SliderBounds verticalBounds = sliderBounds(exitLayout.sliderX(), exitLayout.sliderY() + 22,
                    exitLayout.sliderWidth(), "exitVertical");
            if (isInSlider(mouseX, mouseY, verticalBounds)) {
                draggingSlider = "exitVertical";
                applySliderValue(section, "exitVertical", mouseX);
                return true;
            }
        }
        return false;
    }

    private boolean canEditRequiredExitGeometry(MKWorkspaceFamilyHorizontalExitDefinition exit) {
        return exit.pathKind() == MKWorkspaceHorizontalExitPathKind.INGRESS;
    }

    private boolean handleTogglePress(double mouseX, double mouseY) {
        for (int index = 0; index < toggleSpecs().size(); index++) {
            ToggleSpec spec = toggleSpecs().get(index);
            if (isInToggle(mouseX, mouseY, toggleBounds(getX(), getY(), getWidth(), getHeight(), index))) {
                spec.toggle().run();
                return true;
            }
        }
        return false;
    }

    private boolean handleCounterPress(MKTowerStackSizingReport.SectionInfo section, CounterBounds bounds,
                                       double mouseX, double mouseY, String id) {
        if (isInButton(mouseX, mouseY, bounds.minusX(), bounds.buttonY())) {
            if ("max".equals(id)) {
                controls.adjustMaxFloors(section.key(), -1);
            } else {
                controls.adjustMinFloors(section.key(), -1);
            }
            return true;
        }
        if (isInButton(mouseX, mouseY, bounds.plusX(), bounds.buttonY())) {
            if ("max".equals(id)) {
                controls.adjustMaxFloors(section.key(), 1);
            } else {
                controls.adjustMinFloors(section.key(), 1);
            }
            return true;
        }
        return false;
    }

    private void applySliderValue(MKTowerStackSizingReport.SectionInfo section, String slider, double mouseX) {
        if ("stackWidth".equals(slider) || "stackLength".equals(slider) || "shaftSize".equals(slider)) {
            applyGlobalSliderValue(slider, mouseX);
            return;
        }
        ControlLayout layout = controlLayout(getX(), getY(), getWidth(), getHeight());
        int sliderY = switch (slider) {
            case "height" -> layout.controlY() + REGION_FIRST_CONTROL_OFFSET;
            case "exitSide" -> exitLayout(section, layout).sliderY();
            case "exitVertical" -> exitLayout(section, layout).sliderY() + 22;
            case "floorMinMain" -> floorTopologyControlY(section, layout) + 14;
            case "floorMaxMain" -> floorTopologyControlY(section, layout) + 36;
            case "floorBranchCap" -> floorTopologyControlY(section, layout) + 58;
            case "floorLeadIn" -> floorTopologyControlY(section, layout) + 80;
            case "mainRoomWidth", "mainRoomLength", "mainRoomHeight",
                    "branchRoomWidth", "branchRoomLength", "branchRoomHeight" ->
                    floorRoomSliderY(floorTopologyControlY(section, layout), slider);
            default -> marginSliderY(section, layout);
        };
        SliderBounds bounds = sliderBounds(layout.controlX(), sliderY, layout.controlWidth(), slider);
        if ("exitSide".equals(slider) || "exitVertical".equals(slider)) {
            ExitLayout exitLayout = exitLayout(section, layout);
            bounds = sliderBounds(exitLayout.sliderX(), sliderY, exitLayout.sliderWidth(), slider);
        } else if ("floorLeadIn".equals(slider)) {
            int modeWidth = Math.min(92, layout.controlWidth());
            bounds = sliderBounds(layout.controlX() + modeWidth + 6, sliderY,
                    Math.max(40, layout.controlWidth() - modeWidth - 6), slider);
        }
        if ("height".equals(slider)) {
            controls.height(section.key(), sliderValue(mouseX, bounds, controls.heightMin(), controls.heightMax()));
        } else if ("exitSide".equals(slider)) {
            controls.selectedExitSideOffset(section.key(), sliderValue(mouseX, bounds,
                    controls.selectedExitSideMin(section.key()), controls.selectedExitSideMax(section.key())));
        } else if ("exitVertical".equals(slider)) {
            controls.selectedExitVerticalOffset(section.key(), sliderValue(mouseX, bounds, 0,
                    controls.selectedExitVerticalMax(section.key())));
        } else if ("floorMinMain".equals(slider)) {
            controls.floorMinMainPathPieces(section.key(), sliderValue(mouseX, bounds, 0, 10));
        } else if ("floorMaxMain".equals(slider)) {
            controls.floorMaxMainPathPieces(section.key(), sliderValue(mouseX, bounds, 0, 10));
        } else if ("floorBranchCap".equals(slider)) {
            controls.floorMaxBranchPiecesBeforeCap(section.key(), sliderValue(mouseX, bounds, 0,
                    MKWorkspaceFloorTopologySettings.MAX_BRANCH_PIECES_BEFORE_CAP));
        } else if ("floorLeadIn".equals(slider)) {
            controls.floorManualHallwayLeadInPieces(section.key(), sliderValue(mouseX, bounds, 0,
                    MKWorkspaceFloorTopologySettings.MAX_MANUAL_HALLWAY_LEAD_IN_PIECES));
        } else if ("mainRoomWidth".equals(slider)) {
            controls.floorRoomWidth(section.key(), MKWorkspaceFloorRoomKind.MAIN_ROOM,
                    makeOdd(sliderValue(mouseX, bounds, 3, MAX_STACK_FOOTPRINT)));
        } else if ("mainRoomLength".equals(slider)) {
            controls.floorRoomLength(section.key(), MKWorkspaceFloorRoomKind.MAIN_ROOM,
                    makeOdd(sliderValue(mouseX, bounds, 3, MAX_STACK_FOOTPRINT)));
        } else if ("mainRoomHeight".equals(slider)) {
            controls.floorRoomHeight(section.key(), MKWorkspaceFloorRoomKind.MAIN_ROOM,
                    sliderValue(mouseX, bounds, controls.floorRoomHeightMin(section.key()),
                            controls.floorRoomHeightMax(section.key())));
        } else if ("branchRoomWidth".equals(slider)) {
            controls.floorRoomWidth(section.key(), MKWorkspaceFloorRoomKind.BRANCH_ROOM,
                    makeOdd(sliderValue(mouseX, bounds, 3, MAX_STACK_FOOTPRINT)));
        } else if ("branchRoomLength".equals(slider)) {
            controls.floorRoomLength(section.key(), MKWorkspaceFloorRoomKind.BRANCH_ROOM,
                    makeOdd(sliderValue(mouseX, bounds, 3, MAX_STACK_FOOTPRINT)));
        } else if ("branchRoomHeight".equals(slider)) {
            controls.floorRoomHeight(section.key(), MKWorkspaceFloorRoomKind.BRANCH_ROOM,
                    sliderValue(mouseX, bounds, controls.floorRoomHeightMin(section.key()),
                            controls.floorRoomHeightMax(section.key())));
        } else {
            controls.margin(section.key(), sliderValue(mouseX, bounds, 0, controls.marginMax(section.key())));
        }
    }

    private void applyGlobalSliderValue(String slider, double mouseX) {
        ControlLayout layout = controlLayout(getX(), getY(), getWidth(), getHeight());
        int sliderY = switch (slider) {
            case "stackWidth" -> getY() + GLOBAL_CONTROL_START_OFFSET;
            case "stackLength" -> getY() + GLOBAL_CONTROL_START_OFFSET + 22;
            case "shaftSize" -> getY() + GLOBAL_CONTROL_START_OFFSET + 44;
            default -> getY();
        };
        SliderBounds bounds = sliderBounds(layout.controlX(), sliderY, layout.controlWidth(), slider);
        if ("stackWidth".equals(slider)) {
            controls.stackWidth(makeOdd(sliderValue(mouseX, bounds, 3, MAX_STACK_FOOTPRINT)));
        } else if ("stackLength".equals(slider)) {
            controls.stackLength(makeOdd(sliderValue(mouseX, bounds, 3, MAX_STACK_FOOTPRINT)));
        } else if ("shaftSize".equals(slider)) {
            controls.shaftSize(nearestAllowedShaftSize(sliderValue(mouseX, bounds,
                    controls.shaftSizeMin(), controls.shaftSizeMax())));
        }
    }

    private int marginSliderY(MKTowerStackSizingReport.SectionInfo section, ControlLayout layout) {
        int y = layout.controlY() + REGION_FIRST_CONTROL_OFFSET + 22;
        if (controls.hasFloorCounts(section.key())) {
            y += 18;
            if (controls.maxFloors(section.key()) > 0) {
                y += 18;
            }
        }
        return y;
    }

    private int floorRoomSliderY(int floorControlY, String id) {
        return switch (id) {
            case "mainRoomWidth" -> floorControlY + 116;
            case "mainRoomLength" -> floorControlY + 138;
            case "mainRoomHeight" -> floorControlY + 160;
            case "branchRoomWidth" -> floorControlY + 196;
            case "branchRoomLength" -> floorControlY + 218;
            case "branchRoomHeight" -> floorControlY + 240;
            default -> floorControlY;
        };
    }

    private int floorTopologyControlY(MKTowerStackSizingReport.SectionInfo section, ControlLayout layout) {
        int y = layout.controlY() + REGION_FIRST_CONTROL_OFFSET + 22;
        if (controls.hasFloorCounts(section.key())) {
            y += 18;
            if (controls.maxFloors(section.key()) > 0) {
                y += 18;
            }
        }
        if (controls.hasMargin(section.key())) {
            y += 22;
        }
        return y;
    }

    private int sliderValue(double mouseX, SliderBounds bounds, int min, int max) {
        float percent = (float) ((mouseX - bounds.trackX()) / Math.max(1.0, bounds.trackWidth()));
        percent = Math.max(0.0f, Math.min(1.0f, percent));
        return min + Math.round(percent * (max - min));
    }

    private int makeOdd(int value) {
        return value % 2 == 0 ? value + 1 : value;
    }

    private int nearestAllowedShaftSize(int value) {
        return controls.allowedShaftSizes().stream()
                .min(java.util.Comparator.comparingInt(allowed -> Math.abs(allowed - value)))
                .orElse(controls.shaftSize());
    }

    private Optional<MKTowerStackSizingReport.SectionInfo> hoveredSection(int x, int y, int width,
                                                                         int height, int mouseX,
                                                                         int mouseY) {
        StackBounds stackBounds = stackBounds(x, y, width, height);
        int totalBlocks = Math.max(1, report.sections().stream().mapToInt(this::displayHeight).sum());
        int cursor = stackBounds.bottom();
        List<MKTowerStackSizingReport.SectionInfo> sections = report.sections();
        for (int index = 0; index < sections.size(); index++) {
            MKTowerStackSizingReport.SectionInfo section = sections.get(index);
            int sectionHeight = scaledSectionHeight(section, index, sections, stackBounds.top(), stackBounds.bottom(),
                    totalBlocks, cursor);
            int top = cursor - sectionHeight;
            if (mouseX >= stackBounds.left() && mouseX <= stackBounds.left() + stackBounds.width() &&
                    mouseY >= top && mouseY <= cursor) {
                return Optional.of(section);
            }
            cursor = top;
        }
        return Optional.empty();
    }

    private Optional<String> hoveredGroundLineTooltip(int x, int y, int width, int height, int mouseX, int mouseY) {
        StackBounds stackBounds = stackBounds(x, y, width, height);
        int groundY = groundLineY(stackBounds);
        if (groundY < stackBounds.top() || groundY > stackBounds.bottom()) {
            return Optional.empty();
        }
        int left = stackBounds.left() - 5;
        int lineWidth = stackBounds.width() + 10;
        return isInRect(mouseX, mouseY, left, groundY - 3, lineWidth, 6)
                ? Optional.of("Ground Line")
                : Optional.empty();
    }

    private int groundLineY(StackBounds stackBounds) {
        int totalBlocks = Math.max(1, report.sections().stream().mapToInt(this::displayHeight).sum());
        int cursor = stackBounds.bottom();
        List<MKTowerStackSizingReport.SectionInfo> sections = report.sections();
        for (int index = 0; index < sections.size(); index++) {
            MKTowerStackSizingReport.SectionInfo section = sections.get(index);
            int sectionHeight = scaledSectionHeight(section, index, sections, stackBounds.top(), stackBounds.bottom(),
                    totalBlocks, cursor);
            if ("entry".equals(section.key())) {
                return cursor;
            }
            cursor -= sectionHeight;
        }
        return -1;
    }

    private Optional<ToggleSpec> hoveredToggle(int x, int y, int width, int height, int mouseX, int mouseY) {
        for (int index = 0; index < toggleSpecs().size(); index++) {
            ToggleSpec spec = toggleSpecs().get(index);
            if (isInToggle(mouseX, mouseY, toggleBounds(x, y, width, height, index))) {
                return Optional.of(spec);
            }
        }
        return Optional.empty();
    }

    private int drawSection(GuiGraphics graphics, int left, int width, int cursor, int height, int color,
                            int mouseX, int mouseY, MKTowerStackSizingReport.SectionInfo section) {
        int top = cursor - height;
        graphics.fill(left, top, left + width, cursor - 1, color);
        graphics.fill(left, top, left + width, top + 1, 0xAA000000);
        drawExitMarkers(graphics, section, left, top, width, height);
        if (mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= cursor) {
            drawOutline(graphics, left, top, width, cursor, HOVER_OUTLINE);
        }
        if (section.key().equals(selectedKey)) {
            drawOutline(graphics, left - 1, top - 1, width + 2, cursor + 1, SELECTED_OUTLINE);
        }
        return top;
    }

    private StackBounds stackBounds(int x, int y, int width, int height) {
        int maxWidth = Math.min(88, Math.max(52, width / 3));
        int scaledWidth = Math.max(18, Math.min(maxWidth,
                Math.round(maxWidth * (Math.min(MAX_STACK_FOOTPRINT, Math.max(1, report.width())) /
                        (float) MAX_STACK_FOOTPRINT))));
        int left = x + 14 + ((maxWidth - scaledWidth) / 2);
        int top = y + 8;
        int bottom = Math.min(top + STACK_DIAGRAM_HEIGHT, y + height - 6);
        return new StackBounds(left, top, Math.max(top + 1, bottom), scaledWidth, maxWidth);
    }

    private List<ToggleSpec> toggleSpecs() {
        return List.of(
                new ToggleSpec("Basement Entry", "Basement Entry", controls.basementEntryEnabled(),
                        controls::toggleBasementEntry),
                new ToggleSpec("Top Cap Approach", "Top Cap Approach", controls.topCapApproachEnabled(),
                        controls::toggleTopCapApproach),
                new ToggleSpec("Bottom Cap Approach", "Basement Cap Approach", controls.basementCapApproachEnabled(),
                        controls::toggleBasementCapApproach)
        );
    }

    private ToggleBounds toggleBounds(int x, int y, int width, int height, int index) {
        ControlLayout layout = controlLayout(x, y, width, height);
        int rowY = y + TOGGLE_START_OFFSET + (index * (TOGGLE_ROW_HEIGHT + TOGGLE_ROW_GAP));
        int boxX = layout.controlX() + 2;
        int boxY = rowY + 3;
        return new ToggleBounds(layout.controlX(), rowY, layout.controlWidth(), boxX, boxY,
                boxX + 14, rowY + 4);
    }

    private ControlLayout controlLayout(int x, int y, int width, int height) {
        int maxStackWidth = Math.min(88, Math.max(52, width / 3));
        int controlX = x + 14 + maxStackWidth + 12;
        return new ControlLayout(controlX, y + REGION_CONTROL_TOP_OFFSET, Math.max(80, x + width - 8 - controlX));
    }

    private SliderBounds sliderBounds(int x, int y, int width, String id) {
        int trackX = x + 52;
        return new SliderBounds(id, trackX, y + 6, Math.max(24, width - 58));
    }

    private CounterBounds counterBounds(int x, int y, String id) {
        return new CounterBounds(id, x + 52, x + 72, x + 94, y + 1);
    }

    private boolean isInSlider(double mouseX, double mouseY, SliderBounds bounds) {
        return mouseX >= bounds.trackX() && mouseX <= bounds.trackX() + bounds.trackWidth() &&
                mouseY >= bounds.trackY() - 4 && mouseY <= bounds.trackY() + 8;
    }

    private boolean isInButton(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX <= x + 13 && mouseY >= y && mouseY <= y + 13;
    }

    private boolean isInToggle(double mouseX, double mouseY, ToggleBounds bounds) {
        return mouseX >= bounds.cellX() && mouseX <= bounds.cellX() + bounds.cellWidth() &&
                mouseY >= bounds.cellY() && mouseY <= bounds.cellY() + TOGGLE_ROW_HEIGHT;
    }

    private boolean isInRect(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private boolean intersects(ButtonBounds first, ButtonBounds second) {
        return first.x() < second.x() + second.width() &&
                first.x() + first.width() > second.x() &&
                first.y() < second.y() + second.height() &&
                first.y() + first.height() > second.y();
    }

    private void drawOutline(GuiGraphics graphics, int left, int top, int width, int bottom, int color) {
        graphics.fill(left, top, left + width, top + 1, color);
        graphics.fill(left, bottom - 1, left + width, bottom, color);
        graphics.fill(left, top, left + 1, bottom, color);
        graphics.fill(left + width - 1, top, left + width, bottom, color);
    }

    private void drawDiagonalHatch(GuiGraphics graphics, ButtonBounds bounds, int color) {
        int spacing = 6;
        int left = bounds.x();
        int top = bounds.y();
        int right = bounds.x() + bounds.width();
        int bottom = bounds.y() + bounds.height();
        for (int startX = left - bounds.height(); startX < right; startX += spacing) {
            for (int step = 0; step <= bounds.height(); step++) {
                int px = startX + step;
                int py = top + step;
                if (px >= left && px < right && py >= top && py < bottom) {
                    graphics.fill(px, py, Math.min(right, px + 2), Math.min(bottom, py + 2), color);
                }
            }
        }
    }

    private void drawExitMarkers(GuiGraphics graphics, MKTowerStackSizingReport.SectionInfo section,
                                 int sectionLeft, int top, int sectionWidth, int sectionHeight) {
        for (MKWorkspaceFamilyHorizontalExitDefinition exit : controls.exits(section.key())) {
            if (exit.direction().getAxis().isVertical()) {
                continue;
            }
            int centerY = top + Math.max(2, Math.min(sectionHeight - 2,
                    (sectionHeight / 2) + Math.round(exit.verticalOffset() / (float) Math.max(1,
                            section.height()))));
            int centerX = sectionLeft + (sectionWidth / 2);
            switch (exit.direction()) {
                case EAST -> drawExitTick(graphics, sectionLeft + sectionWidth - 1, centerY, true);
                case WEST -> drawExitTick(graphics, sectionLeft, centerY, false);
                case NORTH -> drawExitDiamond(graphics, centerX, top + 3);
                case SOUTH -> drawExitDiamond(graphics, centerX, top + Math.max(3, sectionHeight - 4));
                default -> {
                }
            }
        }
    }

    private void drawExitTick(GuiGraphics graphics, int x, int y, boolean rightSide) {
        int start = rightSide ? x - 1 : x;
        int end = rightSide ? x + 5 : x + 6;
        graphics.fill(start - 1, y - 2, end + 1, y + 3, EXIT_MARKER_SHADOW);
        graphics.fill(start, y - 1, end, y + 2, EXIT_MARKER);
    }

    private void drawExitDiamond(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 2, y - 1, x + 3, y + 2, EXIT_MARKER_SHADOW);
        graphics.fill(x - 1, y, x + 2, y + 1, EXIT_MARKER);
        graphics.fill(x, y - 1, x + 1, y + 2, EXIT_MARKER);
    }

    private void drawExitArm(GuiGraphics graphics, MKTowerStackSizingReport.SectionInfo section, Direction direction,
                             int roomLeft, int roomTop, int roomRight, int roomBottom, int centerX, int centerY,
                             int mouseX, int mouseY) {
        int color = exitColor(section, direction);
        if (hitExitDirection(centerX - (EXIT_MASK_SIZE / 2), centerY - (EXIT_MASK_SIZE / 2), mouseX, mouseY) ==
                direction) {
            color = CONTROL_ACTIVE;
        }
        switch (direction) {
            case NORTH -> graphics.fill(centerX - (EXIT_ARM_THICKNESS / 2), roomTop - EXIT_ARM_LENGTH,
                    centerX + (EXIT_ARM_THICKNESS / 2), roomTop, color);
            case EAST -> graphics.fill(roomRight, centerY - (EXIT_ARM_THICKNESS / 2),
                    roomRight + EXIT_ARM_LENGTH, centerY + (EXIT_ARM_THICKNESS / 2), color);
            case SOUTH -> graphics.fill(centerX - (EXIT_ARM_THICKNESS / 2), roomBottom,
                    centerX + (EXIT_ARM_THICKNESS / 2), roomBottom + EXIT_ARM_LENGTH, color);
            case WEST -> graphics.fill(roomLeft - EXIT_ARM_LENGTH, centerY - (EXIT_ARM_THICKNESS / 2),
                    roomLeft, centerY + (EXIT_ARM_THICKNESS / 2), color);
            default -> {
            }
        }
    }

    private void drawVerticalExitButton(GuiGraphics graphics, Minecraft mc, MKTowerStackSizingReport.SectionInfo section,
                                        Direction direction, int maskX, int maskY, int x, int y, int mouseX,
                                        int mouseY) {
        int color = exitColor(section, direction);
        if (hitExitDirection(maskX, maskY, mouseX, mouseY) == direction) {
            color = CONTROL_ACTIVE;
        }
        graphics.fill(x, y, x + EXIT_VERTICAL_SIZE, y + EXIT_VERTICAL_SIZE, color);
        drawOutline(graphics, x, y, EXIT_VERTICAL_SIZE, y + EXIT_VERTICAL_SIZE, CONTROL_ACTIVE);
        graphics.drawCenteredString(mc.font, Component.literal(direction == Direction.UP ? "T" : "B"),
                x + (EXIT_VERTICAL_SIZE / 2), y + 7, exitForDirection(section.key(), direction).isPresent() ?
                        TEXT : MUTED_TEXT);
    }

    private void drawExitLabel(GuiGraphics graphics, Minecraft mc, MKTowerStackSizingReport.SectionInfo section,
                               Direction direction, int x, int y) {
        Optional<MKWorkspaceFamilyHorizontalExitDefinition> exit = exitForDirection(section.key(), direction);
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
                case LINK_CANDIDATE -> "L";
                case VERTICAL_ACCESS -> "";
            };
            color = controls.exitRequired(section.key(), direction) ? EXIT_REQUIRED : TEXT;
        }
        graphics.drawCenteredString(mc.font, Component.literal(label), x, y, color);
    }

    private int exitColor(MKTowerStackSizingReport.SectionInfo section, Direction direction) {
        Optional<MKWorkspaceFamilyHorizontalExitDefinition> exit = exitForDirection(section.key(), direction);
        if (exit.isEmpty()) {
            return EXIT_INACTIVE;
        }
        Optional<MKWorkspaceFamilyHorizontalExitDefinition> selected = controls.selectedExit(section.key());
        if (selected.isPresent() && selected.get().direction() == direction) {
            return EXIT_SELECTED;
        }
        return controls.exitRequired(section.key(), direction) ? EXIT_REQUIRED : EXIT_ACTIVE;
    }

    private Optional<MKWorkspaceFamilyHorizontalExitDefinition> exitForDirection(String sectionKey,
                                                                                 Direction direction) {
        return controls.exits(sectionKey).stream()
                .filter(exit -> exit.direction() == direction)
                .findFirst();
    }

    private Direction hitExitDirection(int x, int y, int mouseX, int mouseY) {
        int centerX = x + (EXIT_MASK_SIZE / 2);
        int centerY = y + (EXIT_MASK_SIZE / 2);
        int roomLeft = centerX - (EXIT_ROOM_SIZE / 2);
        int roomTop = centerY - (EXIT_ROOM_SIZE / 2);
        int roomRight = roomLeft + EXIT_ROOM_SIZE;
        int roomBottom = roomTop + EXIT_ROOM_SIZE;
        int topButtonY = y + 6;
        if (isInRect(mouseX, mouseY, x + 6, topButtonY, EXIT_VERTICAL_SIZE, EXIT_VERTICAL_SIZE)) {
            return Direction.UP;
        }
        if (isInRect(mouseX, mouseY, x + EXIT_MASK_SIZE - 6 - EXIT_VERTICAL_SIZE, topButtonY,
                EXIT_VERTICAL_SIZE, EXIT_VERTICAL_SIZE)) {
            return Direction.DOWN;
        }
        if (mouseX >= centerX - (EXIT_ARM_THICKNESS / 2) && mouseX <= centerX + (EXIT_ARM_THICKNESS / 2)) {
            if (mouseY >= roomTop - EXIT_ARM_LENGTH && mouseY <= roomTop) {
                return Direction.NORTH;
            }
            if (mouseY >= roomBottom && mouseY <= roomBottom + EXIT_ARM_LENGTH) {
                return Direction.SOUTH;
            }
        }
        if (mouseY >= centerY - (EXIT_ARM_THICKNESS / 2) && mouseY <= centerY + (EXIT_ARM_THICKNESS / 2)) {
            if (mouseX >= roomLeft - EXIT_ARM_LENGTH && mouseX <= roomLeft) {
                return Direction.WEST;
            }
            if (mouseX >= roomRight && mouseX <= roomRight + EXIT_ARM_LENGTH) {
                return Direction.EAST;
            }
        }
        return null;
    }

    private ExitLayout exitLayout(MKTowerStackSizingReport.SectionInfo section, ControlLayout layout) {
        int maskY = layout.controlY() + 118;
        if (controls.hasFloorCounts(section.key()) && controls.maxFloors(section.key()) > 0) {
            maskY += 18;
        }
        if (controls.hasMargin(section.key())) {
            maskY += 20;
        }
        int editorX = layout.controlX() + EXIT_MASK_SIZE + 7;
        int editorWidth = Math.max(48, layout.controlWidth() - EXIT_MASK_SIZE - 7);
        return new ExitLayout(layout.controlX(), maskY, editorX, maskY, editorWidth,
                layout.controlX(), maskY + EXIT_MASK_SIZE + 13, layout.controlWidth());
    }

    private ButtonBounds editorButtonBounds(int x, int y, String id) {
        int width = Math.max(72, controlLayout(getX(), getY(), getWidth(), getHeight()).controlWidth() -
                EXIT_MASK_SIZE - 7);
        return switch (id) {
            case "direction" -> new ButtonBounds(x, y, width, 18);
            case "role" -> new ButtonBounds(x, y + 20, width, 18);
            case "connection" -> new ButtonBounds(x, y + 40, width, 18);
            case "profile" -> new ButtonBounds(x, y + 60, width, 18);
            default -> new ButtonBounds(x, y, width, 18);
        };
    }

    private Optional<String> hoveredExitTooltip(int x, int y, int width, int height, int mouseX, int mouseY) {
        Optional<MKTowerStackSizingReport.SectionInfo> sectionOpt = selectedSection();
        if (sectionOpt.isEmpty()) {
            return Optional.empty();
        }
        MKTowerStackSizingReport.SectionInfo section = sectionOpt.get();
        ControlLayout layout = controlLayout(x, y, width, height);
        ExitLayout exitLayout = exitLayout(section, layout);
        Direction direction = hitExitDirection(exitLayout.maskX(), exitLayout.maskY(), mouseX, mouseY);
        if (direction != null) {
            return Optional.of(exitDirectionTooltip(section, direction));
        }
        Optional<MKWorkspaceFamilyHorizontalExitDefinition> selectedExit = controls.selectedExit(section.key());
        if (selectedExit.isEmpty()) {
            return Optional.empty();
        }
        MKWorkspaceFamilyHorizontalExitDefinition exit = selectedExit.get();
        if (!exit.isVerticalAccess()) {
            for (String id : List.of("direction", "role", "connection", "profile")) {
                ButtonBounds bounds = editorButtonBounds(exitLayout.editorX(), exitLayout.editorY(), id);
                if (isInRect(mouseX, mouseY, bounds.x(), bounds.y(), bounds.width(), bounds.height())) {
                    return Optional.of(exitButtonTooltip(section, exit, id));
                }
            }
            if (isInSlider(mouseX, mouseY, sliderBounds(exitLayout.sliderX(), exitLayout.sliderY(),
                    exitLayout.sliderWidth(), "exitSide"))) {
                return Optional.of("Side Offset: " + exit.sideOffset() + "\nRange: " +
                        controls.selectedExitSideMin(section.key()) + " to " +
                        controls.selectedExitSideMax(section.key()));
            }
            if (isInSlider(mouseX, mouseY, sliderBounds(exitLayout.sliderX(), exitLayout.sliderY() + 22,
                    exitLayout.sliderWidth(), "exitVertical"))) {
                return Optional.of("Vertical Offset: " + exit.verticalOffset() + "\nRange: 0 to " +
                        controls.selectedExitVerticalMax(section.key()));
            }
        }
        return Optional.empty();
    }

    private String exitDirectionTooltip(MKTowerStackSizingReport.SectionInfo section, Direction direction) {
        Optional<MKWorkspaceFamilyHorizontalExitDefinition> exit = exitForDirection(section.key(), direction);
        String label = direction.getAxis().isVertical() ?
                (direction == Direction.UP ? "Top" : "Bottom") : formatDirection(direction);
        if (exit.isEmpty()) {
            return label + ": no exit";
        }
        return label + "\n" + fullExitValue(section, exit.get());
    }

    private String exitButtonTooltip(MKTowerStackSizingReport.SectionInfo section,
                                     MKWorkspaceFamilyHorizontalExitDefinition exit, String id) {
        String locked = controls.exitRequired(section.key(), exit.direction()) ? "\nRequired topological exit" : "";
        return switch (id) {
            case "direction" -> "Direction: " + formatDirection(exit.direction()) + locked;
            case "role" -> "Role: " + formatPathKind(exit.pathKind()) + locked;
            case "connection" -> "Connection: " +
                    formatConnection(exit.connectionMode().getSerializedName()) + locked;
            case "profile" -> "Opening Profile: " + exit.openingProfileId() + locked;
            default -> fullExitValue(section, exit);
        };
    }

    private String fullExitValue(MKTowerStackSizingReport.SectionInfo section,
                                 MKWorkspaceFamilyHorizontalExitDefinition exit) {
        if (exit.isVerticalAccess()) {
            return "Vertical access" +
                    (controls.exitRequired(section.key(), exit.direction()) ? "\nRequired topological exit" : "");
        }
        return "Role: " + formatPathKind(exit.pathKind()) +
                "\nConnection: " + formatConnection(exit.connectionMode().getSerializedName()) +
                "\nOpening Profile: " + exit.openingProfileId() +
                "\nSide Offset: " + exit.sideOffset() +
                "\nVertical Offset: " + exit.verticalOffset() +
                (controls.exitRequired(section.key(), exit.direction()) ? "\nRequired topological exit" : "");
    }

    private String formatDirection(Direction direction) {
        return WorkspaceTopologyUiSupport.formatTopologyLabel(direction.getSerializedName());
    }

    private String formatPathKind(com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind pathKind) {
        return WorkspaceTopologyUiSupport.formatTopologyLabel(pathKind.getSerializedName());
    }

    private String formatConnection(String connectionMode) {
        return WorkspaceTopologyUiSupport.formatTopologyLabel(connectionMode);
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

    private int scaledSectionHeight(MKTowerStackSizingReport.SectionInfo section, int index,
                                    List<MKTowerStackSizingReport.SectionInfo> sections, int stackTop,
                                    int stackBottom, int totalBlocks, int cursor) {
        int availableHeight = Math.max(1, stackBottom - stackTop);
        int previewBlocks = heightPreviewBlocks(totalBlocks);
        int scaled = Math.round((displayHeight(section) / (float) previewBlocks) * availableHeight);
        return Math.min(Math.max(0, cursor - stackTop), Math.max(section.active() ? 2 : 4, scaled));
    }

    private int displayHeight(MKTowerStackSizingReport.SectionInfo section) {
        return section.active() ? section.height() : 1;
    }

    private int heightPreviewBlocks(int totalBlocks) {
        if (totalBlocks <= SMALL_HEIGHT_PREVIEW_BLOCKS) {
            return SMALL_HEIGHT_PREVIEW_BLOCKS;
        }
        if (totalBlocks <= MEDIUM_HEIGHT_PREVIEW_BLOCKS) {
            return MEDIUM_HEIGHT_PREVIEW_BLOCKS;
        }
        return MAX_HEIGHT_PREVIEW_BLOCKS;
    }

    private Optional<MKTowerStackSizingReport.SectionInfo> selectedSection() {
        return report.sections().stream()
                .filter(section -> section.key().equals(selectedKey))
                .findFirst();
    }

    private int colorForKey(String key) {
        return switch (key) {
            case "entry" -> ENTRY;
            case "main_floor" -> MAIN;
            case "basement_floor" -> BASEMENT;
            case "basement_entry" -> BASEMENT_ENTRY;
            case "basement_cap" -> BASEMENT_CAP;
            case "basement_cap_approach", "top_cap_approach" -> APPROACH;
            case "top_cap" -> CAP;
            default -> MUTED_TEXT;
        };
    }

    private String tooltip(MKTowerStackSizingReport.SectionInfo section) {
        String tooltip = section.label() + "\nheight " + section.height();
        if (!section.active()) {
            tooltip += "\nmax floors 0";
        }
        List<MKWorkspaceFamilyHorizontalExitDefinition> exits = controls.exits(section.key()).stream()
                .filter(exit -> exit.direction().getAxis().isHorizontal())
                .toList();
        if (!exits.isEmpty()) {
            tooltip += "\nhorizontal exits: " + exits.stream()
                    .map(exit -> exit.direction().getSerializedName() + " " + exit.pathKind().getSerializedName() +
                            " y+" + exit.verticalOffset() + " s" + exit.sideOffset())
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("");
        }
        return tooltip;
    }

    public interface Controls {
        int stackWidth();

        void stackWidth(int value);

        int stackLength();

        void stackLength(int value);

        int shaftSize();

        void shaftSize(int value);

        List<Integer> allowedShaftSizes();

        default int shaftSizeMin() {
            return allowedShaftSizes().stream().min(Integer::compareTo).orElse(1);
        }

        default int shaftSizeMax() {
            return allowedShaftSizes().stream().max(Integer::compareTo).orElse(shaftSize());
        }

        MKVerticalAccessPlacement shaftPlacement();

        void cycleShaftPlacement(boolean reverse);

        int heightMin();

        int heightMax();

        int height(String sectionKey);

        void height(String sectionKey, int value);

        boolean hasFloorCounts(String sectionKey);

        int minFloors(String sectionKey);

        int maxFloors(String sectionKey);

        void adjustMinFloors(String sectionKey, int delta);

        void adjustMaxFloors(String sectionKey, int delta);

        boolean hasMargin(String sectionKey);

        String marginLabel(String sectionKey);

        int margin(String sectionKey);

        int marginMax(String sectionKey);

        void margin(String sectionKey, int value);

        boolean basementEntryEnabled();

        void toggleBasementEntry();

        boolean topCapApproachEnabled();

        void toggleTopCapApproach();

        boolean basementCapApproachEnabled();

        void toggleBasementCapApproach();

        List<MKWorkspaceFamilyHorizontalExitDefinition> exits(String sectionKey);

        Optional<MKWorkspaceFamilyHorizontalExitDefinition> selectedExit(String sectionKey);

        boolean exitRequired(String sectionKey, Direction direction);

        void selectExit(String sectionKey, Direction direction);

        void toggleExit(String sectionKey, Direction direction);

        void cycleSelectedExitDirection(String sectionKey, boolean reverse);

        void cycleSelectedExitPathKind(String sectionKey, boolean reverse);

        void cycleSelectedExitConnectionMode(String sectionKey, boolean reverse);

        void cycleSelectedExitOpeningProfile(String sectionKey, boolean reverse);

        int selectedExitSideMin(String sectionKey);

        int selectedExitSideMax(String sectionKey);

        int selectedExitVerticalMax(String sectionKey);

        void selectedExitSideOffset(String sectionKey, int value);

        void selectedExitVerticalOffset(String sectionKey, int value);

        default boolean hasFloorTopology(String sectionKey) {
            return true;
        }

        default int floorMinMainPathPieces(String sectionKey) {
            return MKWorkspaceFloorTopologySettings.DEFAULT_MIN_MAIN_PATH_PIECES;
        }

        default void floorMinMainPathPieces(String sectionKey, int value) {
        }

        default int floorMaxMainPathPieces(String sectionKey) {
            return MKWorkspaceFloorTopologySettings.DEFAULT_MAX_MAIN_PATH_PIECES;
        }

        default void floorMaxMainPathPieces(String sectionKey, int value) {
        }

        default int floorMaxBranchPiecesBeforeCap(String sectionKey) {
            return MKWorkspaceFloorTopologySettings.DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP;
        }

        default void floorMaxBranchPiecesBeforeCap(String sectionKey, int value) {
        }

        default MKWorkspaceHallwayLeadInMode floorHallwayLeadInMode(String sectionKey) {
            return MKWorkspaceHallwayLeadInMode.AUTO;
        }

        default void cycleFloorHallwayLeadInMode(String sectionKey, boolean reverse) {
        }

        default int floorManualHallwayLeadInPieces(String sectionKey) {
            return 1;
        }

        default void floorManualHallwayLeadInPieces(String sectionKey, int value) {
        }

        default boolean floorMainHallwaysEnabled(String sectionKey) {
            return true;
        }

        default void floorMainHallwaysEnabled(String sectionKey, boolean value) {
        }

        default boolean floorBranchHallwaysEnabled(String sectionKey) {
            return true;
        }

        default void floorBranchHallwaysEnabled(String sectionKey, boolean value) {
        }

        default boolean floorMainCapApproachEnabled(String sectionKey) {
            return false;
        }

        default void floorMainCapApproachEnabled(String sectionKey, boolean value) {
        }

        default int recommendedHallwayLeadInPieces(String sectionKey) {
            return Math.max(1, Math.max(stackWidth(), stackLength()) / 8);
        }

        default int effectiveHallwayLeadInPieces(String sectionKey) {
            return floorHallwayLeadInMode(sectionKey) == MKWorkspaceHallwayLeadInMode.MANUAL ?
                    floorManualHallwayLeadInPieces(sectionKey) :
                    recommendedHallwayLeadInPieces(sectionKey);
        }

        default int floorRoomWidth(String sectionKey, MKWorkspaceFloorRoomKind kind) {
            return stackWidth();
        }

        default void floorRoomWidth(String sectionKey, MKWorkspaceFloorRoomKind kind, int value) {
        }

        default int floorRoomLength(String sectionKey, MKWorkspaceFloorRoomKind kind) {
            return stackLength();
        }

        default void floorRoomLength(String sectionKey, MKWorkspaceFloorRoomKind kind, int value) {
        }

        default int floorRoomHeight(String sectionKey, MKWorkspaceFloorRoomKind kind) {
            return height(sectionKey);
        }

        default void floorRoomHeight(String sectionKey, MKWorkspaceFloorRoomKind kind, int value) {
        }

        default int floorRoomHeightMin(String sectionKey) {
            return 3;
        }

        default int floorRoomHeightMax(String sectionKey) {
            return height(sectionKey);
        }

        default int stackWidth(String sectionKey) {
            return stackWidth();
        }

        default int stackLength(String sectionKey) {
            return stackLength();
        }
    }

    private record StackBounds(int left, int top, int bottom, int width, int maxWidth) {
    }

    private record ControlLayout(int controlX, int controlY, int controlWidth) {
    }

    private record SliderBounds(String id, int trackX, int trackY, int trackWidth) {
    }

    private record CounterBounds(String id, int minusX, int valueX, int plusX, int buttonY) {
    }

    private record ToggleBounds(int cellX, int cellY, int cellWidth, int boxX, int boxY, int labelX, int labelY) {
    }

    private record ToggleSpec(String label, String tooltip, boolean enabled, Runnable toggle) {
    }

    private record FloorPlanSegment(ButtonBounds bounds, int color, Direction direction, String label, String tooltip) {
    }

    private record FloorPlanStep(String label, int color, int major, int minor, String tooltip) {
    }

    private record ExitLayout(int maskX, int maskY, int editorX, int editorY, int editorWidth,
                              int sliderX, int sliderY, int sliderWidth) {
    }

    private record ButtonBounds(int x, int y, int width, int height) {
    }
}
