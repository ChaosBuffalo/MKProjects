package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspaceResolvedSlotCatalog;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.CompatButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Slot/family browser. The detail column intentionally renders only the selected family's templates. */
public class WorkspaceFormFamiliesPage extends WorkspacePageBase {
    public static final String ID = "form_families";
    private static final int SELECTOR_WIDTH = 250;
    private static final int COLUMN_GAP = 12;
    private static final int FAMILY_ROW_HEIGHT = 14;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);
        addTitle(screen, root, Component.literal("Topology Slots & Content Families"));
        MKText help = addHeaderText(screen, root, Component.literal(
                "Choose a family under its topology slot. The right panel shows only that family's canonical and variants."));

        WorkspaceDraftSession editor = screen.draftSession();
        WorkspaceContentTree tree = WorkspaceContentTree.buildDeclared(currentPieces(screen), declaredSlots(editor));
        WorkspaceContentTree.FamilyNode selected = selectedFamily(editor, tree);
        if (selected != null && !selected.groupKey().equals(screen.selectedTopologyKey())) {
            screen.selectWorkspaceTopologySlot(selected.groupKey());
        }

        int scrollTop = screen.scrollTopAfterHeader(root, help);
        int scrollHeight = screen.panelY() + screen.panelHeight() - screen.bottomPadding() -
                screen.buttonHeight() - 12 - scrollTop;
        int left = screen.panelX() + 10;
        int detailLeft = left + SELECTOR_WIDTH + COLUMN_GAP;
        int detailWidth = screen.panelX() + screen.panelWidth() - 10 - detailLeft;

        MKScrollView selectorView = new MKScrollView(left, scrollTop, SELECTOR_WIDTH, scrollHeight);
        selectorView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(selectorView);
        MKStackLayoutVertical selector = new MKStackLayoutVertical(0, 0, SELECTOR_WIDTH - 8);
        selector.setMargins(4, 4, 4, 4);
        selector.setPaddingTop(4).setPaddingBot(4);
        addTree(screen, selector, editor, tree, selected);
        finishScrollContent(screen, selectorView, selector);

        MKScrollView detailView = new MKScrollView(detailLeft, scrollTop, detailWidth, scrollHeight);
        detailView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(detailView);
        MKStackLayoutVertical detail = new MKStackLayoutVertical(0, 0, detailWidth - 8);
        detail.setMargins(4, 4, 4, 4);
        detail.setPaddingTop(4).setPaddingBot(4);
        addFamilyDetail(screen, detail, selected);
        finishScrollContent(screen, detailView, detail);

        addApplyBackButtonRow(screen, root, () -> screen.goBackOrSwitchTo(WorkspaceManagePage.ID));
        return root;
    }

    private void addTree(MKWorkspaceScreen screen, MKStackLayoutVertical selector, WorkspaceDraftSession editor,
                         WorkspaceContentTree tree, WorkspaceContentTree.FamilyNode selected) {
        if (tree.slots().isEmpty()) {
            addTreeText(screen, selector, "No topology slots are declared.");
            return;
        }
        for (WorkspaceContentTree.SlotNode slot : tree.slots()) {
            boolean selectedSlot = selected != null && selected.slotId().equals(slot.slotId());
            MKText slotHeader = screen.makeWhiteText(Component.literal((selectedSlot ? "▾ " : "• ") +
                    truncate(slot.label(), 29)));
            slotHeader.setWidth(SELECTOR_WIDTH - 18);
            slotHeader.setMultiline(true);
            selector.addWidget(slotHeader);
            selector.addConstraintToWidget(MarginConstraint.LEFT, slotHeader);

            String slotStatus = slot.families().size() + (slot.families().size() == 1 ? " family" : " families");
            if (!slot.scaffolds().isEmpty()) slotStatus += " | scaffold " + slot.scaffolds().getFirst().pieceName();
            addTreeText(screen, selector, "  " + slotStatus);
            if (slot.families().isEmpty()) addTreeText(screen, selector, "  No placeable content families");

            for (WorkspaceContentTree.FamilyNode family : slot.families()) {
                boolean isSelected = selected != null && selected.slotId().equals(family.slotId()) &&
                        selected.familyId().equals(family.familyId());
                String label = truncate(family.familyId(), 20) +
                        " [" + family.status() + "]";
                CompatButton row = new CompatButton(Component.literal(label), SELECTOR_WIDTH - 24,
                        FAMILY_ROW_HEIGHT).setSelected(isSelected).setPressedCallback((button, mouseButton) -> {
                    editor.selectContentFamily(family.slotId(), family.familyId());
                    screen.selectWorkspaceTopologySlot(family.groupKey());
                    screen.flagNeedSetup();
                    return true;
                });
                selector.addWidget(row);
                selector.addConstraintToWidget(MarginConstraint.LEFT, row);
            }
        }
    }

    private void addFamilyDetail(MKWorkspaceScreen screen, MKStackLayoutVertical detail,
                                 WorkspaceContentTree.FamilyNode selected) {
        if (selected == null) {
            addTreeText(screen, detail, "Select a content family on the left to manage its templates.");
            return;
        }
        WorkspaceTemplateDetailPane.addTemplateControls(screen, detail, () -> { }, false);
    }

    private WorkspaceContentTree.FamilyNode selectedFamily(WorkspaceDraftSession editor,
                                                            WorkspaceContentTree tree) {
        WorkspaceContentTree.FamilyNode selected = tree.family(editor.selectedContentSlotId(),
                editor.selectedContentFamilyId());
        if (selected == null) {
            selected = tree.firstFamily();
            if (selected == null) editor.clearContentFamilySelection();
            else editor.selectContentFamily(selected.slotId(), selected.familyId());
        }
        return selected;
    }

    private List<MKWorkspacePieceDefinition> currentPieces(MKWorkspaceScreen screen) {
        if (screen.workspace() == null) return List.of();
        return screen.draftSession().filterPendingDeletedVariants(screen.workspace().pieces());
    }

    private Map<String, String> declaredSlots(WorkspaceDraftSession editor) {
        LinkedHashMap<String, String> slots = new LinkedHashMap<>();
        MKWorkspaceResolvedSlotCatalog.resolve(editor.roomTopologySlots(), editor.insertSlots()).slots()
                .forEach(slot -> slots.putIfAbsent(slot.slotId(),
                        WorkspacePieceDisplay.formatTopologyLabel(slot.slotId())));
        return Map.copyOf(slots);
    }

    private void addTreeText(MKWorkspaceScreen screen, MKStackLayoutVertical content, String value) {
        MKText text = screen.makeWhiteText(Component.literal(value));
        text.setWidth(Math.max(120, content.getWidth() - 16));
        text.setMultiline(true);
        content.addWidget(text);
        content.addConstraintToWidget(MarginConstraint.LEFT, text);
    }

    private String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, Math.max(1, max - 3)) + "...";
    }

}
