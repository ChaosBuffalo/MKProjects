package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspace.network.packets.TeleportToWorkspacePiecePacket;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.planner.MKWorkspaceSlotSchema;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WorkspaceFormFamiliesPage extends WorkspacePageBase {
    public static final String ID = "form_families";
    private static final int SELECTOR_WIDTH = 230;
    private static final int COLUMN_GAP = 12;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Template Families"));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Select a template family on the left, then edit settings, open variants, or teleport from the detail pane."));
        WorkspaceDraftSession editor = screen.draftSession();
        List<TemplateFamilyEntry> entries = templateFamilyEntries(editor);
        TemplateFamilyEntry selected = selectedEntry(editor, entries);

        int scrollTop = screen.scrollTopAfterHeader(root, helpText);
        int scrollHeight = screen.panelY() + screen.panelHeight() - screen.bottomPadding() -
                screen.buttonHeight() - 12 - scrollTop;
        int left = screen.panelX() + 10;
        int detailLeft = left + SELECTOR_WIDTH + COLUMN_GAP;
        int detailWidth = screen.panelX() + screen.panelWidth() - 10 - detailLeft;

        MKScrollView selectorScrollView = new MKScrollView(left, scrollTop, SELECTOR_WIDTH, scrollHeight);
        selectorScrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(selectorScrollView);

        MKScrollView detailScrollView = new MKScrollView(detailLeft, scrollTop, detailWidth, scrollHeight);
        detailScrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(detailScrollView);

        MKStackLayoutVertical selector = new MKStackLayoutVertical(0, 0, SELECTOR_WIDTH - 8);
        selector.setMargins(4, 4, 4, 4);
        selector.setPaddingTop(4).setPaddingBot(4);
        addSelectorEntries(screen, selector, editor, entries, selected);
        finishScrollContent(screen, selectorScrollView, selector);

        MKStackLayoutVertical detail = new MKStackLayoutVertical(0, 0, detailWidth - 8);
        detail.setMargins(4, 4, 4, 4);
        detail.setPaddingTop(4).setPaddingBot(4);
        addSelectedEntryDetail(screen, detail, editor, selected);
        finishScrollContent(screen, detailScrollView, detail);

        addApplyBackButtonRow(screen, root, () -> handleFooterBack(screen));
        return root;
    }

    private void handleFooterBack(MKWorkspaceScreen screen) {
        WorkspaceDraftSession editor = screen.draftSession();
        if (editor.selectedTemplateFamilyTemplateKey() != null ||
                editor.selectedTemplateFamilyEditId() != null) {
            editor.selectedTemplateFamilyTemplateKey(null);
            editor.selectedTemplateFamilyEditId(null);
            editor.selectedFamilyExitIndex(-1);
            screen.clearSelectedTopologyKey();
            screen.flagNeedSetup();
            return;
        }
        screen.goBackOrSwitchTo(WorkspaceManagePage.ID);
    }

    private List<TemplateFamilyEntry> templateFamilyEntries(WorkspaceDraftSession editor) {
        java.util.ArrayList<TemplateFamilyEntry> entries = new java.util.ArrayList<>();
        for (MKWorkspaceSlotSchema slot : editor.roomTopologySlots()) {
            entries.add(TemplateFamilyEntry.roomSlot(slot));
        }
        List<MKWorkspaceLinearRunFamilyDefinition> linearRuns = editor.linearRunFamilies();
        for (int i = 0; i < linearRuns.size(); i++) {
            MKWorkspaceLinearRunFamilyDefinition linearRun = linearRuns.get(i);
            if (editor.plannerAdapter().showLinearRunInTemplateFamilies(editor, linearRun)) {
                entries.add(TemplateFamilyEntry.linearRun(linearRun, i));
            }
        }
        List<WorkspaceTemplateFamilyDisplay> extraTemplateFamilies =
                editor.plannerAdapter().extraTemplateFamilies(editor);
        for (int i = 0; i < extraTemplateFamilies.size(); i++) {
            entries.add(TemplateFamilyEntry.extra(extraTemplateFamilies.get(i), i));
        }
        List<MKWorkspaceInsertFamilyDefinition> insertFamilies = editor.insertFamilies();
        for (int i = 0; i < insertFamilies.size(); i++) {
            entries.add(TemplateFamilyEntry.insertFamily(insertFamilies.get(i), i));
        }
        return List.copyOf(entries);
    }

    private TemplateFamilyEntry selectedEntry(WorkspaceDraftSession editor, List<TemplateFamilyEntry> entries) {
        String selectedId = editor.selectedTemplateFamilyId();
        TemplateFamilyEntry selected = entries.stream()
                .filter(entry -> entry.id().equals(selectedId))
                .findFirst()
                .orElseGet(() -> entries.isEmpty() ? null : entries.getFirst());
        if (selected != null && !selected.id().equals(selectedId)) {
            editor.selectedTemplateFamilyId(selected.id());
            editor.selectedTemplateFamilyEditId(null);
            editor.selectedTemplateFamilyTemplateKey(null);
        }
        if (selected == null) {
            editor.selectedTemplateFamilyEditId(null);
        }
        return selected;
    }

    private void addSelectorEntries(MKWorkspaceScreen screen, MKStackLayoutVertical selector,
                                    WorkspaceDraftSession editor, List<TemplateFamilyEntry> entries,
                                    TemplateFamilyEntry selected) {
        addSelectorHeader(screen, selector, "Room & Slot Families");
        entries.stream()
                .filter(entry -> entry.kind() == TemplateFamilyKind.ROOM_SLOT)
                .forEach(entry -> addSelectorButton(screen, selector, editor, entry, selected));

        addSelectorHeader(screen, selector, "Run Families");
        entries.stream()
                .filter(entry -> entry.kind() == TemplateFamilyKind.LINEAR_RUN)
                .forEach(entry -> addSelectorButton(screen, selector, editor, entry, selected));
        addAddLinearRunButton(screen, selector, editor);

        addSelectorHeader(screen, selector, "Insert Families");
        entries.stream()
                .filter(entry -> entry.kind() == TemplateFamilyKind.INSERT_FAMILY)
                .forEach(entry -> addSelectorButton(screen, selector, editor, entry, selected));
        addAddInsertFamilyButton(screen, selector, editor);

        List<TemplateFamilyEntry> extraEntries = entries.stream()
                .filter(entry -> entry.kind() == TemplateFamilyKind.EXTRA)
                .toList();
        if (!extraEntries.isEmpty()) {
            addSelectorHeader(screen, selector, "Planner Source");
            extraEntries.forEach(entry -> addSelectorButton(screen, selector, editor, entry, selected));
        }
    }

    private void addSelectorButton(MKWorkspaceScreen screen, MKStackLayoutVertical selector,
                                   WorkspaceDraftSession editor, TemplateFamilyEntry entry,
                                   TemplateFamilyEntry selected) {
        boolean isSelected = selected != null && selected.id().equals(entry.id());
        MKButton button = new MKButton(Component.literal((isSelected ? "> " : "") +
                truncateSelectorLabel(entry.label())), SELECTOR_WIDTH - 18, screen.buttonHeight());
        selector.addWidget(button);
        selector.addConstraintToWidget(new CenterXConstraint(), button);
        button.setPressedCallback((pressedButton, mouseButton) -> {
            editor.selectedTemplateFamilyId(entry.id());
            editor.selectedTemplateFamilyTemplateKey(null);
            editor.selectedTemplateFamilyEditId(null);
            screen.clearSelectedTopologyKey();
            screen.flagNeedSetup();
            return true;
        });
    }

    private void addSelectedEntryDetail(MKWorkspaceScreen screen, MKStackLayoutVertical detail,
                                        WorkspaceDraftSession editor, TemplateFamilyEntry selected) {
        if (selected == null) {
            addDetailText(screen, detail, "No template families are available for this workspace.");
            return;
        }

        String templateKey = editor.selectedTemplateFamilyTemplateKey();
        if (templateKey != null) {
            if (!templateKey.equals(screen.selectedTopologyKey())) {
                screen.selectWorkspaceTopologySlot(templateKey);
            }
            if (!screen.topologySlotEditor().selectedPieces().isEmpty()) {
                WorkspaceTemplateDetailPane.addTemplateControls(screen, detail, () -> {
                    editor.selectedTemplateFamilyTemplateKey(null);
                    screen.clearSelectedTopologyKey();
                    screen.flagNeedSetup();
                }, false);
                return;
            }
            editor.selectedTemplateFamilyTemplateKey(null);
            screen.clearSelectedTopologyKey();
        }

        String editId = editor.selectedTemplateFamilyEditId();
        if (editId != null && selected.kind() == TemplateFamilyKind.ROOM_SLOT &&
                editId.startsWith("family:")) {
            int familyIndex = parseIndex(editId.substring("family:".length()));
            List<MKWorkspaceRoomFamilyDefinition> families = editor.familyDefinitions();
            if (familyIndex >= 0 && familyIndex < families.size() &&
                    families.get(familyIndex).topologySlotId().equals(selected.slot().slotId())) {
                new WorkspaceFormFamilyDetailPage().addInlineFamilyDetailControls(screen, detail, familyIndex, () -> {
                    editor.selectedTemplateFamilyEditId(null);
                    editor.selectedFamilyExitIndex(-1);
                    screen.flagNeedSetup();
                });
                return;
            }
            editor.selectedTemplateFamilyEditId(null);
        }
        if (editId != null && selected.kind() == TemplateFamilyKind.INSERT_FAMILY &&
                editId.startsWith("insert:")) {
            int insertIndex = parseIndex(editId.substring("insert:".length()));
            List<MKWorkspaceInsertFamilyDefinition> insertFamilies = editor.insertFamilies();
            if (insertIndex >= 0 && insertIndex < insertFamilies.size()) {
                new WorkspaceFormInsertFamilyDetailPage().addInlineInsertFamilyDetailControls(screen, detail,
                        insertIndex, () -> {
                            editor.selectedTemplateFamilyEditId(null);
                            screen.flagNeedSetup();
                        });
                return;
            }
            editor.selectedTemplateFamilyEditId(null);
        }

        addSectionHeader(screen, detail, selected.label());
        addDetailText(screen, detail, selected.summary());
        switch (selected.kind()) {
            case ROOM_SLOT -> addRoomSlotFamily(screen, detail, editor, selected.slot());
            case LINEAR_RUN -> addLinearRunFamily(screen, detail, editor,
                    selected.linearRun(), selected.linearRunIndex());
            case INSERT_FAMILY -> addInsertFamily(screen, detail, editor, selected.insertFamily(),
                    selected.insertFamilyIndex());
            case EXTRA -> addTemplateFamilyDisplay(screen, detail, selected.display());
        }
    }

    private void addSectionHeader(MKWorkspaceScreen screen, MKStackLayoutVertical content, String label) {
        MKText header = screen.makeWhiteText(Component.literal(label));
        header.setWidth(contentTextWidth(content));
        header.setMultiline(true);
        content.addWidget(header);
        content.addConstraintToWidget(MarginConstraint.LEFT, header);
    }

    private void addSelectorHeader(MKWorkspaceScreen screen, MKStackLayoutVertical content, String label) {
        MKText header = screen.makeWhiteText(Component.literal(label));
        header.setWidth(SELECTOR_WIDTH - 18);
        header.setMultiline(true);
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
        summary.setWidth(contentTextWidth(content));
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
            editor.selectedTemplateFamilyEditId("family:" + editor.selectedFamilyIndex());
            editor.selectedTemplateFamilyTemplateKey(null);
            screen.clearSelectedTopologyKey();
            screen.flagNeedSetup();
            return true;
        });
    }

    private void addRoomFamily(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                               WorkspaceDraftSession editor, MKWorkspaceRoomFamilyDefinition family, int index) {
        MKWorkspaceTopologySlotMetadata slotMetadata = MKWorkspaceTopologySlotMetadata.fromFamily(family);
        List<String> baseNames = editor.plannerAdapter().templateBaseNamesForFamily(editor, family);
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
                        formatTopologyLabel(slotMetadata.pieceKind()) + "  |  shaft " +
                        (family.supportsVerticalAccess() ? "yes" : "no") + "  |  " +
                        authoredPieceSummary(authoredPieces)));
        familySummary.setWidth(contentTextWidth(content));
        familySummary.setMultiline(true);
        content.addWidget(familySummary);
        content.addConstraintToWidget(MarginConstraint.LEFT, familySummary);

        MKButton editButton = new MKButton(Component.literal("Edit Family"), 180, screen.buttonHeight());
        content.addWidget(editButton);
        content.addConstraintToWidget(new CenterXConstraint(), editButton);
        editButton.setPressedCallback((button, mouseButton) -> {
            editor.selectedFamilyIndex(index);
            editor.selectedFamilyExitIndex(-1);
            editor.selectedTemplateFamilyEditId("family:" + index);
            editor.selectedTemplateFamilyTemplateKey(null);
            screen.clearSelectedTopologyKey();
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

        addTeleportToRowButton(screen, content, authoredPieces);
    }

    private void addLinearRunFamily(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                    WorkspaceDraftSession editor, MKWorkspaceLinearRunFamilyDefinition linearRun,
                                    int index) {
        List<MKWorkspacePieceDefinition> authoredPieces = authoredPiecesForLinearRuns(screen,
                List.of(linearRun.linearRunId()));
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
        summary.setWidth(contentTextWidth(content));
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
            openTemplatesForPrefix(screen, linearRun.linearRunId());
            return true;
        });

        addTeleportToRowButton(screen, content, authoredPieces);
    }

    private void addTemplateFamilyDisplay(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                          WorkspaceTemplateFamilyDisplay display) {
        List<MKWorkspacePieceDefinition> authoredPieces = display.usesLinearRunFamily() ?
                authoredPiecesForLinearRuns(screen, List.of(display.linearRunFamilyId())) :
                authoredPiecesForBaseNames(screen, display.templateBaseNames());
        MKText header = screen.makeWhiteText(Component.literal(display.label()));
        content.addWidget(header);
        content.addConstraintToWidget(MarginConstraint.LEFT, header);

        MKText summary = screen.makeWhiteText(Component.literal(
                display.summary() + "  |  " + authoredPieceSummary(authoredPieces)));
        summary.setWidth(contentTextWidth(content));
        summary.setMultiline(true);
        content.addWidget(summary);
        content.addConstraintToWidget(MarginConstraint.LEFT, summary);

        MKButton openTemplates = new MKButton(Component.literal("Open Templates"), 180, screen.buttonHeight());
        openTemplates.setEnabled(!authoredPieces.isEmpty());
        content.addWidget(openTemplates);
        content.addConstraintToWidget(new CenterXConstraint(), openTemplates);
        openTemplates.setPressedCallback((button, mouseButton) -> {
            if (display.usesLinearRunFamily()) {
                openTemplatesForPrefix(screen, display.linearRunFamilyId());
            } else {
                openTemplatesForBaseNames(screen, display.templateBaseNames());
            }
            return true;
        });

        addTeleportToRowButton(screen, content, authoredPieces);
    }

    private void addInsertFamily(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                 WorkspaceDraftSession editor,
                                 MKWorkspaceInsertFamilyDefinition insertFamily, int index) {
        List<MKWorkspacePieceDefinition> authoredPieces = authoredPiecesForInsertFamily(screen,
                insertFamily.familyId());
        MKText header = screen.makeWhiteText(Component.literal(insertFamily.familyId()));
        content.addWidget(header);
        content.addConstraintToWidget(MarginConstraint.LEFT, header);

        MKText summary = screen.makeWhiteText(Component.literal(
                formatInsertFamilySummary(insertFamily) + "  |  " + authoredPieceSummary(authoredPieces)));
        summary.setWidth(contentTextWidth(content));
        summary.setMultiline(true);
        content.addWidget(summary);
        content.addConstraintToWidget(MarginConstraint.LEFT, summary);

        MKButton editButton = new MKButton(Component.literal("Edit Insert"), 180, screen.buttonHeight());
        content.addWidget(editButton);
        content.addConstraintToWidget(new CenterXConstraint(), editButton);
        editButton.setPressedCallback((button, mouseButton) -> {
            editor.selectedInsertFamilyIndex(index);
            editor.selectedTemplateFamilyEditId("insert:" + index);
            editor.selectedTemplateFamilyTemplateKey(null);
            screen.clearSelectedTopologyKey();
            screen.flagNeedSetup();
            return true;
        });

        MKButton openTemplates = new MKButton(Component.literal("Open Templates"), 180, screen.buttonHeight());
        openTemplates.setEnabled(!authoredPieces.isEmpty());
        content.addWidget(openTemplates);
        content.addConstraintToWidget(new CenterXConstraint(), openTemplates);
        openTemplates.setPressedCallback((button, mouseButton) -> {
            openTemplatesForInsertFamily(screen, insertFamily.familyId());
            return true;
        });

        addTeleportToRowButton(screen, content, authoredPieces);
    }

    private void addTeleportToRowButton(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                        List<MKWorkspacePieceDefinition> authoredPieces) {
        MKButton teleport = new MKButton(Component.literal("Teleport To Row"), 180, screen.buttonHeight());
        teleport.setEnabled(!authoredPieces.isEmpty());
        content.addWidget(teleport);
        content.addConstraintToWidget(new CenterXConstraint(), teleport);
        teleport.setPressedCallback((button, mouseButton) -> {
            MKWorkspacePieceDefinition targetPiece = authoredPieces.stream()
                    .filter(piece -> piece.variantIndex() == 0)
                    .findFirst()
                    .orElse(authoredPieces.getFirst());
            PacketDistributor.sendToServer(new TeleportToWorkspacePiecePacket(screen.anchor(), targetPiece.pieceId()));
            if (!screen.draftSession().dirty()) {
                screen.closeScreen();
            }
            return true;
        });
    }

    private void addAddInsertFamilyButton(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                          WorkspaceDraftSession editor) {
        MKButton addInsertFamily = new MKButton(Component.literal("Add Insert"), 180, screen.buttonHeight());
        content.addWidget(addInsertFamily);
        content.addConstraintToWidget(new CenterXConstraint(), addInsertFamily);
        addInsertFamily.setPressedCallback((button, mouseButton) -> {
            editor.selectedInsertFamilyIndex(editor.addInsertFamily());
            editor.selectedTemplateFamilyId("insert:" + editor.selectedInsertFamilyIndex());
            editor.selectedTemplateFamilyEditId("insert:" + editor.selectedInsertFamilyIndex());
            editor.selectedTemplateFamilyTemplateKey(null);
            screen.clearSelectedTopologyKey();
            screen.flagNeedSetup();
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
            editor.selectedTemplateFamilyId("run:" + editor.selectedLinearRunIndex());
            screen.pushState(WorkspaceFormLinearRunDetailPage.ID);
            screen.flagNeedSetup();
            return true;
        });
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
            if (selectTemplatesForBaseName(screen, baseName)) {
                return;
            }
        }
    }

    private boolean selectTemplatesForBaseName(MKWorkspaceScreen screen, String baseName) {
        if (screen.workspace() == null) {
            return false;
        }
        for (Map.Entry<String, List<MKWorkspacePieceDefinition>> entry :
                WorkspacePieceDisplay.groupAuthoredPiecesByTopology(screen.workspace()).entrySet()) {
            List<MKWorkspacePieceDefinition> pieces = screen.draftSession()
                    .filterPendingDeletedVariants(entry.getValue());
            if (pieces.stream().anyMatch(piece -> baseName.equals(WorkspacePieceDisplay.getBaseName(piece)))) {
                selectInlineTemplateGroup(screen, entry.getKey());
                return true;
            }
        }
        return false;
    }

    private boolean openTemplatesForPrefix(MKWorkspaceScreen screen, String topologyPrefix) {
        if (screen.workspace() == null) {
            return false;
        }
        for (Map.Entry<String, List<MKWorkspacePieceDefinition>> entry :
                WorkspacePieceDisplay.groupAuthoredPiecesByTopology(screen.workspace()).entrySet()) {
            List<MKWorkspacePieceDefinition> pieces = screen.draftSession()
                    .filterPendingDeletedVariants(entry.getValue());
            if (pieces.stream().anyMatch(piece -> pieceMatchesTopologyPrefix(piece, topologyPrefix))) {
                selectInlineTemplateGroup(screen, entry.getKey());
                return true;
            }
        }
        return false;
    }

    private void openTemplatesForInsertFamily(MKWorkspaceScreen screen, String insertFamilyId) {
        if (selectTemplatesForInsertFamily(screen, insertFamilyId)) {
            return;
        }
        openTemplatesForBaseNames(screen, List.of(insertFamilyId));
    }

    private boolean selectTemplatesForInsertFamily(MKWorkspaceScreen screen, String insertFamilyId) {
        if (screen.workspace() == null) {
            return false;
        }
        for (Map.Entry<String, List<MKWorkspacePieceDefinition>> entry :
                WorkspacePieceDisplay.groupAuthoredPiecesByTopology(screen.workspace()).entrySet()) {
            List<MKWorkspacePieceDefinition> pieces = screen.draftSession()
                    .filterPendingDeletedVariants(entry.getValue());
            if (pieces.stream().anyMatch(piece -> insertFamilyId.equals(piece.tags()
                    .get(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_ID)))) {
                selectInlineTemplateGroup(screen, entry.getKey());
                return true;
            }
        }
        return false;
    }

    private void selectInlineTemplateGroup(MKWorkspaceScreen screen, String topologyKey) {
        screen.selectWorkspaceTopologySlot(topologyKey);
        screen.draftSession().selectedTemplateFamilyTemplateKey(topologyKey);
        screen.draftSession().selectedTemplateFamilyEditId(null);
        screen.flagNeedSetup();
    }

    private boolean pieceMatchesTopologyPrefix(MKWorkspacePieceDefinition piece, String topologyPrefix) {
        String topologySlotId = piece.tags().get("workspace_topology_slot_id");
        String topologyGroup = piece.tags().get("workspace_topology_group");
        String linearRunFamilyId = piece.tags().get("workspace_linear_run_family_id");
        return matchesPrefix(topologySlotId, topologyPrefix) ||
                matchesPrefix(topologyGroup, topologyPrefix) ||
                matchesPrefix(linearRunFamilyId, topologyPrefix) ||
                matchesPrefix(piece.roleId(), topologyPrefix);
    }

    private boolean matchesPrefix(String value, String prefix) {
        return value != null && (value.equals(prefix) || value.startsWith(prefix + "."));
    }

    private List<MKWorkspacePieceDefinition> authoredPiecesForLinearRuns(MKWorkspaceScreen screen,
                                                                         List<String> linearRunIds) {
        if (screen.workspace() == null) {
            return List.of();
        }
        return screen.workspace().pieces().stream()
                .filter(WorkspacePieceDisplay::isAuthoredTemplatePiece)
                .filter(piece -> {
                    String linearRunId = piece.tags().get("workspace_linear_run_family_id");
                    return linearRunId != null && linearRunIds.contains(linearRunId);
                })
                .toList();
    }

    private List<MKWorkspacePieceDefinition> authoredPiecesForInsertFamily(MKWorkspaceScreen screen,
                                                                           String insertFamilyId) {
        if (screen.workspace() == null) {
            return List.of();
        }
        return screen.workspace().pieces().stream()
                .filter(WorkspacePieceDisplay::isAuthoredTemplatePiece)
                .filter(piece -> insertFamilyId.equals(piece.tags()
                        .get(MKWorkspaceInsertFamilyDefinition.TAG_INSERT_FAMILY_ID)) ||
                        insertFamilyId.equals(WorkspacePieceDisplay.getBaseName(piece)))
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

    private String formatInsertFamilySummary(MKWorkspaceInsertFamilyDefinition insertFamily) {
        return WorkspacePieceDisplay.formatTopologyLabel(insertFamily.kind().getSerializedName()) +
                "  |  " + insertFamily.width() + "x" + insertFamily.height() + "x" +
                insertFamily.depth();
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

    private String formatTopologyLabel(String key) {
        return WorkspacePieceDisplay.formatTopologyLabel(key);
    }

    private String truncateSelectorLabel(String label) {
        return label.length() <= 24 ? label : label.substring(0, 21) + "...";
    }

    private int parseIndex(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private int contentTextWidth(MKStackLayoutVertical content) {
        return Math.max(120, content.getWidth() - 16);
    }

    private void addDetailText(MKWorkspaceScreen screen, MKStackLayoutVertical content, String label) {
        MKText text = screen.makeWhiteText(Component.literal(label));
        text.setWidth(contentTextWidth(content));
        text.setMultiline(true);
        content.addWidget(text);
        content.addConstraintToWidget(MarginConstraint.LEFT, text);
    }

    private enum TemplateFamilyKind {
        ROOM_SLOT,
        LINEAR_RUN,
        INSERT_FAMILY,
        EXTRA
    }

    private record TemplateFamilyEntry(
            String id,
            TemplateFamilyKind kind,
            String label,
            String summary,
            MKWorkspaceSlotSchema slot,
            MKWorkspaceLinearRunFamilyDefinition linearRun,
            int linearRunIndex,
            MKWorkspaceInsertFamilyDefinition insertFamily,
            int insertFamilyIndex,
            WorkspaceTemplateFamilyDisplay display
    ) {
        static TemplateFamilyEntry roomSlot(MKWorkspaceSlotSchema slot) {
            return new TemplateFamilyEntry(
                    "slot:" + slot.slotId(),
                    TemplateFamilyKind.ROOM_SLOT,
                    WorkspacePieceDisplay.formatTopologyLabel(slot.slotId()),
                    WorkspacePieceDisplay.formatTopologyLabel(slot.slotKind()) + " / " +
                            WorkspacePieceDisplay.formatTopologyLabel(
                                    slot.repeat().name().toLowerCase(Locale.ROOT)),
                    slot,
                    null,
                    -1,
                    null,
                    -1,
                    null
            );
        }

        static TemplateFamilyEntry linearRun(MKWorkspaceLinearRunFamilyDefinition linearRun, int index) {
            return new TemplateFamilyEntry(
                    "run:" + index,
                    TemplateFamilyKind.LINEAR_RUN,
                    WorkspacePieceDisplay.formatTopologyLabel(linearRun.linearRunId()),
                    WorkspacePieceDisplay.formatTopologyLabel(linearRun.kind().getSerializedName()) + " / " +
                            linearRun.topologySlotId(),
                    null,
                    linearRun,
                    index,
                    null,
                    -1,
                    null
            );
        }

        static TemplateFamilyEntry insertFamily(MKWorkspaceInsertFamilyDefinition insertFamily, int index) {
            return new TemplateFamilyEntry(
                    "insert:" + index,
                    TemplateFamilyKind.INSERT_FAMILY,
                    insertFamily.familyId(),
                    WorkspacePieceDisplay.formatTopologyLabel(insertFamily.kind().getSerializedName()) + " / " +
                            insertFamily.width() + "x" + insertFamily.height() + "x" + insertFamily.depth(),
                    null,
                    null,
                    -1,
                    insertFamily,
                    index,
                    null
            );
        }

        static TemplateFamilyEntry extra(WorkspaceTemplateFamilyDisplay display, int index) {
            return new TemplateFamilyEntry(
                    "extra:" + index,
                    TemplateFamilyKind.EXTRA,
                    display.label(),
                    display.summary(),
                    null,
                    null,
                    -1,
                    null,
                    -1,
                    display
            );
        }
    }
}


