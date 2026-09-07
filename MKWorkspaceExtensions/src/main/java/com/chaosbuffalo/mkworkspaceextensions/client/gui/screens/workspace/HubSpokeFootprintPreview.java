package com.chaosbuffalo.mkworkspaceextensions.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner.HubSpokePlanner;
import com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner.HubSpokePlannerSettings;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import net.minecraft.core.Direction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HubSpokeFootprintPreview extends MKWidget {
    private static final int BACKGROUND = 0x99101010;
    private static final int CENTER = 0xDDA6B7C7;
    private static final int SPOKE = 0xCC6EA46D;
    private static final int CORNER = 0xDDBC985A;
    private static final int GRID = 0x44373737;
    private static final int CAP = 0xAA446C8E;
    private static final int FOOTPRINT = 0xCC6EA46D;
    private static final int TEXT = 0xFFE0E0E0;
    private static final int MUTED_TEXT = 0xFFB8B8B8;
    private static final int STRUCTURE_RADIUS_LIMIT = 128;

    private final MKStructureWorkspace workspace;

    private record Rect(double left, double top, double width, double height) {
        double right() {
            return left + width;
        }

        double bottom() {
            return top + height;
        }
    }

    private record CornerRect(Rect rect, String suffix) {
    }

    public HubSpokeFootprintPreview(int width, int height, MKStructureWorkspace workspace) {
        super(0, 0, width, height);
        this.workspace = workspace;
    }

    @Override
    public void draw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height,
                     int mouseX, int mouseY, float partialTicks) {
        graphics.fill(x, y, x + width, y + height, BACKGROUND);
        int labelHeight = mc.font.lineHeight + 4;
        int plotSize = Math.max(96, Math.min(width - 12, height - labelHeight - 12));
        int plotX = x + (width - plotSize) / 2;
        int plotY = y + 6;
        int originX = plotX + plotSize / 2;
        int originY = plotY + plotSize / 2;
        drawGrid(graphics, plotX, plotY, plotSize);

        int centerSize = familySize(HubSpokePlanner.CENTER_SLOT, 15);
        int spokeWidth = familyWidth(HubSpokePlanner.SPOKE_SLOT, 5);
        int cornerWidth = familyWidth(HubSpokePlanner.CORNER_SLOT, 7);
        int cornerLength = familyLength(HubSpokePlanner.CORNER_SLOT, 7);
        HubSpokePlannerSettings settings = HubSpokePlannerSettings.from(workspace.topologyProfile());
        Map<Direction, HubSpokePlannerSettings.SpokeTemplate> assignments = settings.templateAssignments();
        Map<String, Direction> cornerAnchors = HubSpokePlanner.cornerAnchorDirections(assignments.keySet());
        HubSpokePlanner.CornerAttachmentMode attachmentMode =
                HubSpokePlanner.cornerAttachmentMode(centerSize, spokeWidth, cornerWidth);
        Rect center = centerRect(centerSize);
        List<Rect> spokes = spokeRects(centerSize, spokeWidth, assignments);
        List<CornerRect> corners = cornerRects(centerSize, spokeWidth, assignments, cornerAnchors,
                cornerWidth, cornerLength, attachmentMode);
        Rect footprint = bounds(center, spokes, corners);
        int footprintWidth = (int) Math.ceil(footprint.width());
        int footprintLength = (int) Math.ceil(footprint.height());
        int requiredRadius = (int) Math.ceil(Math.max(
                Math.max(Math.abs(footprint.left()), Math.abs(footprint.right())),
                Math.max(Math.abs(footprint.top()), Math.abs(footprint.bottom()))));
        double scale = (plotSize - 12) / (double) (STRUCTURE_RADIUS_LIMIT * 2);

        int capSize = px(scale, STRUCTURE_RADIUS_LIMIT * 2);
        drawOutline(graphics, originX - capSize / 2, originY - capSize / 2, capSize, capSize, CAP);
        drawOutline(graphics, world(originX, scale, footprint.left()), world(originY, scale, footprint.top()),
                px(scale, footprintWidth), px(scale, footprintLength), FOOTPRINT);

        fillWorldRect(graphics, originX, originY, scale, center, CENTER);
        for (Rect spoke : spokes) {
            fillWorldRect(graphics, originX, originY, scale, spoke, SPOKE);
        }
        for (CornerRect corner : corners) {
            fillChamfer(graphics, originX, originY, scale, corner.rect(), corner.suffix());
        }

        graphics.drawString(mc.font, "Footprint " + footprintWidth + "x" + footprintLength +
                "  " + requiredRadius + "/" + STRUCTURE_RADIUS_LIMIT, x + 6, y + height - labelHeight, TEXT, false);
        graphics.drawString(mc.font, "N", originX - 3, plotY + 2, MUTED_TEXT, false);
        graphics.drawString(mc.font, "S", originX - 3, plotY + plotSize - mc.font.lineHeight - 2,
                MUTED_TEXT, false);
        graphics.drawString(mc.font, "W", plotX + 2, originY - mc.font.lineHeight / 2, MUTED_TEXT, false);
        graphics.drawString(mc.font, "E", plotX + plotSize - 8, originY - mc.font.lineHeight / 2,
                MUTED_TEXT, false);
    }

    private void drawGrid(GuiGraphics graphics, int x, int y, int size) {
        for (int i = 1; i < 4; i++) {
            int pos = x + (size * i) / 4;
            graphics.fill(pos, y, pos + 1, y + size, GRID);
            graphics.fill(x, pos, x + size, pos + 1, GRID);
        }
    }

    private void drawOutline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        int right = x + Math.max(1, width);
        int bottom = y + Math.max(1, height);
        graphics.fill(x, y, right, y + 1, color);
        graphics.fill(x, bottom - 1, right, bottom, color);
        graphics.fill(x, y, x + 1, bottom, color);
        graphics.fill(right - 1, y, right, bottom, color);
    }

    private void fillWorldRect(GuiGraphics graphics, int originX, int originY, double scale,
                               double left, double top, double width, double height, int color) {
        int x1 = world(originX, scale, left);
        int y1 = world(originY, scale, top);
        int x2 = world(originX, scale, left + width);
        int y2 = world(originY, scale, top + height);
        graphics.fill(Math.min(x1, x2), Math.min(y1, y2),
                Math.max(x1, x2) + 1, Math.max(y1, y2) + 1, color);
    }

    private void fillWorldRect(GuiGraphics graphics, int originX, int originY, double scale, Rect rect, int color) {
        fillWorldRect(graphics, originX, originY, scale, rect.left(), rect.top(), rect.width(), rect.height(), color);
    }

    private void fillChamfer(GuiGraphics graphics, int originX, int originY, double scale, Rect rect, String corner) {
        fillChamfer(graphics, world(originX, scale, rect.left()), world(originY, scale, rect.top()),
                Math.max(px(scale, (int) Math.round(rect.width())), px(scale, (int) Math.round(rect.height()))),
                corner);
    }

    private void fillChamfer(GuiGraphics graphics, int left, int top, int size, String corner) {
        for (int row = 0; row < size; row++) {
            int start;
            int end;
            switch (corner) {
                case "north_east" -> {
                    start = 0;
                    end = row + 1;
                }
                case "south_east" -> {
                    start = 0;
                    end = size - row;
                }
                case "south_west" -> {
                    start = row;
                    end = size;
                }
                default -> {
                    start = size - row - 1;
                    end = size;
                }
            }
            if (start < end) {
                graphics.fill(left + start, top + row, left + end, top + row + 1, CORNER);
            }
        }
    }

    private int px(double scale, int blocks) {
        return Math.max(1, (int) Math.round(scale * blocks));
    }

    private int world(int origin, double scale, double blocks) {
        return origin + (int) Math.round(blocks * scale);
    }

    private Rect centerRect(int centerSize) {
        return new Rect(-centerSize / 2.0, -centerSize / 2.0, centerSize, centerSize);
    }

    private List<Rect> spokeRects(int centerSize, int spokeWidth,
                                  Map<Direction, HubSpokePlannerSettings.SpokeTemplate> assignments) {
        double halfCenter = centerSize / 2.0;
        double halfSpoke = spokeWidth / 2.0;
        ArrayList<Rect> spokes = new ArrayList<>();
        if (assignments.containsKey(Direction.NORTH)) {
            int northLength = assignments.get(Direction.NORTH).length();
            spokes.add(new Rect(-halfSpoke, -halfCenter - northLength, spokeWidth, northLength));
        }
        if (assignments.containsKey(Direction.EAST)) {
            int eastLength = assignments.get(Direction.EAST).length();
            spokes.add(new Rect(halfCenter, -halfSpoke, eastLength, spokeWidth));
        }
        if (assignments.containsKey(Direction.SOUTH)) {
            int southLength = assignments.get(Direction.SOUTH).length();
            spokes.add(new Rect(-halfSpoke, halfCenter, spokeWidth, southLength));
        }
        if (assignments.containsKey(Direction.WEST)) {
            int westLength = assignments.get(Direction.WEST).length();
            spokes.add(new Rect(-halfCenter - westLength, -halfSpoke, westLength, spokeWidth));
        }
        return List.copyOf(spokes);
    }

    private List<CornerRect> cornerRects(int centerSize, int spokeWidth,
                                         Map<Direction, HubSpokePlannerSettings.SpokeTemplate> assignments,
                                         Map<String, Direction> cornerAnchors,
                                         int cornerWidth, int cornerLength,
                                         HubSpokePlanner.CornerAttachmentMode attachmentMode) {
        if (attachmentMode == HubSpokePlanner.CornerAttachmentMode.CENTER) {
            return centerAttachedCornerRects(centerSize, spokeWidth, cornerAnchors, cornerWidth, cornerLength);
        }
        return spokeAttachedCornerRects(centerSize, spokeWidth, assignments, cornerAnchors, cornerWidth, cornerLength);
    }

    private List<CornerRect> centerAttachedCornerRects(int centerSize, int spokeWidth,
                                                       Map<String, Direction> cornerAnchors,
                                                       int cornerWidth, int cornerLength) {
        double halfCenter = centerSize / 2.0;
        double halfCornerWidth = cornerWidth / 2.0;
        int offset = HubSpokePlanner.centerCornerLateralOffset(centerSize, spokeWidth, cornerWidth);
        ArrayList<CornerRect> corners = new ArrayList<>();
        for (String suffix : List.of("north_west", "north_east", "south_east", "south_west")) {
            Direction anchorDirection = cornerAnchors.get(suffix);
            if (anchorDirection == null) {
                continue;
            }
            corners.add(new CornerRect(centerAttachedCornerRect(suffix, anchorDirection, halfCenter,
                    halfCornerWidth, cornerWidth, cornerLength, offset), suffix));
        }
        return List.copyOf(corners);
    }

    private Rect centerAttachedCornerRect(String suffix, Direction anchorDirection, double halfCenter,
                                          double halfCornerWidth, int cornerWidth, int cornerLength, int offset) {
        return switch (anchorDirection) {
            case NORTH -> new Rect(centerCornerLateralPosition(suffix, offset, halfCornerWidth),
                    -halfCenter - cornerLength, cornerWidth, cornerLength);
            case SOUTH -> new Rect(centerCornerLateralPosition(suffix, offset, halfCornerWidth),
                    halfCenter, cornerWidth, cornerLength);
            case EAST -> new Rect(halfCenter,
                    centerCornerVerticalPosition(suffix, offset, halfCornerWidth), cornerWidth, cornerLength);
            case WEST -> new Rect(-halfCenter - cornerWidth,
                    centerCornerVerticalPosition(suffix, offset, halfCornerWidth), cornerWidth, cornerLength);
            default -> new Rect(0, 0, cornerWidth, cornerLength);
        };
    }

    private double centerCornerLateralPosition(String suffix, int offset, double halfCornerWidth) {
        return switch (suffix) {
            case "north_west", "south_west" -> -offset - halfCornerWidth;
            case "north_east", "south_east" -> offset - halfCornerWidth;
            default -> -halfCornerWidth;
        };
    }

    private double centerCornerVerticalPosition(String suffix, int offset, double halfCornerLength) {
        return switch (suffix) {
            case "north_west", "north_east" -> -offset - halfCornerLength;
            case "south_east", "south_west" -> offset - halfCornerLength;
            default -> -halfCornerLength;
        };
    }

    private List<Rect> spokeAttachedCornerRects(int centerSize, int spokeWidth, int spokeLength,
                                                int cornerWidth, int cornerLength) {
        HubSpokePlannerSettings settings = new HubSpokePlannerSettings(List.of(
                new HubSpokePlannerSettings.SpokeTemplate(HubSpokePlanner.SPOKE_BASE_NAME, "Spoke",
                        spokeLength, HubSpokePlanner.defaultPlatformHeight(),
                        List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST))));
        return spokeAttachedCornerRects(centerSize, spokeWidth, settings.templateAssignments(),
                HubSpokePlanner.cornerAnchorDirections(settings.templateAssignments().keySet()),
                cornerWidth, cornerLength).stream().map(CornerRect::rect).toList();
    }

    private List<CornerRect> spokeAttachedCornerRects(int centerSize, int spokeWidth,
                                                      Map<Direction, HubSpokePlannerSettings.SpokeTemplate> assignments,
                                                      Map<String, Direction> cornerAnchors,
                                                      int cornerWidth, int cornerLength) {
        double halfCenter = centerSize / 2.0;
        double halfSpoke = spokeWidth / 2.0;
        double halfCornerLength = cornerLength / 2.0;
        double northSpokeLeft = -halfSpoke;
        double northSpokeRight = halfSpoke;
        double southSpokeRight = halfSpoke;
        double southSpokeLeft = -halfSpoke;
        ArrayList<CornerRect> corners = new ArrayList<>();
        for (String suffix : List.of("north_west", "north_east", "south_east", "south_west")) {
            Direction anchorDirection = cornerAnchors.get(suffix);
            if (anchorDirection == null || !assignments.containsKey(anchorDirection)) {
                continue;
            }
            int spokeLength = assignments.get(anchorDirection).length();
            int offset = HubSpokePlanner.spokeCornerLateralOffset(spokeLength, cornerLength, anchorDirection);
            corners.add(new CornerRect(spokeAttachedCornerRect(suffix, anchorDirection, halfCenter, halfSpoke,
                    halfCornerLength, cornerWidth, cornerLength, spokeLength, offset), suffix));
        }
        return List.copyOf(corners);
    }

    private Rect spokeAttachedCornerRect(String suffix, Direction anchorDirection, double halfCenter, double halfSpoke,
                                         double halfCornerLength, int cornerWidth, int cornerLength,
                                         int spokeLength, int offset) {
        return switch (anchorDirection) {
            case NORTH -> {
                double centerZ = -halfCenter - (spokeLength / 2.0);
                double x = switch (suffix) {
                    case "north_west" -> -halfSpoke - cornerWidth;
                    case "north_east" -> halfSpoke;
                    default -> -halfSpoke;
                };
                yield new Rect(x, centerZ + offset - halfCornerLength, cornerWidth, cornerLength);
            }
            case SOUTH -> {
                double centerZ = halfCenter + (spokeLength / 2.0);
                double x = switch (suffix) {
                    case "south_west" -> -halfSpoke - cornerWidth;
                    case "south_east" -> halfSpoke;
                    default -> -halfSpoke;
                };
                yield new Rect(x, centerZ + offset - halfCornerLength, cornerWidth, cornerLength);
            }
            case EAST -> {
                double centerX = halfCenter + (spokeLength / 2.0);
                double y = switch (suffix) {
                    case "north_east" -> -halfSpoke - cornerLength;
                    case "south_east" -> halfSpoke;
                    default -> -halfSpoke;
                };
                yield new Rect(centerX + offset - (cornerWidth / 2.0), y, cornerWidth, cornerLength);
            }
            case WEST -> {
                double centerX = -halfCenter - (spokeLength / 2.0);
                double y = switch (suffix) {
                    case "north_west" -> -halfSpoke - cornerLength;
                    case "south_west" -> halfSpoke;
                    default -> -halfSpoke;
                };
                yield new Rect(centerX + offset - (cornerWidth / 2.0), y, cornerWidth, cornerLength);
            }
            default -> new Rect(0, 0, cornerWidth, cornerLength);
        };
    }

    private Rect bounds(Rect center, List<Rect> spokes, List<CornerRect> corners) {
        double left = center.left();
        double top = center.top();
        double right = center.right();
        double bottom = center.bottom();
        ArrayList<Rect> rects = new ArrayList<>(spokes);
        rects.addAll(corners.stream().map(CornerRect::rect).toList());
        for (Rect rect : rects) {
            left = Math.min(left, rect.left());
            top = Math.min(top, rect.top());
            right = Math.max(right, rect.right());
            bottom = Math.max(bottom, rect.bottom());
        }
        return new Rect(left, top, right - left, bottom - top);
    }

    private int familySize(String slotId, int fallback) {
        return Math.max(familyWidth(slotId, fallback), familyLength(slotId, fallback));
    }

    private int familyWidth(String slotId, int fallback) {
        return workspace.familyDefinitions().stream()
                .filter(family -> family.topologySlotId().equals(slotId))
                .findFirst()
                .map(MKWorkspaceRoomFamilyDefinition::roomWidth)
                .orElse(fallback);
    }

    private int familyLength(String slotId, int fallback) {
        return workspace.familyDefinitions().stream()
                .filter(family -> family.topologySlotId().equals(slotId))
                .findFirst()
                .map(MKWorkspaceRoomFamilyDefinition::roomLength)
                .orElse(fallback);
    }
}
