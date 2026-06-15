package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
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
import java.util.stream.Collectors;

public class WorkspaceFormFamiliesPage extends WorkspacePageBase {
    public static final String ID = "form_families";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Topology Slot Families"));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Assign room, corner, and gate families to topology slots. Linear-run slots are edited on the Linear Run Families page."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, helpText);
        MKStackLayoutVertical content = createContentStack(screen);
        WorkspaceDraftSession editor = screen.draftSession();

        for (MKWorkspaceSlotSchema slot : editor.roomTopologySlots()) {
            MKText header = screen.makeWhiteText(Component.literal(formatTopologyLabel(slot.slotId())));
            content.addWidget(header);
            content.addConstraintToWidget(MarginConstraint.LEFT, header);

            MKText summary = screen.makeWhiteText(Component.literal(
                    editor.familyCount(slot.slotId()) + " families  |  " +
                            formatTopologyLabel(slot.slotKind()) + "  |  " +
                            formatTopologyLabel(slot.repeat().name().toLowerCase(java.util.Locale.ROOT))));
            summary.setWidth(screen.contentWidth());
            summary.setMultiline(true);
            content.addWidget(summary);
            content.addConstraintToWidget(MarginConstraint.LEFT, summary);

            List<MKWorkspaceRoomFamilyDefinition> families = editor.familyDefinitions();
            for (int index : editor.familyIndexesForTopologySlot(slot.slotId())) {
                MKWorkspaceRoomFamilyDefinition family = families.get(index);
                MKWorkspaceTopologySlotMetadata slotMetadata = MKWorkspaceTopologySlotMetadata.fromFamily(family);
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
                                (family.supportsVerticalAccess() ? "yes" : "no")));
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

        finishScrollContent(screen, scrollView, content);
        addBackButton(screen, root, WorkspaceFormPage.ID);
        return root;
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


