package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspace.network.packets.AddWorkspaceVariantPacket;
import com.chaosbuffalo.mkworkspace.network.packets.ClearWorkspaceStairsPacket;
import com.chaosbuffalo.mkworkspace.network.packets.GenerateWorkspaceStairsPacket;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.StackConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class WorkspaceTopologySlotPage extends WorkspacePageBase {
    public static final String ID = "topology_slot";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        WorkspaceTopologySlotEditor editor = screen.topologySlotEditor();
        if (editor.selectedTopologyKey() == null) {
            screen.switchToExistingState(WorkspaceManagePage.ID);
            return new WorkspaceManagePage().build(screen);
        }

        List<MKWorkspacePieceDefinition> pieces = editor.selectedPieces();
        if (pieces.isEmpty()) {
            editor.clearSelection();
            screen.switchToExistingState(WorkspaceManagePage.ID);
            return new WorkspaceManagePage().build(screen);
        }

        boolean stairCategory = WorkspacePieceDisplay.supportsStairGeneration(pieces);
        MKWorkspacePieceDefinition templatePiece = pieces.stream()
                .filter(piece -> piece.variantIndex() == 0)
                .findFirst()
                .orElse(pieces.get(0));

        editor.ensureOverridesInitialized();
        editor.stairWidth(MKWorkspaceDimensions.snapToNearestAllowedStairWidth(
                editor.shaftWidth(), editor.stairWidth()));

        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal(WorkspacePieceDisplay.buildWorkspaceGroupLabel(templatePiece)));

        MKText summary = screen.makeWhiteText(Component.literal(stairCategory
                ? "Manage variants and generate stairs into the shaft for an exact template or variant."
                : "Manage variants for this template set."));
        summary.setWidth(screen.contentWidth());
        summary.setMultiline(true);
        root.addWidget(summary);
        root.addConstraintToWidget(StackConstraint.VERTICAL, summary);
        root.addConstraintToWidget(new CenterXConstraint(), summary);

        int buttonCount = stairCategory ? 3 : 2;
        int buttonAreaHeight = (buttonCount * screen.buttonHeight()) +
                screen.buttonGap() + screen.bottomPadding();
        int paletteAreaHeight = 0;
        int scrollTop = screen.scrollTopAfterHeader(root, summary);
        int scrollHeight = screen.panelY() + screen.panelHeight() - buttonAreaHeight -
                paletteAreaHeight - 12 - scrollTop;
        MKScrollView scrollView = new MKScrollView(screen.panelX() + 10, scrollTop,
                screen.scrollWidth(), scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = createContentStack(screen);
        if (stairCategory) {
            addStairControls(screen, editor, content);
        }

        for (MKWorkspacePieceDefinition piece : pieces) {
            addPieceControls(screen, editor, content, stairCategory, piece);
        }

        finishScrollContent(screen, scrollView, content);

        String baseName = WorkspacePieceDisplay.getBaseName(templatePiece);
        if (stairCategory) {
            MKButton reset = addBottomButton(screen, root, Component.literal("Use Workspace Defaults"), 180, 1);
            reset.setPressedCallback((button, mouseButton) -> {
                editor.resetOverrides();
                screen.flagNeedSetup();
                return true;
            });

            MKButton addCopy = addBottomButton(screen, root,
                    Component.translatable("mknpc.workspace.button.add_copy"), 180, 2);
            addCopy.setPressedCallback((button, mouseButton) -> {
                PacketDistributor.sendToServer(new AddWorkspaceVariantPacket(screen.anchor(), baseName));
                return true;
            });
        } else {
            MKButton addCopy = addBottomButton(screen, root,
                    Component.translatable("mknpc.workspace.button.add_copy"), 180, 1);
            addCopy.setPressedCallback((button, mouseButton) -> {
                PacketDistributor.sendToServer(new AddWorkspaceVariantPacket(screen.anchor(), baseName));
                return true;
            });
        }

        MKButton back = addBottomButton(screen, root, Component.literal("Back"), 120, 0);
        back.setPressedCallback((button, mouseButton) -> {
            editor.clearSelection();
            screen.goBackOrSwitchTo(WorkspaceManagePage.ID);
            return true;
        });

        return root;
    }

    private void addStairControls(MKWorkspaceScreen screen, WorkspaceTopologySlotEditor editor,
                                  MKStackLayoutVertical content) {
        MKText stairModeLabel = screen.makeWhiteText(Component.translatable("mknpc.workspace.field.stair_mode"));
        MKButton stairModeButton = new MKButton(getStairModeComponent(editor.stairMode()), 180,
                screen.buttonHeight());
        addRow(content, stairModeLabel, stairModeButton);
        stairModeButton.setPressedCallback((button, mouseButton) -> {
            editor.stairMode(cycleStairMode(editor.stairMode(), isReverseClick(mouseButton)));
            button.buttonText = getStairModeComponent(editor.stairMode());
            return true;
        });

        MKText riseTypeLabel = screen.makeWhiteText(Component.literal("Rise Type"));
        MKButton riseTypeButton = new MKButton(getStairRiseTypeComponent(editor.stairRiseType()), 180,
                screen.buttonHeight());
        addRow(content, riseTypeLabel, riseTypeButton);
        riseTypeButton.setPressedCallback((button, mouseButton) -> {
            editor.stairRiseType(cycleValue(List.of(MKWorkspaceStairRiseType.values()),
                    editor.stairRiseType(), isReverseClick(mouseButton)));
            button.buttonText = getStairRiseTypeComponent(editor.stairRiseType());
            return true;
        });

        MKText widthLabel = screen.makeWhiteText(Component.literal("Stair Width"));
        MKButton widthButton = new MKButton(Component.literal(Integer.toString(editor.stairWidth())), 180,
                screen.buttonHeight());
        addRow(content, widthLabel, widthButton);
        widthButton.setPressedCallback((button, mouseButton) -> {
            editor.stairWidth(cycleAllowedStairWidth(editor.shaftWidth(), editor.stairWidth(),
                    isReverseClick(mouseButton)));
            button.buttonText = Component.literal(Integer.toString(editor.stairWidth()));
            return true;
        });
    }

    private void addPieceControls(MKWorkspaceScreen screen, WorkspaceTopologySlotEditor editor,
                                  MKStackLayoutVertical content, boolean stairCategory,
                                  MKWorkspacePieceDefinition piece) {
        MKText pieceText = screen.makeWhiteText(Component.literal(WorkspacePieceDisplay.describePiece(piece)));
        pieceText.setWidth(screen.contentWidth());
        content.addWidget(pieceText);
        content.addConstraintToWidget(MarginConstraint.LEFT, pieceText);

        if (WorkspacePieceDisplay.supportsStairGeneration(piece)) {
            MKText stairStatus = screen.makeWhiteText(Component.literal("Stairs: " +
                    (WorkspacePieceDisplay.hasGeneratedStairs(piece) ? "Generated" : "Not Generated")));
            stairStatus.setWidth(screen.contentWidth());
            content.addWidget(stairStatus);
            content.addConstraintToWidget(MarginConstraint.LEFT, stairStatus);
        }

        String sourcePieceName = piece.pieceName();
        String sourceBaseName = WorkspacePieceDisplay.getBaseName(piece);
        MKButton copyVariant = new MKButton(Component.literal("Copy This Variant"), 180, screen.buttonHeight());
        content.addWidget(copyVariant);
        content.addConstraintToWidget(new CenterXConstraint(), copyVariant);
        copyVariant.setPressedCallback((button, mouseButton) -> {
            PacketDistributor.sendToServer(new AddWorkspaceVariantPacket(screen.anchor(),
                    sourceBaseName, sourcePieceName));
            return true;
        });

        if (stairCategory && WorkspacePieceDisplay.supportsStairGeneration(piece)) {
            String pieceName = piece.pieceName();
            MKButton generateStairs = new MKButton(Component.literal("Generate Stairs"), 180, screen.buttonHeight());
            content.addWidget(generateStairs);
            content.addConstraintToWidget(new CenterXConstraint(), generateStairs);
            generateStairs.setPressedCallback((button, mouseButton) -> {
                PacketDistributor.sendToServer(new GenerateWorkspaceStairsPacket(screen.anchor(), pieceName,
                        editor.stairMode(), editor.stairRiseType(), editor.stairWidth()));
                return true;
            });

            MKButton clearStairs = new MKButton(Component.literal("Clear Stairs"), 180, screen.buttonHeight());
            content.addWidget(clearStairs);
            content.addConstraintToWidget(new CenterXConstraint(), clearStairs);
            clearStairs.setPressedCallback((button, mouseButton) -> {
                PacketDistributor.sendToServer(new ClearWorkspaceStairsPacket(screen.anchor(), pieceName));
                return true;
            });
        }
    }

    private void addRow(MKStackLayoutVertical root, MKText label, MKButton button) {
        root.addWidget(label);
        root.addConstraintToWidget(MarginConstraint.LEFT, label);
        root.addWidget(button);
        root.addConstraintToWidget(new CenterXConstraint(), button);
    }

    private Component getStairModeComponent(MKWorkspaceStairMode mode) {
        return Component.literal(WorkspacePieceDisplay.formatTopologyLabel(mode.getSerializedName()));
    }

    private Component getStairRiseTypeComponent(MKWorkspaceStairRiseType riseType) {
        return Component.literal(WorkspacePieceDisplay.formatTopologyLabel(riseType.getSerializedName()));
    }

    private boolean isReverseClick(int mouseButton) {
        return mouseButton == 1;
    }

    private MKWorkspaceStairMode cycleStairMode(MKWorkspaceStairMode current, boolean reverse) {
        return cycleValue(List.of(
                MKWorkspaceStairMode.AUTO,
                MKWorkspaceStairMode.RUN_PROFILE,
                MKWorkspaceStairMode.LADDER,
                MKWorkspaceStairMode.NONE
        ), current, reverse);
    }

    private <T> T cycleValue(List<T> values, T current, boolean reverse) {
        return cycleValue(values, current, reverse, values.isEmpty() ? current : values.getFirst());
    }

    private <T> T cycleValue(List<T> values, T current, boolean reverse, T fallback) {
        if (values.isEmpty()) {
            return fallback;
        }
        int index = values.indexOf(current);
        if (index < 0) {
            return values.getFirst();
        }
        int nextIndex = Math.floorMod(index + (reverse ? -1 : 1), values.size());
        return values.get(nextIndex);
    }

    private int cycleAllowedStairWidth(int shaftWidth, int currentWidth, boolean reverse) {
        List<Integer> allowedWidths = MKWorkspaceDimensions.getAllowedStairWidths(shaftWidth);
        int snapped = MKWorkspaceDimensions.snapToNearestAllowedStairWidth(shaftWidth, currentWidth);
        return cycleValue(allowedWidths, snapped, reverse, currentWidth);
    }
}
