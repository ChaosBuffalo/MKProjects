package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.network.packets.ExportWorkspacePiecesPacket;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorkspaceManagePage extends WorkspacePageBase {
    public static final String ID = "workspace";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(WorkspacePageContext context) {
        MKLayout root = createPanel(context);
        MKStructureWorkspace workspace = context.workspace();

        addTitle(context, root, Component.translatable("mknpc.workspace.screen.manage_title"));
        MKText summary = addHeaderText(context, root, Component.translatable("mknpc.workspace.screen.manage_summary",
                workspace.namespace(), workspace.structureName(), workspace.pieces().size()));

        int buttonCount = 4;
        int buttonAreaHeight = (buttonCount * context.buttonHeight()) +
                ((buttonCount - 1) * context.buttonGap()) + context.bottomPadding();
        int scrollTop = context.scrollTopAfterHeader(root, summary);
        int scrollHeight = context.panelY() + context.panelHeight() - buttonAreaHeight - 8 - scrollTop;
        MKScrollView scrollView = new MKScrollView(context.panelX() + 10, scrollTop,
                context.scrollWidth(), scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = createContentStack(context);
        for (Map.Entry<String, List<MKWorkspacePieceDefinition>> entry : groupPiecesByTopology(workspace).entrySet()) {
            String topologyKey = entry.getKey();
            List<MKWorkspacePieceDefinition> pieces = entry.getValue();
            MKWorkspacePieceDefinition templatePiece = pieces.stream()
                    .filter(piece -> piece.variantIndex() == 0)
                    .findFirst()
                    .orElse(pieces.get(0));

            MKText header = context.makeWhiteText(Component.literal(buildWorkspaceGroupLabel(templatePiece)));
            header.setWidth(context.contentWidth());
            content.addWidget(header);
            content.addConstraintToWidget(MarginConstraint.LEFT, header);

            int variantCount = countVariants(pieces);
            long generatedCount = pieces.stream().filter(WorkspaceManagePage::hasGeneratedStairs).count();
            MKText details = context.makeWhiteText(Component.literal(
                    pieces.size() + " piece" + (pieces.size() == 1 ? "" : "s") + " - " +
                            variantCount + " variant" + (variantCount == 1 ? "" : "s") +
                            " - template " + getBaseName(templatePiece) +
                            (supportsStairGeneration(pieces) ? " - stairs " + generatedCount + "/" + pieces.size() : "")));
            details.setWidth(context.contentWidth());
            content.addWidget(details);
            content.addConstraintToWidget(MarginConstraint.LEFT, details);

            MKButton openCategory = new MKButton(Component.literal("Open Category"), 180, context.buttonHeight());
            content.addWidget(openCategory);
            content.addConstraintToWidget(new CenterXConstraint(), openCategory);
            openCategory.setPressedCallback((button, mouseButton) -> {
                context.openWorkspaceCategory().accept(topologyKey);
                return true;
            });
        }

        finishScrollContent(context, scrollView, content);

        MKButton close = addBottomButton(context, root, Component.translatable("mknpc.workspace.button.close"), 120, 0);
        close.setPressedCallback((button, mouseButton) -> {
            context.closeScreen().run();
            return true;
        });

        MKButton utilities = addBottomButton(context, root, Component.literal("Utilities"), 180, 1);
        utilities.setPressedCallback((button, mouseButton) -> {
            context.pushState().accept(WorkspaceUtilitiesPage.ID);
            context.flagNeedSetup().run();
            return true;
        });

        MKButton exportAll = addBottomButton(context, root,
                Component.translatable("mknpc.workspace.button.export_all"), 180, 2);
        exportAll.setPressedCallback((button, mouseButton) -> {
            PacketDistributor.sendToServer(new ExportWorkspacePiecesPacket(context.anchor()));
            return true;
        });

        MKButton editTemplates = addBottomButton(context, root,
                Component.translatable("mknpc.workspace.button.edit_template_settings"), 180, 3);
        editTemplates.setPressedCallback((button, mouseButton) -> {
            context.pushState().accept(WorkspaceFormPage.ID);
            context.flagNeedSetup().run();
            return true;
        });

        return root;
    }

    private Map<String, List<MKWorkspacePieceDefinition>> groupPiecesByTopology(MKStructureWorkspace workspace) {
        Map<String, List<MKWorkspacePieceDefinition>> grouped = new LinkedHashMap<>();
        List<MKWorkspacePieceDefinition> sortedPieces = workspace.pieces().stream()
                .sorted(Comparator
                        .comparing(WorkspaceManagePage::buildWorkspaceGroupLabel)
                        .thenComparingInt(MKWorkspacePieceDefinition::variantIndex))
                .toList();
        for (MKWorkspacePieceDefinition piece : sortedPieces) {
            String topologyKey = buildWorkspaceGroupKey(piece);
            grouped.computeIfAbsent(topologyKey, ignored -> new java.util.ArrayList<>()).add(piece);
        }
        return grouped;
    }

    private String buildWorkspaceGroupKey(MKWorkspacePieceDefinition piece) {
        String hallwayFamilyId = piece.tags().get("workspace_hallway_family_id");
        if (hallwayFamilyId != null) {
            return "hallway:" + hallwayFamilyId + ":" +
                    piece.tags().getOrDefault("workspace_hallway_path_kind", "branch");
        }
        String familyId = piece.tags().get("workspace_family_id");
        if (familyId != null) {
            return "room:" + piece.tags().getOrDefault("workspace_category", "main") + ":" +
                    familyId + ":" + piece.tags().getOrDefault("workspace_horizontal_exits", "none");
        }
        return "role:" + piece.role().getSerializedName();
    }

    private static String buildWorkspaceGroupLabel(MKWorkspacePieceDefinition piece) {
        String hallwayFamilyId = piece.tags().get("workspace_hallway_family_id");
        if (hallwayFamilyId != null) {
            return "Hallway / " + hallwayFamilyId + " / " +
                    formatTopologyLabel(piece.tags().getOrDefault("workspace_hallway_path_kind", "branch"));
        }
        String familyId = piece.tags().get("workspace_family_id");
        if (familyId != null) {
            return formatTopologyLabel(piece.tags().getOrDefault("workspace_category", "main")) +
                    " / " + familyId +
                    " / exits " + piece.tags().getOrDefault("workspace_horizontal_exits", "none");
        }
        return formatTopologyLabel(piece.role().getSerializedName());
    }

    private static String getBaseName(MKWorkspacePieceDefinition piece) {
        return piece.tags().getOrDefault("workspace_base_name", piece.pieceName());
    }

    private static int countVariants(List<MKWorkspacePieceDefinition> pieces) {
        return (int) pieces.stream().filter(piece -> piece.variantIndex() > 0).count();
    }

    private static boolean supportsStairGeneration(List<MKWorkspacePieceDefinition> pieces) {
        return pieces.stream().anyMatch(piece -> MKWorkspaceVerticalAccessTags.supportsVerticalAccess(piece.tags()));
    }

    private static boolean hasGeneratedStairs(MKWorkspacePieceDefinition piece) {
        return !piece.generatedStairPositions().isEmpty() &&
                !"none".equals(piece.tags().getOrDefault("generated_stair_mode", "none"));
    }

    private static String formatTopologyLabel(String key) {
        String[] parts = key.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            if (!part.isEmpty()) {
                builder.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    builder.append(part.substring(1));
                }
            }
        }
        return builder.toString();
    }
}
