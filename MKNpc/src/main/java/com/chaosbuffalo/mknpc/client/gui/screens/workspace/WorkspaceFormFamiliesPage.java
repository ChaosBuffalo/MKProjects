package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWalledKeepWorkspacePlanner;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspaceSlotSchema;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class WorkspaceFormFamiliesPage extends WorkspacePageBase {
    public static final String ID = "form_families";
    private static final String COURTYARD_PATH_LINEAR_RUN_ID = "keep_courtyard_path";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Template Families"));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Review the authored template families used by this workspace. Room slots and linear runs are grouped here; runtime-only derived slots are hidden."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, helpText);
        MKStackLayoutVertical content = createContentStack(screen);
        WorkspaceDraftSession editor = screen.draftSession();

        addSectionHeader(screen, content, "Room & Slot Families");
        for (MKWorkspaceSlotSchema slot : editor.roomTopologySlots()) {
            addRoomSlotFamily(screen, content, editor, slot);
        }

        addSectionHeader(screen, content, "Run Template Families");
        List<MKWorkspaceLinearRunFamilyDefinition> linearRuns = editor.linearRunFamilies();
        for (int i = 0; i < linearRuns.size(); i++) {
            addLinearRunFamily(screen, content, editor, linearRuns.get(i), i);
        }
        if (shouldShowCourtyardPathSources(editor, linearRuns)) {
            addSyntheticCourtyardPathFamily(screen, content);
        }
        addAddLinearRunButton(screen, content, editor);

        finishScrollContent(screen, scrollView, content);
        addBackButton(screen, root, WorkspaceFormPage.ID);
        return root;
    }

    private void addSectionHeader(MKWorkspaceScreen screen, MKStackLayoutVertical content, String label) {
        MKText header = screen.makeWhiteText(Component.literal(label));
        header.setWidth(screen.contentWidth());
        content.addWidget(header);
        content.addConstraintToWidget(MarginConstraint.LEFT, header);
    }

    private void addRoomSlotFamily(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                   WorkspaceDraftSession editor, MKWorkspaceSlotSchema slot) {
        MKText header = screen.makeWhiteText(Component.literal(formatTopologyLabel(slot.slotId())));
        content.addWidget(header);
        content.addConstraintToWidget(MarginConstraint.LEFT, header);

        MKText summary = screen.makeWhiteText(Component.literal(
                editor.familyCount(slot.slotId()) + " families  |  " +
                        formatTopologyLabel(slot.slotKind()) + "  |  " +
                        formatTopologyLabel(slot.repeat().name().toLowerCase(Locale.ROOT))));
        summary.setWidth(screen.contentWidth());
        summary.setMultiline(true);
        content.addWidget(summary);
        content.addConstraintToWidget(MarginConstraint.LEFT, summary);

        List<MKWorkspaceRoomFamilyDefinition> families = editor.familyDefinitions();
        for (int index : editor.familyIndexesForTopologySlot(slot.slotId())) {
            MKWorkspaceRoomFamilyDefinition family = families.get(index);
            addRoomFamily(screen, content, editor, family, index);
        }

        MKButton addButton = new MKButton(Component.literal("Add Family To Slot"), 180, screen.buttonHeight());
        content.addWidget(addButton);
        content.addConstraintToWidget(new CenterXConstraint(), addButton);
        addButton.setPressedCallback((button, mouseButton) -> {
            editor.selectedFamilyIndex(editor.addFamilyDefinition(slot));
            editor.selectedFamilyExitIndex(-1);
            screen.pushState(WorkspaceFormFamilyDetailPage.ID);
            screen.flagNeedSetup();
            return true;
        });
    }

    private void addRoomFamily(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                               WorkspaceDraftSession editor, MKWorkspaceRoomFamilyDefinition family, int index) {
        MKWorkspaceTopologySlotMetadata slotMetadata = MKWorkspaceTopologySlotMetadata.fromFamily(family);
        List<String> baseNames = templateBaseNamesForFamily(family);
        List<MKWorkspacePieceDefinition> authoredPieces = authoredPiecesForBaseNames(screen, baseNames);

        MKText familyHeader = screen.makeWhiteText(Component.literal(" - " + family.baseName()));
        content.addWidget(familyHeader);
        content.addConstraintToWidget(MarginConstraint.LEFT, familyHeader);

        MKText familySummary = screen.makeWhiteText(Component.literal(
                editor.resolvedFamilyRoomWidth(family) + "x" +
                        editor.resolvedFamilyRoomLength(family) + "x" +
                        editor.resolvedFamilyRoomHeight(family) +
                        (editor.familyHasTopologyStack(family) ? " stack" : "") + "  |  " +
                        formatTopologyLabel(slotMetadata.roleKind()) + " / " +
                        formatTopologyLabel(slotMetadata.pieceKind()) + "  |  exits " +
                        summarizeFamilyExits(family) + "  |  shaft " +
                        (family.supportsVerticalAccess() ? "yes" : "no") + "  |  " +
                        authoredPieceSummary(authoredPieces)));
        familySummary.setWidth(screen.contentWidth());
        familySummary.setMultiline(true);
        content.addWidget(familySummary);
        content.addConstraintToWidget(MarginConstraint.LEFT, familySummary);

        MKButton editButton = new MKButton(Component.literal("Edit Family"), 180, screen.buttonHeight());
        content.addWidget(editButton);
        content.addConstraintToWidget(new CenterXConstraint(), editButton);
        editButton.setPressedCallback((button, mouseButton) -> {
            editor.selectedFamilyIndex(index);
            editor.selectedFamilyExitIndex(-1);
            screen.pushState(WorkspaceFormFamilyDetailPage.ID);
            screen.flagNeedSetup();
            return true;
        });

        MKButton openTemplates = new MKButton(Component.literal("Open Templates"), 180, screen.buttonHeight());
        openTemplates.setEnabled(!authoredPieces.isEmpty());
        content.addWidget(openTemplates);
        content.addConstraintToWidget(new CenterXConstraint(), openTemplates);
        openTemplates.setPressedCallback((button, mouseButton) -> {
            openTemplatesForBaseNames(screen, baseNames);
            return true;
        });
    }

    private void addLinearRunFamily(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                    WorkspaceDraftSession editor, MKWorkspaceLinearRunFamilyDefinition linearRun,
                                    int index) {
        List<MKWorkspacePieceDefinition> authoredPieces = authoredPiecesForLinearRun(screen,
                linearRun.linearRunId());
        MKText header = screen.makeWhiteText(Component.literal(formatTopologyLabel(linearRun.linearRunId())));
        content.addWidget(header);
        content.addConstraintToWidget(MarginConstraint.LEFT, header);

        MKText summary = screen.makeWhiteText(Component.literal(
                formatTopologyLabel(linearRun.kind().getSerializedName()) + "  |  " +
                        linearRun.topologySlotId() + "  |  " + linearRun.openingProfileId() + "  |  " +
                        linearRun.length() + "x" + linearRun.interiorWidth() + "x" +
                        linearRun.interiorHeight() + "  |  slope " + linearRun.slopeDelta() + "  |  " +
                        describePathAccess(linearRun.allowOnMainPath(), linearRun.allowOnBranchPath()) +
                        "  |  " + authoredPieceSummary(authoredPieces)));
        summary.setWidth(screen.contentWidth());
        summary.setMultiline(true);
        content.addWidget(summary);
        content.addConstraintToWidget(MarginConstraint.LEFT, summary);

        MKButton editButton = new MKButton(Component.literal("Edit Run Settings"), 180, screen.buttonHeight());
        content.addWidget(editButton);
        content.addConstraintToWidget(new CenterXConstraint(), editButton);
        editButton.setPressedCallback((button, mouseButton) -> {
            editor.selectedLinearRunIndex(index);
            screen.pushState(WorkspaceFormLinearRunDetailPage.ID);
            screen.flagNeedSetup();
            return true;
        });

        MKButton openTemplates = new MKButton(Component.literal("Open Templates"), 180, screen.buttonHeight());
        openTemplates.setEnabled(!authoredPieces.isEmpty());
        content.addWidget(openTemplates);
        content.addConstraintToWidget(new CenterXConstraint(), openTemplates);
        openTemplates.setPressedCallback((button, mouseButton) -> {
            screen.openWorkspaceTopologySlotForPrefix(linearRun.linearRunId());
            return true;
        });
    }

    private void addSyntheticCourtyardPathFamily(MKWorkspaceScreen screen, MKStackLayoutVertical content) {
        List<MKWorkspacePieceDefinition> authoredPieces = authoredPiecesForLinearRun(screen,
                COURTYARD_PATH_LINEAR_RUN_ID);
        MKText header = screen.makeWhiteText(Component.literal("Courtyard Path"));
        content.addWidget(header);
        content.addConstraintToWidget(MarginConstraint.LEFT, header);

        MKText summary = screen.makeWhiteText(Component.literal(
                "Open Walkway  |  keep.courtyard.path  |  derives 7 path sockets  |  " +
                        authoredPieceSummary(authoredPieces)));
        summary.setWidth(screen.contentWidth());
        summary.setMultiline(true);
        content.addWidget(summary);
        content.addConstraintToWidget(MarginConstraint.LEFT, summary);

        MKButton openTemplates = new MKButton(Component.literal("Open Templates"), 180, screen.buttonHeight());
        openTemplates.setEnabled(!authoredPieces.isEmpty());
        content.addWidget(openTemplates);
        content.addConstraintToWidget(new CenterXConstraint(), openTemplates);
        openTemplates.setPressedCallback((button, mouseButton) -> {
            screen.openWorkspaceTopologySlotForPrefix(COURTYARD_PATH_LINEAR_RUN_ID);
            return true;
        });
    }

    private void addAddLinearRunButton(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                       WorkspaceDraftSession editor) {
        MKButton addLinearRun = new MKButton(Component.literal("Add Run Family"), 180, screen.buttonHeight());
        content.addWidget(addLinearRun);
        content.addConstraintToWidget(new CenterXConstraint(), addLinearRun);
        addLinearRun.setPressedCallback((button, mouseButton) -> {
            editor.selectedLinearRunIndex(editor.addLinearRunFamily());
            screen.pushState(WorkspaceFormLinearRunDetailPage.ID);
            screen.flagNeedSetup();
            return true;
        });
    }

    private boolean shouldShowCourtyardPathSources(WorkspaceDraftSession editor,
                                                   List<MKWorkspaceLinearRunFamilyDefinition> linearRuns) {
        return MKWalledKeepWorkspacePlanner.PLANNER_ID.equals(editor.topologyPlannerId()) &&
                linearRuns.stream().noneMatch(run -> COURTYARD_PATH_LINEAR_RUN_ID.equals(run.linearRunId()));
    }

    private List<String> templateBaseNamesForFamily(MKWorkspaceRoomFamilyDefinition family) {
        if (family.baseName().startsWith("keep_corner_shared")) {
            return List.of(family.baseName(),
                    family.baseName().replace("keep_corner_shared", "keep_corner_north_west"));
        }
        return List.of(family.baseName());
    }

    private List<MKWorkspacePieceDefinition> authoredPiecesForBaseNames(MKWorkspaceScreen screen,
                                                                        List<String> baseNames) {
        if (screen.workspace() == null) {
            return List.of();
        }
        return screen.workspace().pieces().stream()
                .filter(WorkspacePieceDisplay::isAuthoredTemplatePiece)
                .filter(piece -> baseNames.contains(WorkspacePieceDisplay.getBaseName(piece)))
                .toList();
    }

    private void openTemplatesForBaseNames(MKWorkspaceScreen screen, List<String> baseNames) {
        for (String baseName : baseNames) {
            if (screen.openWorkspaceTopologySlotForBaseName(baseName)) {
                return;
            }
        }
    }

    private List<MKWorkspacePieceDefinition> authoredPiecesForLinearRun(MKWorkspaceScreen screen,
                                                                        String linearRunId) {
        if (screen.workspace() == null) {
            return List.of();
        }
        return screen.workspace().pieces().stream()
                .filter(WorkspacePieceDisplay::isAuthoredTemplatePiece)
                .filter(piece -> linearRunId.equals(piece.tags().get("workspace_linear_run_family_id")))
                .toList();
    }

    private String authoredPieceSummary(List<MKWorkspacePieceDefinition> pieces) {
        if (pieces.isEmpty()) {
            return "no authored templates";
        }
        long sourceCount = pieces.stream()
                .map(WorkspacePieceDisplay::getBaseName)
                .distinct()
                .count();
        int variants = WorkspacePieceDisplay.countVariants(pieces);
        return sourceCount + " source " + (sourceCount == 1 ? "template" : "templates") +
                " / " + variants + " " + (variants == 1 ? "variant" : "variants");
    }

    private String describePathAccess(boolean main, boolean branch) {
        if (main && branch) {
            return "main + branch";
        }
        if (main) {
            return "main only";
        }
        if (branch) {
            return "branch only";
        }
        return "disabled";
    }

    private String summarizeFamilyExits(MKWorkspaceRoomFamilyDefinition family) {
        if (family.horizontalExits().isEmpty()) {
            return "none";
        }
        return family.horizontalExits().stream()
                .map(this::describeFamilyExit)
                .collect(Collectors.joining(", "));
    }

    private String describeFamilyExit(MKWorkspaceFamilyHorizontalExitDefinition exit) {
        if (exit.isVerticalAccess()) {
            return formatDirection(exit.direction()) + " / " + formatTopologyLabel(exit.pathKind().getSerializedName());
        }
        return formatDirection(exit.direction()) + " / " + formatTopologyLabel(exit.pathKind().getSerializedName()) +
                " / " + formatExitConnectionMode(exit.connectionMode()) + " / " + exit.openingProfileId() +
                " / side " + exit.sideOffset() + " / up " + exit.verticalOffset();
    }

    private String formatExitConnectionMode(MKWorkspaceHorizontalExitConnectionMode connectionMode) {
        return switch (connectionMode) {
            case LINEAR_RUN -> "Linear Run";
            case DIRECT_ROOM -> "Direct Room";
            case NO_CONNECTION -> "No Connection";
        };
    }

    private String formatDirection(Direction direction) {
        return formatTopologyLabel(direction.getSerializedName());
    }

    private String formatTopologyLabel(String key) {
        return WorkspacePieceDisplay.formatTopologyLabel(key);
    }
}


