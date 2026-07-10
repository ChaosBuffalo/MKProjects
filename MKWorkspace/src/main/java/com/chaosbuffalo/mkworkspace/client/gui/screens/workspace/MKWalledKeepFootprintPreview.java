package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWalledKeepPlannerSettings;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWalledKeepWorkspacePlanner;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWalledKeepSizingReport;
import com.chaosbuffalo.mkwidgets.client.gui.instructions.HoveringTextInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.screens.IMKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MKWalledKeepFootprintPreview extends MKWidget {
    private static final int BACKGROUND = 0x99101010;
    private static final int GRID = 0x44373737;
    private static final int CAP_OK = 0xAA446C8E;
    private static final int FOOTPRINT_OK = 0xCC6EA46D;
    private static final int FOOTPRINT_ERROR = 0xCCE06B65;
    private static final int WALL = 0xCC8B8E91;
    private static final int CORNER_TOWER = 0xDDA0A5AA;
    private static final int CENTER_KEEP = 0xDDA6B7C7;
    private static final int PATH = 0xDDBC985A;
    private static final int SLOT = 0xCC5EA6A0;
    private static final int GATE = 0xDDD18A50;
    private static final int ORIGIN = 0xFFE5E5E5;
    private static final int TEXT = 0xFFE0E0E0;
    private static final int MUTED_TEXT = 0xFFB8B8B8;
    private static final int MIN_PIXEL_GAP = 1;
    private static final String PERIMETER_ROOT_SLOT = "keep.perimeter";
    private static final String GATEHOUSE_SLOT = "keep.gate.main";
    private static final String ENTRY_APPROACH_SLOT = "keep.entry_approach.main";
    private static final String COURTYARD_PATH_SLOT = "keep.courtyard.path";
    private static final String COURTYARD_CONTENT_SLOT = "keep.courtyard.content";
    private static final String WALKWAY_WEST_SLOT = "keep.walkway.west";
    private static final String WALKWAY_EAST_SLOT = "keep.walkway.east";

    private final MKStructureWorkspace workspace;
    private final MKWalledKeepSizingReport report;
    private final Consumer<NavigationTarget> navigationCallback;
    private List<PreviewElement> previewElements = List.of();

    public MKWalledKeepFootprintPreview(int width, int height, MKStructureWorkspace workspace,
                                        MKWalledKeepSizingReport report) {
        this(width, height, workspace, report, null);
    }

    public MKWalledKeepFootprintPreview(int width, int height, MKStructureWorkspace workspace,
                                        MKWalledKeepSizingReport report,
                                        Consumer<NavigationTarget> navigationCallback) {
        super(0, 0, width, height);
        this.workspace = workspace;
        this.report = report;
        this.navigationCallback = navigationCallback;
        setLongHoverTicks(5);
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (navigationCallback == null) {
            return false;
        }
        java.util.Optional<PreviewElement> hovered = hoveredElement((int) mouseX, (int) mouseY);
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT && hovered.isEmpty()) {
            navigationCallback.accept(NavigationTarget.back());
            return true;
        }
        if (mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        return hovered
                .map(PreviewElement::target)
                .filter(target -> target.type() != NavigationTargetType.NONE)
                .map(target -> {
                    navigationCallback.accept(target);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public void draw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height,
                     int mouseX, int mouseY, float partialTicks) {
        fillRect(graphics, x, y, width, height, BACKGROUND);

        int labelHeight = mc.font.lineHeight + 4;
        int plotSize = Math.max(96, Math.min(width - 12, height - labelHeight - 12));
        int plotX = x + (width - plotSize) / 2;
        int plotY = y + 6;
        int plotCenterX = plotX + plotSize / 2;
        int plotCenterY = plotY + plotSize / 2;
        int cap = Math.max(1, report.maxDistanceFromCenter());
        double scale = plotSize / (double) (cap * 2);
        ArrayList<PreviewElement> elements = new ArrayList<>();

        drawGrid(graphics, plotX, plotY, plotSize);
        drawOutline(graphics, plotX, plotY, plotSize, plotSize, CAP_OK);

        int footprintColor = report.fitsJigsawCap() ? FOOTPRINT_OK : FOOTPRINT_ERROR;
        int footprintLeft = worldX(plotCenterX, scale, -report.westDistance());
        int footprintRight = worldX(plotCenterX, scale, report.eastDistance());
        int footprintTop = worldY(plotCenterY, scale, -report.northDistance());
        int footprintBottom = worldY(plotCenterY, scale, report.southDistance());
        drawOutline(graphics, footprintLeft, footprintTop,
                Math.max(1, footprintRight - footprintLeft),
                Math.max(1, footprintBottom - footprintTop), footprintColor);

        drawKeepElements(graphics, elements, plotCenterX, plotCenterY, scale,
                new Rect(footprintLeft, footprintTop,
                        Math.max(1, footprintRight - footprintLeft),
                        Math.max(1, footprintBottom - footprintTop)));
        drawOrigin(graphics, plotCenterX, plotCenterY);
        previewElements = List.copyOf(elements);

        String status = report.fitsJigsawCap() ? "fits" : "too large";
        graphics.drawString(mc.font,
                "Footprint " + report.footprintWidth() + "x" + report.footprintLength() +
                        "  " + report.requiredJigsawRadius() + "/" + report.maxDistanceFromCenter() +
                        "  " + status,
                x + 6, y + height - labelHeight, TEXT, false);
        graphics.drawString(mc.font, "N", plotCenterX - 3, plotY + 2, MUTED_TEXT, false);
        graphics.drawString(mc.font, "S", plotCenterX - 3, plotY + plotSize - mc.font.lineHeight - 2,
                MUTED_TEXT, false);
        graphics.drawString(mc.font, "W", plotX + 2, plotCenterY - mc.font.lineHeight / 2,
                MUTED_TEXT, false);
        graphics.drawString(mc.font, "E", plotX + plotSize - 8, plotCenterY - mc.font.lineHeight / 2,
                MUTED_TEXT, false);
    }

    @Override
    public void longHoverDraw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height,
                              int mouseX, int mouseY, float partialTicks) {
        IMKScreen screen = getScreen();
        if (screen == null) {
            return;
        }
        Component tooltip = hoveredElement(mouseX, mouseY)
                .map(element -> Component.literal(element.tooltip()))
                .orElseGet(() -> Component.literal("Top-down footprint preview\n" +
                        "N " + report.northDistance() + " S " + report.southDistance() +
                        " W " + report.westDistance() + " E " + report.eastDistance() + "\n" +
                        "required " + report.requiredJigsawRadius() +
                        " / cap " + report.maxDistanceFromCenter()));
        Vec2i parentPos = getParentCoords(new Vec2i(mouseX, mouseY));
        screen.addPostRenderInstruction(new HoveringTextInstruction(tooltip, parentPos));
    }

    private java.util.Optional<PreviewElement> hoveredElement(int mouseX, int mouseY) {
        for (int index = previewElements.size() - 1; index >= 0; index--) {
            PreviewElement element = previewElements.get(index);
            if (element.rect().contains(mouseX, mouseY)) {
                return java.util.Optional.of(element);
            }
        }
        return java.util.Optional.empty();
    }

    private void drawKeepElements(GuiGraphics graphics, List<PreviewElement> elements,
                                  int originX, int originY, double scale, Rect footprintRect) {
        int centerWidth = verticalStack("keep.center").width();
        int centerLength = verticalStack("keep.center").length();
        Rect centerRect = worldRect(originX, originY, scale,
                -centerWidth / 2.0, -centerLength / 2.0, centerWidth, centerLength);

        int wallPassageWidth = wallPassageWidth();
        int wallBodyWidth = wallPassageWidth + (2 * workspace.shellMargin());
        int wallWidth = Math.max(2, px(scale, wallBodyWidth));
        drawOutlineThick(graphics,
                footprintRect.x(), footprintRect.y(), footprintRect.width(), footprintRect.height(),
                wallWidth, WALL);
        elements.add(new PreviewElement("Perimeter wall",
                "Perimeter wall\nfront segments " + report.frontBranchSegments() + "+" +
                        report.frontBranchSegments() + "\nside segments " + report.verticalWallSegments() +
                        "\nback segments " + report.backWallSegments() +
                        "\ncourtyard required " + report.courtyardRequiredHorizontalSpan() + "x" +
                        report.courtyardRequiredVerticalSpan() +
                        "\ncourtyard realized " + report.courtyardRealizedHorizontalSpan() + "x" +
                        report.courtyardRealizedVerticalSpan() +
                        "\nunit span " + wallUnitSpan() +
                        "\nrecommended span " + report.recommendedWallUnitSpan() +
                        "\npassage " + wallPassageWidth +
                        "\nbody width " + wallBodyWidth,
                footprintRect, NavigationTarget.templateSlot(PERIMETER_ROOT_SLOT)));

        drawCornerTowers(graphics, elements, footprintRect, scale);

        int pathSize = Math.max(centerWidth, report.courtyardPathSize());
        int pathThickness = Math.max(2, px(scale, courtyardPathWidth()));
        Rect pathRect = worldRect(originX, originY, scale,
                -pathSize / 2.0, -pathSize / 2.0, pathSize, pathSize);
        pathRect = expandAroundPoint(pathRect, centerRect, originX, originY,
                pathThickness + MIN_PIXEL_GAP);
        drawOutlineThick(graphics, pathRect.x(), pathRect.y(), pathRect.width(), pathRect.height(),
                pathThickness, PATH);
        elements.add(new PreviewElement("Courtyard path loop",
                "Courtyard path loop\n" + pathSize + " x " + pathSize + " blocks",
                pathRect, NavigationTarget.templateSlot(COURTYARD_PATH_SLOT)));

        int entryLength = report.entryApproachLength();
        int entryWidth = entryApproachWidth();
        Rect entryRect = worldRect(originX, originY, scale,
                -entryWidth / 2.0, centerLength / 2.0, entryWidth, entryLength);
        entryRect = pushBelow(entryRect, centerRect, MIN_PIXEL_GAP);
        drawElement(graphics, elements, entryRect, PATH, "Entry path",
                "Entry path\n" + entryWidth + " x " + entryLength + " blocks",
                NavigationTarget.templateSlot(ENTRY_APPROACH_SLOT));

        int gateWidth = gatehouseWidth();
        int gateLength = gatehouseLength();
        Rect gateRect = worldRect(originX, originY, scale,
                -gateWidth / 2.0, centerLength / 2.0 + entryLength, gateWidth, gateLength);
        gateRect = pushBelow(gateRect, entryRect, MIN_PIXEL_GAP);
        drawElement(graphics, elements, gateRect, GATE, "Gatehouse",
                "Gatehouse\n" + gateWidth + " x " + gateLength + " blocks",
                NavigationTarget.templateSlot(GATEHOUSE_SLOT));

        drawCourtyardSlots(graphics, elements, originX, originY, scale, pathRect, centerRect);
        drawElement(graphics, elements, centerRect, CENTER_KEEP, "Center keep",
                "Center keep\n" + centerWidth + " x " + centerLength + " blocks",
                NavigationTarget.plannerStack("keep.center"));
    }

    private void drawCornerTowers(GuiGraphics graphics, List<PreviewElement> elements, Rect footprintRect,
                                  double scale) {
        MKWorkspaceVerticalStackSettings corner = verticalStack(cornerStackId("keep.corner.north_west"));
        int cornerWidth = Math.max(4, px(scale, corner.width()));
        int cornerHeight = Math.max(4, px(scale, corner.length()));
        drawElement(graphics, elements,
                new Rect(footprintRect.x(), footprintRect.y(), cornerWidth, cornerHeight),
                CORNER_TOWER, "NW corner tower", cornerTooltip("NW", corner),
                NavigationTarget.plannerStack(cornerStackId("keep.corner.north_west")));
        corner = verticalStack(cornerStackId("keep.corner.north_east"));
        cornerWidth = Math.max(4, px(scale, corner.width()));
        cornerHeight = Math.max(4, px(scale, corner.length()));
        drawElement(graphics, elements,
                new Rect(footprintRect.right() - cornerWidth, footprintRect.y(), cornerWidth, cornerHeight),
                CORNER_TOWER, "NE corner tower", cornerTooltip("NE", corner),
                NavigationTarget.plannerStack(cornerStackId("keep.corner.north_east")));
        corner = verticalStack(cornerStackId("keep.corner.south_east"));
        cornerWidth = Math.max(4, px(scale, corner.width()));
        cornerHeight = Math.max(4, px(scale, corner.length()));
        drawElement(graphics, elements,
                new Rect(footprintRect.right() - cornerWidth, footprintRect.bottom() - cornerHeight,
                        cornerWidth, cornerHeight),
                CORNER_TOWER, "SE corner tower", cornerTooltip("SE", corner),
                NavigationTarget.plannerStack(cornerStackId("keep.corner.south_east")));
        corner = verticalStack(cornerStackId("keep.corner.south_west"));
        cornerWidth = Math.max(4, px(scale, corner.width()));
        cornerHeight = Math.max(4, px(scale, corner.length()));
        drawElement(graphics, elements,
                new Rect(footprintRect.x(), footprintRect.bottom() - cornerHeight, cornerWidth, cornerHeight),
                CORNER_TOWER, "SW corner tower", cornerTooltip("SW", corner),
                NavigationTarget.plannerStack(cornerStackId("keep.corner.south_west")));
    }

    private String cornerStackId(String topologySlotId) {
        return MKWalledKeepPlannerSettings.from(workspace.topologyProfile()).uniqueCornerTower(topologySlotId) ?
                topologySlotId : "keep.corner.shared";
    }

    private String cornerTooltip(String cornerLabel, MKWorkspaceVerticalStackSettings corner) {
        return cornerLabel + " corner tower\n" + corner.width() + " x " + corner.length() + " blocks";
    }

    private void drawCourtyardSlots(GuiGraphics graphics, List<PreviewElement> elements,
                                    int originX, int originY, double scale, Rect pathRect, Rect centerRect) {
        int socketSize = Math.max(3, report.courtyardRequestedSocketSize());
        int slotPx = Math.max(3, px(scale, socketSize));
        int maxSlotPx = Math.max(3, Math.min(pathRect.width(), pathRect.height()) - (2 * MIN_PIXEL_GAP));
        slotPx = Math.min(slotPx, maxSlotPx);
        int offset = Math.max(slotPx + MIN_PIXEL_GAP, pathRect.width() / 2 + slotPx / 2 + MIN_PIXEL_GAP);
        drawSlot(graphics, elements, "NW courtyard socket", "keep.courtyard.north_west",
                originX - offset, originY - offset, slotPx, socketSize);
        drawSlot(graphics, elements, "N courtyard socket", "keep.courtyard.north",
                originX, originY - offset, slotPx, socketSize);
        drawSlot(graphics, elements, "NE courtyard socket", "keep.courtyard.north_east",
                originX + offset, originY - offset, slotPx, socketSize);
        drawSlot(graphics, elements, "W courtyard socket", "keep.courtyard.west",
                originX - offset, originY, slotPx, socketSize);
        drawSlot(graphics, elements, "E courtyard socket", "keep.courtyard.east",
                originX + offset, originY, slotPx, socketSize);
        drawSlot(graphics, elements, "SW courtyard socket", "keep.courtyard.south_west",
                originX - offset, originY + offset, slotPx, socketSize);
        drawSlot(graphics, elements, "SE courtyard socket", "keep.courtyard.south_east",
                originX + offset, originY + offset, slotPx, socketSize);
    }

    private void drawSlot(GuiGraphics graphics, List<PreviewElement> elements, String name, String topologySlotId,
                          int centerX, int centerY, int slotPx, int socketSize) {
        Rect rect = new Rect(centerX - slotPx / 2, centerY - slotPx / 2, slotPx, slotPx);
        drawElement(graphics, elements, rect, SLOT, name,
                name + "\n" + socketSize + " x " + socketSize + " blocks",
                NavigationTarget.templateSlot(COURTYARD_CONTENT_SLOT));
    }

    private int wallUnitSpan() {
        return perimeterFamily()
                .map(MKWorkspaceLinearRunFamilyDefinition::length)
                .orElse(MKWalledKeepWorkspacePlanner.DEFAULT_WALL_SEGMENT_LENGTH);
    }

    private int wallPassageWidth() {
        return perimeterFamily()
                .map(MKWorkspaceLinearRunFamilyDefinition::interiorWidth)
                .orElse(3);
    }

    private int gatehouseWidth() {
        return gatehouseFamily()
                .map(MKWorkspaceRoomFamilyDefinition::roomWidth)
                .orElse(wallUnitSpan());
    }

    private int gatehouseLength() {
        return gatehouseFamily()
                .map(MKWorkspaceRoomFamilyDefinition::roomLength)
                .orElse(wallPassageWidth());
    }

    private int entryApproachWidth() {
        return linearRunFamily(ENTRY_APPROACH_SLOT)
                .map(MKWorkspaceLinearRunFamilyDefinition::interiorWidth)
                .orElse(3);
    }

    private int courtyardPathWidth() {
        return linearRunFamily(WALKWAY_WEST_SLOT)
                .or(() -> linearRunFamily(WALKWAY_EAST_SLOT))
                .map(MKWorkspaceLinearRunFamilyDefinition::interiorWidth)
                .orElse(3);
    }

    private java.util.Optional<MKWorkspaceLinearRunFamilyDefinition> perimeterFamily() {
        return linearRunFamily(PERIMETER_ROOT_SLOT)
                .or(() -> workspace.linearRunFamilies().stream()
                        .filter(linearRun -> linearRun.topologySlotId().startsWith(PERIMETER_ROOT_SLOT + "."))
                        .findFirst());
    }

    private java.util.Optional<MKWorkspaceLinearRunFamilyDefinition> linearRunFamily(String topologySlotId) {
        return workspace.linearRunFamilies().stream()
                .filter(linearRun -> linearRun.topologySlotId().equals(topologySlotId))
                .findFirst();
    }

    private java.util.Optional<MKWorkspaceRoomFamilyDefinition> gatehouseFamily() {
        return workspace.familyDefinitions().stream()
                .filter(family -> family.topologySlotId().equals(GATEHOUSE_SLOT))
                .findFirst();
    }

    private MKWorkspaceVerticalStackSettings verticalStack(String stackId) {
        return workspace.topologyProfile().verticalStackSettingsOrDefault(stackId);
    }

    private void drawGrid(GuiGraphics graphics, int x, int y, int size) {
        for (int i = 1; i < 4; i++) {
            int pos = x + (size * i) / 4;
            fillRect(graphics, pos, y, 1, size, GRID);
        }
        for (int i = 1; i < 4; i++) {
            int pos = y + (size * i) / 4;
            fillRect(graphics, x, pos, size, 1, GRID);
        }
    }

    private void drawOrigin(GuiGraphics graphics, int x, int y) {
        fillRect(graphics, x - 2, y - 2, 5, 5, ORIGIN);
    }

    private Rect worldRect(int originX, int originY, double scale,
                           double worldX, double worldY, double worldWidth, double worldHeight) {
        int x = worldX(originX, scale, worldX);
        int y = worldY(originY, scale, worldY);
        int width = Math.max(1, px(scale, worldWidth));
        int height = Math.max(1, px(scale, worldHeight));
        return new Rect(x, y, width, height);
    }

    private void drawElement(GuiGraphics graphics, List<PreviewElement> elements, Rect rect,
                             int color, String name, String tooltip) {
        drawElement(graphics, elements, rect, color, name, tooltip, NavigationTarget.none());
    }

    private void drawElement(GuiGraphics graphics, List<PreviewElement> elements, Rect rect,
                             int color, String name, String tooltip, NavigationTarget target) {
        fillRect(graphics, rect.x(), rect.y(), rect.width(), rect.height(), color);
        elements.add(new PreviewElement(name, tooltip, rect, target));
    }

    private Rect expandAroundPoint(Rect rect, Rect centerRect, int anchorX, int anchorY, int minGap) {
        int requiredWidth = centerRect.width() + (2 * minGap);
        int requiredHeight = centerRect.height() + (2 * minGap);
        if (rect.width() >= requiredWidth && rect.height() >= requiredHeight) {
            return rect;
        }
        int width = Math.max(rect.width(), requiredWidth);
        int height = Math.max(rect.height(), requiredHeight);
        return new Rect(anchorX - width / 2, anchorY - height / 2, width, height);
    }

    private Rect pushBelow(Rect rect, Rect blockingRect, int minGap) {
        if (rect.y() > blockingRect.bottom() + minGap || rect.x() >= blockingRect.right() ||
                rect.right() <= blockingRect.x()) {
            return rect;
        }
        return new Rect(rect.x(), blockingRect.bottom() + minGap, rect.width(), rect.height());
    }

    private int worldX(int originX, double scale, double worldX) {
        return originX + (int) Math.round(worldX * scale);
    }

    private int worldY(int originY, double scale, double worldY) {
        return originY + (int) Math.round(worldY * scale);
    }

    private int px(double scale, double blocks) {
        return (int) Math.round(blocks * scale);
    }

    private void drawOutline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        drawOutlineThick(graphics, x, y, width, height, 1, color);
    }

    private void drawOutlineThick(GuiGraphics graphics, int x, int y, int width, int height, int thickness, int color) {
        fillRect(graphics, x, y, width, thickness, color);
        fillRect(graphics, x, y + height - thickness, width, thickness, color);
        fillRect(graphics, x, y, thickness, height, color);
        fillRect(graphics, x + width - thickness, y, thickness, height, color);
    }

    private void fillRect(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + height, color);
    }

    public enum NavigationTargetType {
        NONE,
        BACK,
        PLANNER_STACK,
        TEMPLATE_SLOT
    }

    public record NavigationTarget(NavigationTargetType type, String id) {
        public static NavigationTarget none() {
            return new NavigationTarget(NavigationTargetType.NONE, "");
        }

        public static NavigationTarget back() {
            return new NavigationTarget(NavigationTargetType.BACK, "");
        }

        public static NavigationTarget plannerStack(String stackId) {
            return new NavigationTarget(NavigationTargetType.PLANNER_STACK, stackId);
        }

        public static NavigationTarget templateSlot(String topologySlotId) {
            return new NavigationTarget(NavigationTargetType.TEMPLATE_SLOT, topologySlotId);
        }
    }

    private record PreviewElement(String name, String tooltip, Rect rect, NavigationTarget target) {
    }

    private record Rect(int x, int y, int width, int height) {
        private int right() {
            return x + width;
        }

        private int bottom() {
            return y + height;
        }

        private int centerX() {
            return x + width / 2;
        }

        private int centerY() {
            return y + height / 2;
        }

        private boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX < right() && mouseY >= y && mouseY < bottom();
        }
    }
}
