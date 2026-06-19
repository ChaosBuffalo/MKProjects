package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorLinkGenerationMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorTopologySettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHallwayLeadInMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKFloorLayoutSolver;
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
    private static final int EXIT_SELECTED = 0xFF8EC5FF;
    private static final int FLOOR_ROOT = 0xCC6EA46D;
    private static final int FLOOR_MAIN = 0xCC3FA66B;
    private static final int FLOOR_MAIN_ROOM = 0xCC4CBF7A;
    private static final int FLOOR_MAIN_CAP = 0xCC2F7D57;
    private static final int FLOOR_BRANCH = 0xCCB88A4F;
    private static final int FLOOR_ROOM = 0xCC8A73A8;
    private static final int FLOOR_CAP = 0xCCD18A50;
    private static final int FLOOR_LINK = 0xCC5FA8B8;
    private static final int COLLISION = 0xFFFF4D4D;
    private static final int STRUCTURE_RADIUS_LIMIT = 128;
    private static final int PREVIEW_SIZE = 240;
    private static final int ROOT_EXIT_CONTROLS_HEIGHT = 124;
    private static final int PATH_CONTROLS_HEIGHT = 490;
    private static final int ROOM_ROW_HEIGHT = 122;
    private static final int ROOM_SECTION_HEADER = 22;
    private static final int MASK_SIZE = 66;
    private static final int MASK_ROOM_SIZE = 18;
    private static final int MASK_ARM = 15;
    private static final int MASK_ARM_THICKNESS = 11;
    private static final int SLIDER_LABEL_WIDTH = 78;

    private enum ViewMode {
        ALL,
        PREVIEW_ONLY,
        SETTINGS_ONLY
    }

    private final String stackId;
    private final String sectionKey;
    private final Controls controls;
    private final ViewMode viewMode;
    private String draggingSlider = "";

    public MKFloorTopologyPlanPreview(int width, String stackId, String sectionKey, Controls controls) {
        this(width, stackId, sectionKey, controls, ViewMode.ALL);
    }

    private MKFloorTopologyPlanPreview(int width, String stackId, String sectionKey, Controls controls,
                                       ViewMode viewMode) {
        super(0, 0, width, heightFor(controls, sectionKey, viewMode));
        this.stackId = stackId;
        this.sectionKey = sectionKey;
        this.controls = controls;
        this.viewMode = viewMode;
    }

    public static MKFloorTopologyPlanPreview previewOnly(int width, String stackId, String sectionKey,
                                                         Controls controls) {
        return new MKFloorTopologyPlanPreview(width, stackId, sectionKey, controls, ViewMode.PREVIEW_ONLY);
    }

    public static MKFloorTopologyPlanPreview settingsOnly(int width, String stackId, String sectionKey,
                                                          Controls controls) {
        return new MKFloorTopologyPlanPreview(width, stackId, sectionKey, controls, ViewMode.SETTINGS_ONLY);
    }

    public static int heightFor(Controls controls, String sectionKey) {
        return heightFor(controls, sectionKey, ViewMode.ALL);
    }

    private static int heightFor(Controls controls, String sectionKey, ViewMode viewMode) {
        if (!controls.hasFloorTopology(sectionKey)) {
            return 0;
        }
        if (viewMode == ViewMode.PREVIEW_ONLY) {
            return 12 + PREVIEW_SIZE + 18;
        }
        int settingsHeight = ROOT_EXIT_CONTROLS_HEIGHT + PATH_CONTROLS_HEIGHT + roomSectionsHeight(controls, sectionKey);
        if (viewMode == ViewMode.SETTINGS_ONLY) {
            return 12 + settingsHeight + 18;
        }
        return 12 + PREVIEW_SIZE + settingsHeight + 18;
    }

    private static int roomSectionsHeight(Controls controls, String sectionKey) {
        int mainRows = Math.max(1, controls.roomProfiles(sectionKey, MKWorkspaceFloorRoomKind.MAIN_ROOM).size());
        int branchRows = Math.max(1, controls.roomProfiles(sectionKey, MKWorkspaceFloorRoomKind.BRANCH_ROOM).size());
        int branchCapRows = Math.max(1, controls.roomProfiles(sectionKey, MKWorkspaceFloorRoomKind.BRANCH_CAP).size());
        int approachRows = controls.floorMainCapApproachEnabled(sectionKey) ?
                Math.max(1, controls.roomProfiles(sectionKey, MKWorkspaceFloorRoomKind.MAIN_CAP_APPROACH).size()) : 0;
        int capRows = Math.max(1, controls.roomProfiles(sectionKey, MKWorkspaceFloorRoomKind.MAIN_CAP).size());
        int sectionHeaders = 4 + (controls.floorMainCapApproachEnabled(sectionKey) ? 1 : 0);
        return (ROOM_SECTION_HEADER * sectionHeaders) +
                ((mainRows + branchRows + branchCapRows + approachRows + capRows) * ROOM_ROW_HEIGHT);
    }

    @Override
    public void draw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height,
                     int mouseX, int mouseY, float partialTicks) {
        if (!controls.hasFloorTopology(sectionKey)) {
            return;
        }
        graphics.fill(x, y, x + width, y + height, BACKGROUND);
        ButtonBounds preview = previewBounds(x, y, width);
        if (drawsPreview()) {
            graphics.drawString(mc.font, "Floor Plan - " + WorkspaceTopologyUiSupport.formatTopologyLabel(sectionKey),
                    x + 8, y + 8, TEXT, false);
            drawPlan(graphics, mc, preview, mouseX, mouseY);
        }
        if (drawsSettings()) {
            int cursorY = settingsStartY(x, y, width);
            drawRootExitControls(graphics, mc, x + 8, cursorY, width - 16, mouseX, mouseY);
            cursorY += ROOT_EXIT_CONTROLS_HEIGHT;
            drawPathControls(graphics, mc, x + 8, cursorY, width - 16, mouseX, mouseY);
            cursorY += PATH_CONTROLS_HEIGHT;
            for (MKWorkspaceFloorRoomKind kind : roomKindsForUi()) {
                cursorY = drawRoomSection(graphics, mc, x + 8, cursorY, width - 16, kind, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (!controls.hasFloorTopology(sectionKey)) {
            return false;
        }
        if (drawsPreview() && handlePreviewPress(mouseX, mouseY)) {
            return true;
        }
        if (drawsSettings() && handleRootExitPress(mouseX, mouseY, mouseButton)) {
            return true;
        }
        if (drawsSettings() && handlePathControlPress(mouseX, mouseY, mouseButton)) {
            return true;
        }
        return drawsSettings() && handleRoomPress(mouseX, mouseY, mouseButton);
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
        MKFloorLayoutSolver.FloorLayoutResult result = floorLayoutResult();
        drawOutline(graphics, panel.x(), panel.y(), panel.width(), panel.y() + panel.height(),
                result.fitsHardLimit() ? CONTROL_ACTIVE : COLLISION);
        List<PlanSegment> segments = screenSegments(panel, result.segments());
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
            if (segment.colliding()) {
                drawDiagonalHatch(graphics, segment.bounds(), COLLISION);
            }
        }
        if (segments.size() <= 1) {
            graphics.drawCenteredString(mc.font, Component.literal("No horizontal floor paths"),
                    panel.x() + panel.width() / 2, panel.y() + panel.height() / 2 + 20, MUTED_TEXT);
        }
        boolean locked = controls.lockedLayoutSeed(sectionKey).isPresent();
        drawButton(graphics, mc, rerollButton(panel), "Reroll", mouseX, mouseY, locked);
        drawButton(graphics, mc, lockButton(panel), locked ? "Unlock" : "Lock", mouseX, mouseY, false);
        if (!result.fitsHardLimit()) {
            String warning = result.hasRequiredRejections() ?
                    "Required floor path rejected" :
                    "Exceeds 128 block horizontal bound";
            graphics.drawString(mc.font, warning,
                    panel.x() + panel.width() - mc.font.width(warning) - 6,
                    rerollButton(panel).y() + rerollButton(panel).height() + 4, COLLISION, false);
        }
        String rejectedText = result.rejectedExits().isEmpty() ? "" : "  rejected " + result.rejectedExits().size();
        graphics.drawString(mc.font, "sprawl " + Math.round(controls.floorSprawl(sectionKey) * 100.0f) +
                        "%  lead " + controls.effectiveHallwayLeadInPieces(sectionKey) +
                        "  ext " + Math.round(result.extents().minX()) + "," +
                        Math.round(result.extents().maxX()) + " / " +
                        Math.round(result.extents().minY()) + "," +
                        Math.round(result.extents().maxY()) + rejectedText,
                panel.x() + 5, panel.y() + panel.height() - 13, MUTED_TEXT, false);
    }

    private void drawPathControls(GuiGraphics graphics, Minecraft mc, int x, int y, int width,
                                  int mouseX, int mouseY) {
        graphics.drawString(mc.font, "Path Settings", x, y + 28, TEXT, false);
        drawCheckbox(graphics, mc, pathToggleBounds(x, y, 0), "Main Halls",
                controls.floorMainHallwaysEnabled(sectionKey), mouseX, mouseY);
        drawCheckbox(graphics, mc, pathToggleBounds(x, y, 1), "Branch Halls",
                controls.floorBranchHallwaysEnabled(sectionKey), mouseX, mouseY);
        drawCheckbox(graphics, mc, pathToggleBounds(x, y, 2), "Main Approach",
                controls.floorMainCapApproachEnabled(sectionKey), mouseX, mouseY);
        ButtonBounds modeBounds = leadModeBounds(x, y, width);
        graphics.fill(modeBounds.x(), modeBounds.y(), modeBounds.x() + modeBounds.width(),
                modeBounds.y() + modeBounds.height(),
                isInRect(mouseX, mouseY, modeBounds.x(), modeBounds.y(), modeBounds.width(), modeBounds.height()) ?
                        CONTROL_ACTIVE : CONTROL);
        graphics.drawString(mc.font, fit("Lead " +
                        WorkspaceTopologyUiSupport.formatTopologyLabel(
                                controls.floorHallwayLeadInMode(sectionKey).getSerializedName()),
                modeBounds.width() - 4), modeBounds.x() + 2, modeBounds.y() + 3, TEXT, false);
        drawSlider(graphics, mc, "Min Main", controls.floorMinMainPathPieces(sectionKey), 0, 10,
                x, y + 50, width, mouseX, mouseY, "floorMinMain");
        drawSlider(graphics, mc, "Max Main", controls.floorMaxMainPathPieces(sectionKey), 0, 10,
                x, y + 72, width, mouseX, mouseY, "floorMaxMain");
        drawSlider(graphics, mc, "Branches", controls.floorMaxBranchPiecesBeforeCap(sectionKey), 0,
                MKWorkspaceFloorTopologySettings.MAX_BRANCH_PIECES_BEFORE_CAP,
                x, y + 94, width, mouseX, mouseY, "floorBranchCap");
        drawSlider(graphics, mc, "Sprawl", Math.round(controls.floorSprawl(sectionKey) * 100.0f), 0, 100,
                x, y + 116, width, mouseX, mouseY, "floorSprawl");
        if (controls.floorHallwayLeadInMode(sectionKey) == MKWorkspaceHallwayLeadInMode.MANUAL) {
            drawSlider(graphics, mc, "Lead In", controls.floorManualHallwayLeadInPieces(sectionKey), 0,
                    MKWorkspaceFloorTopologySettings.MAX_MANUAL_HALLWAY_LEAD_IN_PIECES,
                    x, y + 138, width, mouseX, mouseY, "floorLeadIn");
        }
        int linkY = y + 164;
        graphics.drawString(mc.font, "Link Settings", x, linkY, TEXT, false);
        drawCheckbox(graphics, mc, linkToggleBounds(x, linkY), "Enable Links",
                controls.floorLinksEnabled(sectionKey), mouseX, mouseY);
        drawSlider(graphics, mc, "Density", Math.round(controls.floorLinkDensity(sectionKey) * 100.0f), 0, 100,
                x, linkY + 24, width, mouseX, mouseY, "floorLinkDensity");
        drawSlider(graphics, mc, "Max Links", controls.floorMaxLinksPerFloor(sectionKey), 0,
                MKWorkspaceFloorTopologySettings.MAX_LINKS_PER_FLOOR,
                x, linkY + 46, width, mouseX, mouseY, "floorMaxLinks");
        drawSlider(graphics, mc, "Per Room", controls.floorMaxLinksPerRoom(sectionKey), 0,
                MKWorkspaceFloorTopologySettings.MAX_LINKS_PER_ROOM,
                x, linkY + 68, width, mouseX, mouseY, "floorMaxLinksPerRoom");
        drawSlider(graphics, mc, "Length", controls.floorMaxLinkLength(sectionKey), 0,
                MKWorkspaceFloorTopologySettings.MAX_LINK_LENGTH,
                x, linkY + 90, width, mouseX, mouseY, "floorMaxLinkLength");
        ButtonBounds linkMode = linkModeBounds(x, linkY, width);
        graphics.fill(linkMode.x(), linkMode.y(), linkMode.x() + linkMode.width(),
                linkMode.y() + linkMode.height(),
                isInRect(mouseX, mouseY, linkMode.x(), linkMode.y(), linkMode.width(), linkMode.height()) ?
                        CONTROL_ACTIVE : CONTROL);
        graphics.drawString(mc.font, fit("Mode " +
                        WorkspaceTopologyUiSupport.formatTopologyLabel(
                                controls.floorLinkGenerationMode(sectionKey).getSerializedName()),
                linkMode.width() - 4), linkMode.x() + 2, linkMode.y() + 3, TEXT, false);
        drawSlider(graphics, mc, "Decay", Math.round(controls.floorLinkDecay(sectionKey) * 100.0f), 0, 100,
                x, linkY + 136, width, mouseX, mouseY, "floorLinkDecay");
        drawSlider(graphics, mc, "Endpoint", controls.floorEndpointIntactRadius(sectionKey), 0,
                MKWorkspaceFloorTopologySettings.MAX_ENDPOINT_INTACT_RADIUS,
                x, linkY + 158, width, mouseX, mouseY, "floorEndpointIntact");
        drawSlider(graphics, mc, "Mid Decay", Math.round(controls.floorMiddleDecayBonus(sectionKey) * 100.0f),
                0, 100, x, linkY + 180, width, mouseX, mouseY, "floorMiddleDecay");
        ButtonBounds insertFamily = insertFamilyBounds(x, linkY, width);
        graphics.fill(insertFamily.x(), insertFamily.y(), insertFamily.x() + insertFamily.width(),
                insertFamily.y() + insertFamily.height(),
                isInRect(mouseX, mouseY, insertFamily.x(), insertFamily.y(), insertFamily.width(),
                        insertFamily.height()) ? CONTROL_ACTIVE : CONTROL);
        String insertLabel = controls.floorInsertFamily(sectionKey).orElse("None");
        graphics.drawString(mc.font, fit("Insert " + insertLabel, insertFamily.width() - 4),
                insertFamily.x() + 2, insertFamily.y() + 3, TEXT, false);
        drawSlider(graphics, mc, "Depth", controls.floorInsertDepth(sectionKey), 1,
                MKWorkspaceFloorTopologySettings.MAX_INSERT_DEPTH,
                x, linkY + 226, width, mouseX, mouseY, "floorInsertDepth");
        drawSlider(graphics, mc, "Spacing", controls.floorInsertSpacing(sectionKey), 0,
                MKWorkspaceFloorTopologySettings.MAX_INSERT_SPACING,
                x, linkY + 248, width, mouseX, mouseY, "floorInsertSpacing");
        drawSlider(graphics, mc, "Chance", Math.round(controls.floorInsertProbability(sectionKey) * 100.0f),
                0, 100, x, linkY + 270, width, mouseX, mouseY, "floorInsertProbability");
        drawSlider(graphics, mc, "Max Decay", Math.round(controls.floorInsertMaxDecay(sectionKey) * 100.0f),
                0, 100, x, linkY + 292, width, mouseX, mouseY, "floorInsertMaxDecay");
    }

    private void drawRootExitControls(GuiGraphics graphics, Minecraft mc, int x, int y, int width,
                                      int mouseX, int mouseY) {
        graphics.drawString(mc.font, "Root Exits", x, y + 4, TEXT, false);
        ButtonBounds mask = rootExitMaskBounds(x, y);
        drawRootExitMask(graphics, mc, mask, mouseX, mouseY);
        int editorX = mask.x() + mask.width() + 10;
        Optional<MKWorkspaceFamilyHorizontalExitDefinition> selected = controls.selectedRootExit(sectionKey)
                .filter(exit -> exit.direction().getAxis().isHorizontal());
        if (selected.isEmpty()) {
            graphics.drawString(mc.font, "Left: select", editorX, mask.y() + 6, MUTED_TEXT, false);
            graphics.drawString(mc.font, "Right: branch", editorX, mask.y() + 18, MUTED_TEXT, false);
            return;
        }
        MKWorkspaceFamilyHorizontalExitDefinition exit = selected.get();
        boolean required = controls.rootExitRequired(sectionKey, exit.direction());
        graphics.drawString(mc.font, required ? "Required" : "Editable", editorX, mask.y() + 6,
                required ? EXIT_REQUIRED : SELECTED_OUTLINE, false);
        drawButton(graphics, mc, rootExitButton(editorX, mask.y(), "role"),
                "Role " + WorkspaceTopologyUiSupport.formatTopologyLabel(exit.pathKind().getSerializedName()),
                mouseX, mouseY, required);
        drawButton(graphics, mc, rootExitButton(editorX, mask.y(), "profile"),
                "Open " + exit.openingProfileId(), mouseX, mouseY, false);
        int sliderWidth = Math.max(80, width - mask.width() - 10);
        drawSlider(graphics, mc, "Side", exit.sideOffset(), controls.rootExitSideMin(sectionKey),
                controls.rootExitSideMax(sectionKey), editorX, mask.y() + 66, sliderWidth,
                required ? -1 : mouseX, required ? -1 : mouseY, "rootExitSide");
        drawSlider(graphics, mc, "Vertical", exit.verticalOffset(), 0,
                controls.rootExitVerticalMax(sectionKey), editorX, mask.y() + 88, sliderWidth,
                required ? -1 : mouseX, required ? -1 : mouseY, "rootExitVertical");
    }

    private void drawRootExitMask(GuiGraphics graphics, Minecraft mc, ButtonBounds bounds, int mouseX, int mouseY) {
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(), 0xFF1B1B1F);
        drawOutline(graphics, bounds.x(), bounds.y(), bounds.width(), bounds.y() + bounds.height(), CONTROL_ACTIVE);
        int centerX = bounds.x() + bounds.width() / 2;
        int centerY = bounds.y() + bounds.height() / 2;
        int roomLeft = centerX - MASK_ROOM_SIZE / 2;
        int roomTop = centerY - MASK_ROOM_SIZE / 2;
        int roomRight = roomLeft + MASK_ROOM_SIZE;
        int roomBottom = roomTop + MASK_ROOM_SIZE;
        for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            drawRootExitArm(graphics, direction, roomLeft, roomTop, roomRight, roomBottom, centerX, centerY,
                    mouseX, mouseY);
        }
        graphics.fill(roomLeft, roomTop, roomRight, roomBottom, 0xFF2A3440);
        drawOutline(graphics, roomLeft, roomTop, MASK_ROOM_SIZE, roomBottom, CONTROL_ACTIVE);
        graphics.drawCenteredString(mc.font, Component.literal("R"), centerX, centerY - 4, TEXT);
        drawRootExitLabel(graphics, mc, Direction.NORTH, centerX, roomTop - MASK_ARM - 10);
        drawRootExitLabel(graphics, mc, Direction.EAST, roomRight + MASK_ARM, centerY - 4);
        drawRootExitLabel(graphics, mc, Direction.SOUTH, centerX, roomBottom + MASK_ARM + 1);
        drawRootExitLabel(graphics, mc, Direction.WEST, roomLeft - MASK_ARM, centerY - 4);
    }

    private void drawRootExitArm(GuiGraphics graphics, Direction direction, int roomLeft, int roomTop,
                                 int roomRight, int roomBottom, int centerX, int centerY, int mouseX, int mouseY) {
        Optional<MKWorkspaceFamilyHorizontalExitDefinition> exit = rootExitForDirection(direction);
        boolean selected = controls.selectedRootExit(sectionKey)
                .map(value -> value.direction() == direction)
                .orElse(false);
        int color = exit.map(value -> selected ? EXIT_SELECTED :
                        controls.rootExitRequired(sectionKey, direction) ? EXIT_REQUIRED :
                                value.pathKind().usesMainPath() ? EXIT_ACTIVE : FLOOR_BRANCH)
                .orElse(EXIT_INACTIVE);
        if (hitRootExitDirection(rootExitMaskBounds(getX() + 8, settingsStartY(getX(), getY(), getWidth())),
                mouseX, mouseY) == direction) {
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

    private void drawRootExitLabel(GuiGraphics graphics, Minecraft mc, Direction direction, int x, int y) {
        String label = direction.getName().substring(0, 1).toUpperCase();
        int color = MUTED_TEXT;
        Optional<MKWorkspaceFamilyHorizontalExitDefinition> exit = rootExitForDirection(direction);
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
            color = controls.rootExitRequired(sectionKey, direction) ? EXIT_REQUIRED : TEXT;
        }
        graphics.drawCenteredString(mc.font, Component.literal(label), x, y, color);
    }

    private int drawRoomSection(GuiGraphics graphics, Minecraft mc, int x, int y, int width,
                                MKWorkspaceFloorRoomKind kind, int mouseX, int mouseY) {
        String label = roomSectionLabel(kind);
        int color = roomSectionColor(kind);
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
        if (kind == MKWorkspaceFloorRoomKind.MAIN_ROOM) {
            drawCheckbox(graphics, mc, roomRandomizeMainExitButton(x, y), "Randomize Main Exit",
                    profile.randomizeMainExit(), mouseX, mouseY);
        }
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
                (profile.optionalBranchExitDirection(direction) ||
                        profile.mainExitDirection(direction) ||
                        profile.linkCandidateExitDirection(direction))) {
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
                case LINK_CANDIDATE -> "L";
                case VERTICAL_ACCESS -> "";
            };
            color = profile.requiredExitDirection(direction) ? EXIT_REQUIRED : TEXT;
        }
        graphics.drawCenteredString(mc.font, Component.literal(label), x, y, color);
    }

    private MKFloorLayoutSolver.FloorLayoutResult floorLayoutResult() {
        int padding = controls.layoutFootprintPadding();
        return new MKFloorLayoutSolver().solve(previewSettings(),
                controls.stackWidth(sectionKey) + padding,
                controls.stackLength(sectionKey) + padding,
                controls.rootExits(sectionKey),
                controls.effectiveHallwayLeadInPieces(sectionKey),
                controls.previewSeed(sectionKey));
    }

    private MKWorkspaceFloorTopologySettings previewSettings() {
        int padding = controls.layoutFootprintPadding();
        int leadIn = controls.effectiveHallwayLeadInPieces(sectionKey);
        int hallwayWidth = Math.max(3, 3 + padding);
        return new MKWorkspaceFloorTopologySettings(
                stackId,
                sectionKey,
                controls.floorMinMainPathPieces(sectionKey),
                controls.floorMaxMainPathPieces(sectionKey),
                controls.floorMaxBranchPiecesBeforeCap(sectionKey),
                controls.floorHallwayLeadInMode(sectionKey),
                controls.floorManualHallwayLeadInPieces(sectionKey),
                controls.floorMainHallwaysEnabled(sectionKey),
                controls.floorBranchHallwaysEnabled(sectionKey),
                controls.floorMainCapApproachEnabled(sectionKey),
                controls.floorSprawl(sectionKey),
                controls.floorLinksEnabled(sectionKey),
                controls.floorLinkDensity(sectionKey),
                controls.floorMaxLinksPerFloor(sectionKey),
                controls.floorMaxLinksPerRoom(sectionKey),
                controls.floorMaxLinkLength(sectionKey),
                controls.lockedLayoutSeed(sectionKey),
                Optional.empty(),
                controls.roomProfiles(sectionKey, MKWorkspaceFloorRoomKind.MAIN_ROOM),
                controls.roomProfiles(sectionKey, MKWorkspaceFloorRoomKind.BRANCH_ROOM),
                controls.roomProfiles(sectionKey, MKWorkspaceFloorRoomKind.BRANCH_CAP),
                controls.roomProfiles(sectionKey, MKWorkspaceFloorRoomKind.MAIN_CAP_APPROACH),
                controls.roomProfiles(sectionKey, MKWorkspaceFloorRoomKind.MAIN_CAP)
        ).withPhysicalFootprintPadding(padding)
                .withLayoutHallwayFootprints(leadIn + padding, hallwayWidth, leadIn + padding, hallwayWidth);
    }

    private List<PlanSegment> planSegments(ButtonBounds panel) {
        return screenSegments(panel, floorLayoutResult().segments());
    }

    private void addLogicalPath(List<LogicalSegment> segments, LogicalRect start, Direction direction,
                                boolean main, int branchDepth) {
        int leadIn = controls.effectiveHallwayLeadInPieces(sectionKey);
        boolean hallwaysEnabled = main ? controls.floorMainHallwaysEnabled(sectionKey) :
                controls.floorBranchHallwaysEnabled(sectionKey);
        int hallwayLength = Math.max(1, leadIn);
        LogicalRect cursor = start;
        ArrayList<RoomStep> roomSteps = new ArrayList<>();
        if (main) {
            List<MKWorkspaceFloorRoomProfile> mainProfiles = controls.roomProfiles(sectionKey,
                    MKWorkspaceFloorRoomKind.MAIN_ROOM);
            int roomCount = Math.max(0, controls.floorMaxMainPathPieces(sectionKey));
            for (int i = 0; i < roomCount; i++) {
                roomSteps.add(new RoomStep(mainProfiles.get(i % mainProfiles.size()), "M" + (i + 1), FLOOR_ROOM,
                        "main room " + (i + 1) + " of max " + roomCount, true));
            }
            if (controls.floorMainCapApproachEnabled(sectionKey)) {
                MKWorkspaceFloorRoomProfile approach = controls.roomProfiles(sectionKey,
                        MKWorkspaceFloorRoomKind.MAIN_CAP_APPROACH).getFirst();
                roomSteps.add(new RoomStep(approach, "Approach", FLOOR_MAIN_CAP, "main cap approach", false));
            }
            MKWorkspaceFloorRoomProfile cap = controls.roomProfiles(sectionKey, MKWorkspaceFloorRoomKind.MAIN_CAP)
                    .getFirst();
            roomSteps.add(new RoomStep(cap, "Main Cap", FLOOR_MAIN_CAP, "terminal main cap", false));
        } else {
            List<MKWorkspaceFloorRoomProfile> branchProfiles = controls.roomProfiles(sectionKey,
                    MKWorkspaceFloorRoomKind.BRANCH_ROOM);
            int roomCount = Math.max(0, controls.floorMaxBranchPiecesBeforeCap(sectionKey));
            for (int i = 0; i < roomCount; i++) {
                roomSteps.add(new RoomStep(branchProfiles.get(i % branchProfiles.size()), "B" + (i + 1), FLOOR_ROOM,
                        "branch room " + (i + 1) + " of max " + roomCount, true));
            }
            MKWorkspaceFloorRoomProfile branchCap = controls.roomProfiles(sectionKey, MKWorkspaceFloorRoomKind.BRANCH_CAP)
                    .getFirst();
            roomSteps.add(new RoomStep(branchCap, "Branch Cap", FLOOR_CAP, "terminal branch cap", false));
        }
        for (int i = 0; i < roomSteps.size(); i++) {
            RoomStep step = roomSteps.get(i);
            if (hallwaysEnabled) {
                cursor = appendHallway(segments, cursor, direction, hallwayLength, main, i == 0);
            }
            cursor = appendRoom(segments, cursor, direction, step);
            if (step.allowBranches()) {
                addBranchesFromRoom(segments, cursor, direction, step.profile(), branchDepth);
            }
        }
    }

    private LogicalRect appendHallway(List<LogicalSegment> segments, LogicalRect cursor, Direction direction,
                                      int hallwayLength, boolean main, boolean leadIn) {
        MKWorkspaceFloorRoomProfile reference = controls.roomProfiles(sectionKey,
                main ? MKWorkspaceFloorRoomKind.MAIN_ROOM : MKWorkspaceFloorRoomKind.BRANCH_ROOM).getFirst();
        int hallwayMinor = main ? Math.max(3, Math.min(7, reference.width() / 2)) :
                Math.max(3, Math.min(5, reference.width() / 2));
        LogicalRect rect = rectAfter(cursor, direction, hallwayLength, hallwayMinor);
        String label = main ? "Main Hall" : "Branch Hall";
        segments.add(new LogicalSegment(rect, main ? FLOOR_MAIN : FLOOR_BRANCH, direction, label,
                label + "\n" + formatDirection(direction) +
                        (leadIn ? "\nlead-in " + hallwayLength : "\nlink " + hallwayLength)));
        return rect;
    }

    private LogicalRect appendRoom(List<LogicalSegment> segments, LogicalRect cursor, Direction direction,
                                   RoomStep step) {
        int major = verticalMajor(step.profile(), direction);
        int minor = verticalMinor(step.profile(), direction);
        LogicalRect rect = rectAfter(cursor, direction, major, minor);
        segments.add(new LogicalSegment(rect, step.color(), direction, step.label(),
                step.profile().label() + "\n" + step.profile().width() + " x " + step.profile().length() +
                        "\nheight " + step.profile().height() + "\n" + step.tooltip(), true));
        return rect;
    }

    private void addBranchesFromRoom(List<LogicalSegment> segments, LogicalRect room, Direction pathDirection,
                                     MKWorkspaceFloorRoomProfile profile, int branchDepth) {
        if (branchDepth >= 1) {
            return;
        }
        for (MKWorkspaceFamilyHorizontalExitDefinition exit : profile.horizontalExits()) {
            if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH &&
                    exit.direction() != Direction.SOUTH) {
                Direction branchDirection = rotateRoomExit(exit.direction(), pathDirection);
                addLogicalPath(segments, room, branchDirection, false, branchDepth + 1);
            }
        }
    }

    private Direction rotateRoomExit(Direction localDirection, Direction pathDirection) {
        if (pathDirection == Direction.NORTH) {
            return localDirection;
        }
        if (pathDirection == Direction.SOUTH) {
            return localDirection.getOpposite();
        }
        if (pathDirection == Direction.EAST) {
            return switch (localDirection) {
                case NORTH -> Direction.EAST;
                case EAST -> Direction.SOUTH;
                case SOUTH -> Direction.WEST;
                case WEST -> Direction.NORTH;
                default -> localDirection;
            };
        }
        if (pathDirection == Direction.WEST) {
            return switch (localDirection) {
                case NORTH -> Direction.WEST;
                case EAST -> Direction.NORTH;
                case SOUTH -> Direction.EAST;
                case WEST -> Direction.SOUTH;
                default -> localDirection;
            };
        }
        return localDirection;
    }

    private LogicalRect rectAfter(LogicalRect previous, Direction direction, int major, int minor) {
        float width = direction == Direction.EAST || direction == Direction.WEST ? major : minor;
        float height = direction == Direction.NORTH || direction == Direction.SOUTH ? major : minor;
        float distance = previous.major(direction) / 2.0f + major / 2.0f + 1.0f;
        return new LogicalRect(
                previous.centerX() + directionX(direction) * distance,
                previous.centerY() + directionY(direction) * distance,
                width,
                height
        );
    }

    private List<PlanSegment> screenSegments(ButtonBounds panel, List<MKFloorLayoutSolver.LogicalSegment> logicalSegments) {
        if (logicalSegments.isEmpty()) {
            return List.of();
        }
        int plotX = panel.x() + 6;
        int plotY = panel.y() + 6;
        int plotW = Math.max(1, panel.width() - 12);
        int plotH = Math.max(1, panel.height() - 22);
        float minX = -STRUCTURE_RADIUS_LIMIT;
        float minY = -STRUCTURE_RADIUS_LIMIT;
        float span = STRUCTURE_RADIUS_LIMIT * 2.0f;
        float scale = Math.min(plotW / span, plotH / span);
        float offsetX = (plotW - span * scale) / 2.0f;
        float offsetY = (plotH - span * scale) / 2.0f;
        ArrayList<PlanSegment> segments = new ArrayList<>();
        for (MKFloorLayoutSolver.LogicalSegment segment : logicalSegments) {
            int rawX = plotX + Math.round(offsetX + (segment.rect().left() - minX) * scale);
            int rawY = plotY + Math.round(offsetY + (segment.rect().top() - minY) * scale);
            int rawWidth = Math.max(2, Math.round(segment.rect().width() * scale));
            int rawHeight = Math.max(2, Math.round(segment.rect().height() * scale));
            int x = Math.max(plotX, rawX);
            int y = Math.max(plotY, rawY);
            int right = Math.min(plotX + plotW, rawX + rawWidth);
            int bottom = Math.min(plotY + plotH, rawY + rawHeight);
            int width = right - x;
            int height = bottom - y;
            if (width <= 0 || height <= 0) {
                continue;
            }
            if (width > 4) {
                x += 1;
                width -= 2;
            }
            if (height > 4) {
                y += 1;
                height -= 2;
            }
            segments.add(new PlanSegment(new ButtonBounds(x, y, width, height), segmentColor(segment.kind()),
                    segment.direction(), segment.label(), segment.tooltip(), false));
        }
        return List.copyOf(segments);
    }

    private int segmentColor(MKFloorLayoutSolver.SegmentKind kind) {
        return switch (kind) {
            case ROOT -> FLOOR_ROOT;
            case MAIN_HALL -> FLOOR_MAIN;
            case BRANCH_HALL -> FLOOR_BRANCH;
            case MAIN_ROOM -> FLOOR_MAIN_ROOM;
            case BRANCH_ROOM -> FLOOR_ROOM;
            case MAIN_CAP -> FLOOR_MAIN_CAP;
            case BRANCH_CAP -> FLOOR_CAP;
            case LINK_HALL -> FLOOR_LINK;
        };
    }

    private List<LogicalSegment> collidingRoomSegments(List<LogicalSegment> logicalSegments) {
        ArrayList<LogicalSegment> colliding = new ArrayList<>();
        List<LogicalSegment> rooms = logicalSegments.stream()
                .filter(LogicalSegment::roomRegion)
                .toList();
        for (int i = 0; i < rooms.size(); i++) {
            for (int j = i + 1; j < rooms.size(); j++) {
                if (rooms.get(i).rect().intersects(rooms.get(j).rect())) {
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

    private float directionX(Direction direction) {
        return switch (direction) {
            case EAST -> 1.0f;
            case WEST -> -1.0f;
            default -> 0.0f;
        };
    }

    private float directionY(Direction direction) {
        return switch (direction) {
            case SOUTH -> 1.0f;
            case NORTH -> -1.0f;
            default -> 0.0f;
        };
    }

    private boolean handlePathControlPress(double mouseX, double mouseY, int mouseButton) {
        int y = pathStartY(getX(), getY(), getWidth());
        int x = getX() + 8;
        int width = getWidth() - 16;
        if (isInRect(mouseX, mouseY, pathToggleBounds(x, y, 0))) {
            controls.floorMainHallwaysEnabled(sectionKey, !controls.floorMainHallwaysEnabled(sectionKey));
            return true;
        }
        if (isInRect(mouseX, mouseY, pathToggleBounds(x, y, 1))) {
            controls.floorBranchHallwaysEnabled(sectionKey, !controls.floorBranchHallwaysEnabled(sectionKey));
            return true;
        }
        if (isInRect(mouseX, mouseY, pathToggleBounds(x, y, 2))) {
            controls.floorMainCapApproachEnabled(sectionKey, !controls.floorMainCapApproachEnabled(sectionKey));
            return true;
        }
        if (isInSlider(mouseX, mouseY, sliderBounds(x, y + 50, width, "floorMinMain"))) {
            draggingSlider = "floorMinMain";
            applySliderValue(mouseX);
            return true;
        }
        if (isInSlider(mouseX, mouseY, sliderBounds(x, y + 72, width, "floorMaxMain"))) {
            draggingSlider = "floorMaxMain";
            applySliderValue(mouseX);
            return true;
        }
        if (isInSlider(mouseX, mouseY, sliderBounds(x, y + 94, width, "floorBranchCap"))) {
            draggingSlider = "floorBranchCap";
            applySliderValue(mouseX);
            return true;
        }
        if (isInSlider(mouseX, mouseY, sliderBounds(x, y + 116, width, "floorSprawl"))) {
            draggingSlider = "floorSprawl";
            applySliderValue(mouseX);
            return true;
        }
        ButtonBounds modeBounds = leadModeBounds(x, y, width);
        if (isInRect(mouseX, mouseY, modeBounds.x(), modeBounds.y(), modeBounds.width(), modeBounds.height())) {
            controls.cycleFloorHallwayLeadInMode(sectionKey, WorkspaceTopologyUiSupport.isReverseClick(mouseButton));
            return true;
        }
        if (controls.floorHallwayLeadInMode(sectionKey) == MKWorkspaceHallwayLeadInMode.MANUAL &&
                isInSlider(mouseX, mouseY, sliderBounds(x, y + 138, width, "floorLeadIn"))) {
            draggingSlider = "floorLeadIn";
            applySliderValue(mouseX);
            return true;
        }
        int linkY = y + 164;
        if (isInRect(mouseX, mouseY, linkToggleBounds(x, linkY))) {
            controls.floorLinksEnabled(sectionKey, !controls.floorLinksEnabled(sectionKey));
            return true;
        }
        if (isInRect(mouseX, mouseY, linkModeBounds(x, linkY, width))) {
            controls.cycleFloorLinkGenerationMode(sectionKey, WorkspaceTopologyUiSupport.isReverseClick(mouseButton));
            return true;
        }
        if (isInRect(mouseX, mouseY, insertFamilyBounds(x, linkY, width))) {
            controls.cycleFloorInsertFamily(sectionKey, WorkspaceTopologyUiSupport.isReverseClick(mouseButton));
            return true;
        }
        for (String slider : List.of("floorLinkDensity", "floorMaxLinks", "floorMaxLinksPerRoom",
                "floorMaxLinkLength", "floorLinkDecay", "floorEndpointIntact", "floorMiddleDecay",
                "floorInsertDepth", "floorInsertSpacing", "floorInsertProbability", "floorInsertMaxDecay")) {
            int sliderY = switch (slider) {
                case "floorLinkDensity" -> linkY + 24;
                case "floorMaxLinks" -> linkY + 46;
                case "floorMaxLinksPerRoom" -> linkY + 68;
                case "floorMaxLinkLength" -> linkY + 90;
                case "floorLinkDecay" -> linkY + 136;
                case "floorEndpointIntact" -> linkY + 158;
                case "floorMiddleDecay" -> linkY + 180;
                case "floorInsertDepth" -> linkY + 226;
                case "floorInsertSpacing" -> linkY + 248;
                case "floorInsertProbability" -> linkY + 270;
                default -> linkY + 292;
            };
            if (isInSlider(mouseX, mouseY, sliderBounds(x, sliderY, width, slider))) {
                draggingSlider = slider;
                applySliderValue(mouseX);
                return true;
            }
        }
        return false;
    }

    private boolean handleRootExitPress(double mouseX, double mouseY, int mouseButton) {
        int y = settingsStartY(getX(), getY(), getWidth());
        int x = getX() + 8;
        ButtonBounds mask = rootExitMaskBounds(x, y);
        Direction direction = hitRootExitDirection(mask, (int) mouseX, (int) mouseY);
        if (direction != null) {
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                controls.selectRootExit(sectionKey, direction);
                return true;
            }
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT && !controls.rootExitRequired(sectionKey, direction)) {
                controls.toggleRootExit(sectionKey, direction);
                return true;
            }
            return true;
        }
        Optional<MKWorkspaceFamilyHorizontalExitDefinition> selected = controls.selectedRootExit(sectionKey)
                .filter(exit -> exit.direction().getAxis().isHorizontal());
        if (selected.isEmpty()) {
            return false;
        }
        int editorX = mask.x() + mask.width() + 10;
        boolean required = controls.rootExitRequired(sectionKey, selected.get().direction());
        if (isInRect(mouseX, mouseY, rootExitButton(editorX, mask.y(), "role"))) {
            if (!required) {
                controls.cycleRootExitPathKind(sectionKey, WorkspaceTopologyUiSupport.isReverseClick(mouseButton));
            }
            return true;
        }
        if (isInRect(mouseX, mouseY, rootExitButton(editorX, mask.y(), "profile"))) {
            controls.cycleRootExitOpeningProfile(sectionKey, WorkspaceTopologyUiSupport.isReverseClick(mouseButton));
            return true;
        }
        int sliderWidth = Math.max(80, getWidth() - 16 - mask.width() - 10);
        if (!required && isInSlider(mouseX, mouseY, sliderBounds(editorX, mask.y() + 66, sliderWidth,
                "rootExitSide"))) {
            draggingSlider = "rootExitSide";
            applySliderValue(mouseX);
            return true;
        }
        if (!required && isInSlider(mouseX, mouseY, sliderBounds(editorX, mask.y() + 88, sliderWidth,
                "rootExitVertical"))) {
            draggingSlider = "rootExitVertical";
            applySliderValue(mouseX);
            return true;
        }
        return false;
    }

    private boolean handlePreviewPress(double mouseX, double mouseY) {
        ButtonBounds preview = previewBounds(getX(), getY(), getWidth());
        if (isInRect(mouseX, mouseY, rerollButton(preview))) {
            controls.rerollPreviewSeed(sectionKey);
            return true;
        }
        if (isInRect(mouseX, mouseY, lockButton(preview))) {
            if (controls.lockedLayoutSeed(sectionKey).isPresent()) {
                controls.unlockLayoutSeed(sectionKey);
            } else {
                controls.lockLayoutSeed(sectionKey);
            }
            return true;
        }
        return false;
    }

    private boolean handleRoomPress(double mouseX, double mouseY, int mouseButton) {
        int cursorY = roomStartY(getX(), getY(), getWidth());
        for (MKWorkspaceFloorRoomKind kind : roomKindsForUi()) {
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
        if (kind == MKWorkspaceFloorRoomKind.MAIN_ROOM &&
                isInRect(mouseX, mouseY, roomRandomizeMainExitButton(x, y))) {
            controls.setRoomRandomizeMainExit(sectionKey, kind, index, !profile.randomizeMainExit());
            return true;
        }
        Direction direction = hitRoomExitDirection(x + width - MASK_SIZE - 8, y + 22, (int) mouseX, (int) mouseY);
        if (direction != null) {
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_MIDDLE &&
                    profile.linkCandidateExitDirection(direction)) {
                controls.toggleRoomLinkCandidateExit(sectionKey, kind, index, direction);
            } else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT && kind.hasMainExit() &&
                    profile.mainExitDirection(direction)) {
                controls.setRoomMainExitDirection(sectionKey, kind, index, direction);
            } else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT &&
                    profile.optionalBranchExitDirection(direction)) {
                controls.toggleRoomBranchExit(sectionKey, kind, index, direction);
            } else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT && kind.hasMainExit() &&
                    currentMainExitDirection(profile, direction)) {
                alternateMainExitDirection(direction)
                        .ifPresent(alternate -> controls.setRoomMainExitDirection(sectionKey, kind, index, alternate));
                controls.toggleRoomBranchExit(sectionKey, kind, index, direction);
            } else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT && !kind.hasMainExit() &&
                    profile.optionalBranchExitDirection(direction)) {
                controls.toggleRoomBranchExit(sectionKey, kind, index, direction);
            } else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT &&
                    !profile.optionalBranchExitDirection(direction) &&
                    profile.linkCandidateExitDirection(direction)) {
                controls.toggleRoomLinkCandidateExit(sectionKey, kind, index, direction);
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
        } else if ("rootExitSide".equals(draggingSlider)) {
            controls.rootExitSideOffset(sectionKey, sliderValue(mouseX, bounds,
                    controls.rootExitSideMin(sectionKey), controls.rootExitSideMax(sectionKey)));
        } else if ("rootExitVertical".equals(draggingSlider)) {
            controls.rootExitVerticalOffset(sectionKey, sliderValue(mouseX, bounds, 0,
                    controls.rootExitVerticalMax(sectionKey)));
        } else if ("floorMaxMain".equals(draggingSlider)) {
            controls.floorMaxMainPathPieces(sectionKey, sliderValue(mouseX, bounds, 0, 10));
        } else if ("floorBranchCap".equals(draggingSlider)) {
            controls.floorMaxBranchPiecesBeforeCap(sectionKey, sliderValue(mouseX, bounds, 0,
                    MKWorkspaceFloorTopologySettings.MAX_BRANCH_PIECES_BEFORE_CAP));
        } else if ("floorSprawl".equals(draggingSlider)) {
            controls.floorSprawl(sectionKey, sliderValue(mouseX, bounds, 0, 100) / 100.0f);
        } else if ("floorLeadIn".equals(draggingSlider)) {
            controls.floorManualHallwayLeadInPieces(sectionKey, sliderValue(mouseX, bounds, 0,
                    MKWorkspaceFloorTopologySettings.MAX_MANUAL_HALLWAY_LEAD_IN_PIECES));
        } else if ("floorLinkDensity".equals(draggingSlider)) {
            controls.floorLinkDensity(sectionKey, sliderValue(mouseX, bounds, 0, 100) / 100.0f);
        } else if ("floorMaxLinks".equals(draggingSlider)) {
            controls.floorMaxLinksPerFloor(sectionKey, sliderValue(mouseX, bounds, 0,
                    MKWorkspaceFloorTopologySettings.MAX_LINKS_PER_FLOOR));
        } else if ("floorMaxLinksPerRoom".equals(draggingSlider)) {
            controls.floorMaxLinksPerRoom(sectionKey, sliderValue(mouseX, bounds, 0,
                    MKWorkspaceFloorTopologySettings.MAX_LINKS_PER_ROOM));
        } else if ("floorMaxLinkLength".equals(draggingSlider)) {
            controls.floorMaxLinkLength(sectionKey, sliderValue(mouseX, bounds, 0,
                    MKWorkspaceFloorTopologySettings.MAX_LINK_LENGTH));
        } else if ("floorLinkDecay".equals(draggingSlider)) {
            controls.floorLinkDecay(sectionKey, sliderValue(mouseX, bounds, 0, 100) / 100.0f);
        } else if ("floorEndpointIntact".equals(draggingSlider)) {
            controls.floorEndpointIntactRadius(sectionKey, sliderValue(mouseX, bounds, 0,
                    MKWorkspaceFloorTopologySettings.MAX_ENDPOINT_INTACT_RADIUS));
        } else if ("floorMiddleDecay".equals(draggingSlider)) {
            controls.floorMiddleDecayBonus(sectionKey, sliderValue(mouseX, bounds, 0, 100) / 100.0f);
        } else if ("floorInsertDepth".equals(draggingSlider)) {
            controls.floorInsertDepth(sectionKey, sliderValue(mouseX, bounds, 1,
                    MKWorkspaceFloorTopologySettings.MAX_INSERT_DEPTH));
        } else if ("floorInsertSpacing".equals(draggingSlider)) {
            controls.floorInsertSpacing(sectionKey, sliderValue(mouseX, bounds, 0,
                    MKWorkspaceFloorTopologySettings.MAX_INSERT_SPACING));
        } else if ("floorInsertProbability".equals(draggingSlider)) {
            controls.floorInsertProbability(sectionKey, sliderValue(mouseX, bounds, 0, 100) / 100.0f);
        } else if ("floorInsertMaxDecay".equals(draggingSlider)) {
            controls.floorInsertMaxDecay(sectionKey, sliderValue(mouseX, bounds, 0, 100) / 100.0f);
        } else if (draggingSlider.startsWith("room:")) {
            String[] parts = draggingSlider.split(":");
            MKWorkspaceFloorRoomKind kind = roomKindFromSlider(parts[1]);
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
        if (drawsPreview() && isInRect(mouseX, mouseY, preview.x(), preview.y(), preview.width(), preview.height())) {
            if (isInRect(mouseX, mouseY, rerollButton(preview))) {
                return controls.lockedLayoutSeed(sectionKey).isPresent() ?
                        Optional.of("Reroll sample\nUnlock the sample before rerolling") :
                        Optional.of("Reroll sample\nKeeps the authored settings and changes this preview outcome");
            }
            if (isInRect(mouseX, mouseY, lockButton(preview))) {
                return controls.lockedLayoutSeed(sectionKey).isPresent() ?
                        Optional.of("Unlock sample\nRuntime generation returns to seeded variation") :
                        Optional.of("Lock sample\nExports this preview seed for runtime generation");
            }
            return planSegments(preview).stream()
                    .filter(segment -> isInRect(mouseX, mouseY, segment.bounds().x(), segment.bounds().y(),
                            segment.bounds().width(), segment.bounds().height()))
                    .findFirst()
                    .map(PlanSegment::tooltip)
                    .or(() -> Optional.of("Floor Plan\nTop-down generation preview"));
        }
        if (!drawsSettings()) {
            return Optional.empty();
        }
        int cursorY = roomStartY(x, y, width);
        for (MKWorkspaceFloorRoomKind kind : roomKindsForUi()) {
            cursorY += ROOM_SECTION_HEADER;
            List<MKWorkspaceFloorRoomProfile> profiles = controls.roomProfiles(sectionKey, kind);
            for (int index = 0; index < profiles.size(); index++) {
                if (kind == MKWorkspaceFloorRoomKind.MAIN_ROOM &&
                        isInRect(mouseX, mouseY, roomRandomizeMainExitButton(x, cursorY))) {
                    return Optional.of("Randomize Main Exit\nRuntime may choose any enabled outgoing main or branch " +
                            "direction as this room's main path exit");
                }
                Direction direction = hitRoomExitDirection(x + width - 16 - MASK_SIZE, cursorY + 22, mouseX, mouseY);
                if (direction != null) {
                    MKWorkspaceFloorRoomProfile profile = profiles.get(index);
                    if (profile.requiredExitDirection(direction)) {
                        return Optional.of(formatDirection(direction) + "\nRequired " +
                                requiredExitLabel(profile, direction));
                    }
                    if (profile.kind().hasMainExit() &&
                            profile.mainExitDirection(direction)) {
                        boolean current = currentMainExitDirection(profile, direction);
                        return Optional.of(formatDirection(direction) +
                                (current ? "\nMain path exit" : "\nLeft click: set main path exit") +
                                (profile.optionalBranchExitDirection(direction) ?
                                        "\nRight click: toggle branch exit" :
                                        current ? "\nRight click: move main exit and toggle branch" : ""));
                    }
                    if (profile.optionalBranchExitDirection(direction)) {
                        return Optional.of(formatDirection(direction) +
                                "\nOptional branch exit\nClick to toggle\nMiddle click: toggle link candidate");
                    }
                    if (profile.linkCandidateExitDirection(direction)) {
                        return Optional.of(formatDirection(direction) +
                                "\nLink candidate exit\nMiddle click to toggle");
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
        int x = getX() + 8;
        int width = getWidth() - 16;
        int pathY = pathStartY(getX(), getY(), getWidth());
        if ("rootExitSide".equals(id) || "rootExitVertical".equals(id)) {
            ButtonBounds mask = rootExitMaskBounds(getX() + 8, settingsStartY(getX(), getY(), getWidth()));
            int editorX = mask.x() + mask.width() + 10;
            int sliderWidth = Math.max(80, getWidth() - 16 - mask.width() - 10);
            return sliderBounds(editorX, mask.y() + ("rootExitSide".equals(id) ? 66 : 88), sliderWidth, id);
        }
        if ("floorMinMain".equals(id)) {
            return sliderBounds(x, pathY + 50, width, id);
        }
        if ("floorMaxMain".equals(id)) {
            return sliderBounds(x, pathY + 72, width, id);
        }
        if ("floorBranchCap".equals(id)) {
            return sliderBounds(x, pathY + 94, width, id);
        }
        if ("floorSprawl".equals(id)) {
            return sliderBounds(x, pathY + 116, width, id);
        }
        if ("floorLeadIn".equals(id)) {
            return sliderBounds(x, pathY + 138, width, id);
        }
        if ("floorLinkDensity".equals(id)) {
            return sliderBounds(x, pathY + 188, width, id);
        }
        if ("floorMaxLinks".equals(id)) {
            return sliderBounds(x, pathY + 210, width, id);
        }
        if ("floorMaxLinksPerRoom".equals(id)) {
            return sliderBounds(x, pathY + 232, width, id);
        }
        if ("floorMaxLinkLength".equals(id)) {
            return sliderBounds(x, pathY + 254, width, id);
        }
        if ("floorLinkDecay".equals(id)) {
            return sliderBounds(x, pathY + 300, width, id);
        }
        if ("floorEndpointIntact".equals(id)) {
            return sliderBounds(x, pathY + 322, width, id);
        }
        if ("floorMiddleDecay".equals(id)) {
            return sliderBounds(x, pathY + 344, width, id);
        }
        if ("floorInsertDepth".equals(id)) {
            return sliderBounds(x, pathY + 390, width, id);
        }
        if ("floorInsertSpacing".equals(id)) {
            return sliderBounds(x, pathY + 412, width, id);
        }
        if ("floorInsertProbability".equals(id)) {
            return sliderBounds(x, pathY + 434, width, id);
        }
        if ("floorInsertMaxDecay".equals(id)) {
            return sliderBounds(x, pathY + 456, width, id);
        }
        if (!id.startsWith("room:")) {
            return null;
        }
        String[] parts = id.split(":");
        MKWorkspaceFloorRoomKind targetKind = roomKindFromSlider(parts[1]);
        int targetIndex = Integer.parseInt(parts[2]);
        String field = parts[3];
        int cursorY = roomStartY(getX(), getY(), getWidth());
        for (MKWorkspaceFloorRoomKind kind : roomKindsForUi()) {
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

    private boolean currentMainExitDirection(MKWorkspaceFloorRoomProfile profile, Direction direction) {
        return profile.mainExitDirection()
                .map(direction::equals)
                .orElse(false);
    }

    private Optional<Direction> alternateMainExitDirection(Direction current) {
        return List.of(Direction.NORTH, Direction.EAST, Direction.WEST).stream()
                .filter(direction -> direction != current)
                .findFirst();
    }

    private ButtonBounds previewBounds(int x, int y, int width) {
        int size = Math.min(PREVIEW_SIZE, Math.max(80, width - 16));
        return new ButtonBounds(x + (width - size) / 2, y + 24, size, size);
    }

    private boolean drawsPreview() {
        return viewMode != ViewMode.SETTINGS_ONLY;
    }

    private boolean drawsSettings() {
        return viewMode != ViewMode.PREVIEW_ONLY;
    }

    private int settingsStartY(int x, int y, int width) {
        if (drawsPreview()) {
            ButtonBounds preview = previewBounds(x, y, width);
            return preview.y() + preview.height() + 8;
        }
        return y + 8;
    }

    private int roomStartY(int x, int y, int width) {
        return pathStartY(x, y, width) + PATH_CONTROLS_HEIGHT;
    }

    private int pathStartY(int x, int y, int width) {
        return settingsStartY(x, y, width) + ROOT_EXIT_CONTROLS_HEIGHT;
    }

    private ButtonBounds rerollButton(ButtonBounds preview) {
        return new ButtonBounds(preview.x() + preview.width() - 108, preview.y() + 5, 48, 16);
    }

    private ButtonBounds lockButton(ButtonBounds preview) {
        return new ButtonBounds(preview.x() + preview.width() - 56, preview.y() + 5, 50, 16);
    }

    private ButtonBounds roomAddButton(int x, int y, int width, MKWorkspaceFloorRoomKind kind) {
        return new ButtonBounds(x + width - 46, y + 2, 46, 16);
    }

    private ButtonBounds roomRemoveButton(int x, int y, int width, MKWorkspaceFloorRoomKind kind, int index) {
        return new ButtonBounds(x + width - 58, y + 4, 54, 16);
    }

    private ButtonBounds roomRandomizeMainExitButton(int x, int y) {
        return new ButtonBounds(x + 5, y + 86, 150, 16);
    }

    private ButtonBounds rootExitMaskBounds(int x, int y) {
        return new ButtonBounds(x, y + 20, MASK_SIZE, MASK_SIZE);
    }

    private ButtonBounds rootExitButton(int x, int y, String id) {
        return switch (id) {
            case "role" -> new ButtonBounds(x, y + 20, 132, 18);
            case "profile" -> new ButtonBounds(x, y + 42, 132, 18);
            default -> new ButtonBounds(x, y, 132, 18);
        };
    }

    private ButtonBounds pathToggleBounds(int x, int y, int index) {
        return new ButtonBounds(x + index * 112, y + 4, 106, 16);
    }

    private ButtonBounds linkToggleBounds(int x, int y) {
        return new ButtonBounds(x + 96, y - 2, 118, 16);
    }

    private ButtonBounds leadModeBounds(int x, int y, int width) {
        return new ButtonBounds(x + Math.max(0, width - 150), y + 26, Math.min(150, width), 16);
    }

    private ButtonBounds linkModeBounds(int x, int y, int width) {
        return new ButtonBounds(x, y + 112, width, 16);
    }

    private ButtonBounds insertFamilyBounds(int x, int y, int width) {
        return new ButtonBounds(x, y + 204, width, 16);
    }

    private void drawCheckbox(GuiGraphics graphics, Minecraft mc, ButtonBounds bounds, String label,
                              boolean checked, int mouseX, int mouseY) {
        boolean hovered = isInRect(mouseX, mouseY, bounds);
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(),
                hovered ? CONTROL_ACTIVE : CONTROL);
        int boxX = bounds.x() + 3;
        int boxY = bounds.y() + 3;
        graphics.fill(boxX, boxY, boxX + 10, boxY + 10, checked ? SELECTED_OUTLINE : 0xFF1B1B1F);
        drawOutline(graphics, boxX, boxY, 10, boxY + 10, CONTROL_ACTIVE);
        graphics.drawString(mc.font, fit(label, bounds.width() - 18), bounds.x() + 16, bounds.y() + 4, TEXT, false);
    }

    private Optional<MKWorkspaceFamilyHorizontalExitDefinition> rootExitForDirection(Direction direction) {
        return controls.rootExits(sectionKey).stream()
                .filter(exit -> exit.direction() == direction)
                .findFirst();
    }

    private Direction hitRootExitDirection(ButtonBounds bounds, int mouseX, int mouseY) {
        int centerX = bounds.x() + bounds.width() / 2;
        int centerY = bounds.y() + bounds.height() / 2;
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

    private List<MKWorkspaceFloorRoomKind> roomKindsForUi() {
        ArrayList<MKWorkspaceFloorRoomKind> kinds = new ArrayList<>();
        kinds.add(MKWorkspaceFloorRoomKind.MAIN_ROOM);
        kinds.add(MKWorkspaceFloorRoomKind.BRANCH_ROOM);
        kinds.add(MKWorkspaceFloorRoomKind.BRANCH_CAP);
        if (controls.floorMainCapApproachEnabled(sectionKey)) {
            kinds.add(MKWorkspaceFloorRoomKind.MAIN_CAP_APPROACH);
        }
        kinds.add(MKWorkspaceFloorRoomKind.MAIN_CAP);
        return List.copyOf(kinds);
    }

    private String roomSectionLabel(MKWorkspaceFloorRoomKind kind) {
        return switch (kind) {
            case MAIN_ROOM -> "Main Rooms";
            case BRANCH_ROOM -> "Branch Rooms";
            case BRANCH_CAP -> "Branch Caps";
            case MAIN_CAP_APPROACH -> "Main Cap Approaches";
            case MAIN_CAP -> "Main Caps";
        };
    }

    private int roomSectionColor(MKWorkspaceFloorRoomKind kind) {
        return switch (kind) {
            case MAIN_ROOM -> FLOOR_MAIN;
            case BRANCH_ROOM, BRANCH_CAP -> FLOOR_BRANCH;
            case MAIN_CAP_APPROACH, MAIN_CAP -> FLOOR_MAIN_CAP;
        };
    }

    private MKWorkspaceFloorRoomKind roomKindFromSlider(String value) {
        for (MKWorkspaceFloorRoomKind kind : MKWorkspaceFloorRoomKind.values()) {
            if (kind.getSerializedName().equals(value)) {
                return kind;
            }
        }
        return MKWorkspaceFloorRoomKind.MAIN_ROOM;
    }

    private int verticalMajor(MKWorkspaceFloorRoomProfile profile, Direction direction) {
        return direction == Direction.NORTH || direction == Direction.SOUTH ? profile.length() : profile.width();
    }

    private int verticalMinor(MKWorkspaceFloorRoomProfile profile, Direction direction) {
        return direction == Direction.NORTH || direction == Direction.SOUTH ? profile.width() : profile.length();
    }

    private float fitScale(List<PlanStep> steps, int availableSpan, int availableCross) {
        int desiredMajor = steps.stream().mapToInt(PlanStep::major).sum() + Math.max(0, steps.size() - 1) * 2;
        int desiredMinor = steps.stream().mapToInt(PlanStep::minor).max().orElse(1);
        float majorScale = availableSpan / (float) Math.max(1, desiredMajor);
        float minorScale = availableCross / (float) Math.max(1, desiredMinor);
        return Math.max(0.2f, Math.min(majorScale, minorScale));
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

    private String roomSliderId(MKWorkspaceFloorRoomKind kind, int index, String field) {
        return "room:" + kind.getSerializedName() + ":" + index + ":" +
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

    private int maxCrossSpan(ButtonBounds panel, ButtonBounds root, Direction direction) {
        int padding = 10;
        return switch (direction) {
            case EAST, WEST -> panel.height() - padding * 2;
            case NORTH, SOUTH -> panel.width() - padding * 2;
            default -> Math.max(root.width(), root.height());
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
        int trackX = x + SLIDER_LABEL_WIDTH;
        return new SliderBounds(id, trackX, y + 6, Math.max(24, width - SLIDER_LABEL_WIDTH - 6));
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

        Optional<MKWorkspaceFamilyHorizontalExitDefinition> selectedRootExit(String sectionKey);

        boolean rootExitRequired(String sectionKey, Direction direction);

        void selectRootExit(String sectionKey, Direction direction);

        void toggleRootExit(String sectionKey, Direction direction);

        void cycleRootExitPathKind(String sectionKey, boolean reverse);

        void cycleRootExitOpeningProfile(String sectionKey, boolean reverse);

        int rootExitSideMin(String sectionKey);

        int rootExitSideMax(String sectionKey);

        int rootExitVerticalMax(String sectionKey);

        void rootExitSideOffset(String sectionKey, int value);

        void rootExitVerticalOffset(String sectionKey, int value);

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

        boolean floorMainHallwaysEnabled(String sectionKey);

        void floorMainHallwaysEnabled(String sectionKey, boolean value);

        boolean floorBranchHallwaysEnabled(String sectionKey);

        void floorBranchHallwaysEnabled(String sectionKey, boolean value);

        boolean floorMainCapApproachEnabled(String sectionKey);

        void floorMainCapApproachEnabled(String sectionKey, boolean value);

        float floorSprawl(String sectionKey);

        void floorSprawl(String sectionKey, float value);

        boolean floorLinksEnabled(String sectionKey);

        void floorLinksEnabled(String sectionKey, boolean value);

        float floorLinkDensity(String sectionKey);

        void floorLinkDensity(String sectionKey, float value);

        int floorMaxLinksPerFloor(String sectionKey);

        void floorMaxLinksPerFloor(String sectionKey, int value);

        int floorMaxLinksPerRoom(String sectionKey);

        void floorMaxLinksPerRoom(String sectionKey, int value);

        int floorMaxLinkLength(String sectionKey);

        void floorMaxLinkLength(String sectionKey, int value);

        MKWorkspaceFloorLinkGenerationMode floorLinkGenerationMode(String sectionKey);

        void cycleFloorLinkGenerationMode(String sectionKey, boolean reverse);

        float floorLinkDecay(String sectionKey);

        void floorLinkDecay(String sectionKey, float value);

        int floorEndpointIntactRadius(String sectionKey);

        void floorEndpointIntactRadius(String sectionKey, int value);

        float floorMiddleDecayBonus(String sectionKey);

        void floorMiddleDecayBonus(String sectionKey, float value);

        List<String> floorInsertFamilyIds();

        Optional<String> floorInsertFamily(String sectionKey);

        void cycleFloorInsertFamily(String sectionKey, boolean reverse);

        int floorInsertDepth(String sectionKey);

        void floorInsertDepth(String sectionKey, int value);

        int floorInsertSpacing(String sectionKey);

        void floorInsertSpacing(String sectionKey, int value);

        float floorInsertProbability(String sectionKey);

        void floorInsertProbability(String sectionKey, float value);

        float floorInsertMaxDecay(String sectionKey);

        void floorInsertMaxDecay(String sectionKey, float value);

        long previewSeed(String sectionKey);

        void rerollPreviewSeed(String sectionKey);

        Optional<Long> lockedLayoutSeed(String sectionKey);

        void lockLayoutSeed(String sectionKey);

        void unlockLayoutSeed(String sectionKey);

        int recommendedHallwayLeadInPieces(String sectionKey);

        default int effectiveHallwayLeadInPieces(String sectionKey) {
            return floorHallwayLeadInMode(sectionKey) == MKWorkspaceHallwayLeadInMode.MANUAL ?
                    floorManualHallwayLeadInPieces(sectionKey) :
                    recommendedHallwayLeadInPieces(sectionKey);
        }

        default int layoutFootprintPadding() {
            return 0;
        }

        int floorRoomHeightMin(String sectionKey);

        int floorRoomHeightMax(String sectionKey);

        List<MKWorkspaceFloorRoomProfile> roomProfiles(String sectionKey, MKWorkspaceFloorRoomKind kind);

        void floorRoomWidth(String sectionKey, MKWorkspaceFloorRoomKind kind, int index, int value);

        void floorRoomLength(String sectionKey, MKWorkspaceFloorRoomKind kind, int index, int value);

        void floorRoomHeight(String sectionKey, MKWorkspaceFloorRoomKind kind, int index, int value);

        void addRoomProfile(String sectionKey, MKWorkspaceFloorRoomKind kind);

        void removeRoomProfile(String sectionKey, MKWorkspaceFloorRoomKind kind, int index);

        void setRoomMainExitDirection(String sectionKey, MKWorkspaceFloorRoomKind kind, int index,
                                      Direction direction);

        void setRoomRandomizeMainExit(String sectionKey, MKWorkspaceFloorRoomKind kind, int index, boolean value);

        void toggleRoomBranchExit(String sectionKey, MKWorkspaceFloorRoomKind kind, int index, Direction direction);

        void toggleRoomLinkCandidateExit(String sectionKey, MKWorkspaceFloorRoomKind kind, int index,
                                         Direction direction);
    }

    private record ButtonBounds(int x, int y, int width, int height) {
    }

    private record SliderBounds(String id, int trackX, int trackY, int trackWidth) {
    }

    private record PlanSegment(ButtonBounds bounds, int color, Direction direction, String label, String tooltip,
                               boolean colliding) {
    }

    private record PlanStep(String label, int color, int major, int minor, String tooltip) {
    }

    private record LogicalSegment(LogicalRect rect, int color, Direction direction, String label, String tooltip,
                                  boolean roomRegion) {
        private LogicalSegment(LogicalRect rect, int color, Direction direction, String label, String tooltip) {
            this(rect, color, direction, label, tooltip, false);
        }
    }

    private record LogicalRect(float centerX, float centerY, float width, float height) {
        private float left() {
            return centerX - width / 2.0f;
        }

        private float right() {
            return centerX + width / 2.0f;
        }

        private float top() {
            return centerY - height / 2.0f;
        }

        private float bottom() {
            return centerY + height / 2.0f;
        }

        private float major(Direction direction) {
            return direction == Direction.EAST || direction == Direction.WEST ? width : height;
        }

        private boolean intersects(LogicalRect other) {
            return left() < other.right() && right() > other.left() &&
                    top() < other.bottom() && bottom() > other.top();
        }
    }

    private record RoomStep(MKWorkspaceFloorRoomProfile profile, String label, int color, String tooltip,
                            boolean allowBranches) {
    }
}
