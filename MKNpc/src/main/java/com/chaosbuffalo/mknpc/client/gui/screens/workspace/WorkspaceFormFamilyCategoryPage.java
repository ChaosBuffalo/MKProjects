package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategory;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
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

public class WorkspaceFormFamilyCategoryPage extends WorkspacePageBase {
    public static final String ID = "form_family_category";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        WorkspaceDraftSession editor = screen.draftSession();
        MKTowerWorkspaceCategory selectedCategory = editor.selectedFamilyCategory();
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal(formatTopologyLabel(selectedCategory.getSerializedName()) + " Families"));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Edit legacy family groups. The topology-slot family page is the primary editor for new topology profiles."));

        int buttonAreaHeight = (2 * screen.buttonHeight()) + screen.buttonGap() + screen.bottomPadding();
        int scrollTop = screen.scrollTopAfterHeader(root, helpText);
        int scrollHeight = screen.panelY() + screen.panelHeight() - buttonAreaHeight - 12 - scrollTop;
        MKScrollView scrollView = new MKScrollView(screen.panelX() + 10, scrollTop,
                screen.scrollWidth(), scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = createContentStack(screen);
        List<MKTowerWorkspaceFamilyDefinition> families = editor.familyDefinitions();
        List<Integer> familyIndexes = families.stream()
                .filter(family -> family.category() == selectedCategory)
                .map(families::indexOf)
                .toList();
        for (int index : familyIndexes) {
            MKTowerWorkspaceFamilyDefinition family = families.get(index);
            MKWorkspaceTopologySlotMetadata slotMetadata = MKWorkspaceTopologySlotMetadata.fromFamily(family);
            MKText header = screen.makeWhiteText(Component.literal(family.baseName()));
            content.addWidget(header);
            content.addConstraintToWidget(MarginConstraint.LEFT, header);
            MKText summary = screen.makeWhiteText(Component.literal(
                    editor.resolvedFamilyRoomWidth(family) + "x" +
                            editor.resolvedFamilyRoomLength(family) + "x" +
                            editor.resolvedFamilyRoomHeight(family) +
                            (editor.familyHasTopologyStack(family) ? " stack" : "") + "  |  " +
                            formatTopologyLabel(slotMetadata.pieceRole().getSerializedName()) + "  |  exits " +
                            summarizeFamilyExits(family) + "  |  shaft " +
                            (family.supportsVerticalAccess() ? "yes" : "no")));
            summary.setWidth(screen.contentWidth());
            summary.setMultiline(true);
            content.addWidget(summary);
            content.addConstraintToWidget(MarginConstraint.LEFT, summary);

            MKButton openButton = new MKButton(Component.literal("Edit Family"), 180, screen.buttonHeight());
            content.addWidget(openButton);
            content.addConstraintToWidget(new CenterXConstraint(), openButton);
            openButton.setPressedCallback((button, mouseButton) -> {
                editor.selectedFamilyIndex(index);
                editor.selectedFamilyExitIndex(-1);
                screen.pushState("form_family_detail");
                screen.flagNeedSetup();
                return true;
            });
        }

        finishScrollContent(screen, scrollView, content);

        MKButton addFamily = addBottomButton(screen, root, Component.literal("Add Family"), 180, 1);
        addFamily.setPressedCallback((button, mouseButton) -> {
            editor.selectedFamilyIndex(editor.addFamilyDefinition(selectedCategory));
            editor.selectedFamilyExitIndex(-1);
            screen.pushState("form_family_detail");
            screen.flagNeedSetup();
            return true;
        });

        addBackButton(screen, root, WorkspaceFormFamiliesPage.ID);
        return root;
    }

    private String summarizeFamilyExits(MKTowerWorkspaceFamilyDefinition family) {
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
            case HALLWAY -> "Hallway";
            case DIRECT_ROOM -> "Direct Room";
            case NO_CONNECTION -> "No Connection";
        };
    }

    private String formatDirection(Direction direction) {
        return formatTopologyLabel(direction.getSerializedName());
    }

    private String formatTopologyLabel(String key) {
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


