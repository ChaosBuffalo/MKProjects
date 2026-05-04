package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.network.packets.AddWorkspaceVariantPacket;
import com.chaosbuffalo.mknpc.network.packets.ClearWorkspaceStairsPacket;
import com.chaosbuffalo.mknpc.network.packets.GenerateWorkspaceStairsPacket;
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

public class WorkspaceCategoryPage extends WorkspacePageBase {
    public static final String ID = "category";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(WorkspacePageContext context) {
        WorkspaceCategoryEditor editor = context.categoryEditor();
        if (editor.selectedTopologyKey() == null) {
            context.switchToExistingState().accept(WorkspaceManagePage.ID);
            return new WorkspaceManagePage().build(context);
        }

        List<MKWorkspacePieceDefinition> pieces = editor.selectedPieces();
        if (pieces.isEmpty()) {
            editor.clearSelection();
            context.switchToExistingState().accept(WorkspaceManagePage.ID);
            return new WorkspaceManagePage().build(context);
        }

        boolean stairCategory = WorkspacePieceDisplay.supportsStairGeneration(pieces);
        MKWorkspacePieceDefinition templatePiece = pieces.stream()
                .filter(piece -> piece.variantIndex() == 0)
                .findFirst()
                .orElse(pieces.get(0));

        editor.ensureOverridesInitialized();
        editor.stairWidth(MKWorkspaceDimensions.snapToNearestAllowedStairWidth(
                editor.hallwayWidth(), editor.stairWidth()));

        MKLayout root = createPanel(context);

        addTitle(context, root, Component.literal(WorkspacePieceDisplay.buildWorkspaceGroupLabel(templatePiece)));

        MKText summary = context.makeWhiteText(Component.literal(stairCategory
                ? "Manage variants and generate stairs into the shaft for an exact template or variant."
                : "Manage variants for this template category."));
        summary.setWidth(context.contentWidth());
        summary.setMultiline(true);
        root.addWidget(summary);
        root.addConstraintToWidget(StackConstraint.VERTICAL, summary);
        root.addConstraintToWidget(new CenterXConstraint(), summary);

        int buttonCount = stairCategory ? 3 : 2;
        int buttonAreaHeight = (buttonCount * context.buttonHeight()) +
                context.buttonGap() + context.bottomPadding();
        int paletteAreaHeight = stairCategory ? 112 : 0;
        int scrollTop = context.scrollTopAfterHeader(root, summary);
        int scrollHeight = context.panelY() + context.panelHeight() - buttonAreaHeight -
                paletteAreaHeight - 12 - scrollTop;
        MKScrollView scrollView = new MKScrollView(context.panelX() + 10, scrollTop,
                context.scrollWidth(), scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = createContentStack(context);
        if (stairCategory) {
            addStairControls(context, editor, content);
        }

        for (MKWorkspacePieceDefinition piece : pieces) {
            addPieceControls(context, editor, content, stairCategory, piece);
        }

        finishScrollContent(context, scrollView, content);

        String baseName = WorkspacePieceDisplay.getBaseName(templatePiece);
        if (stairCategory) {
            int paletteTop = scrollTop + scrollHeight + 6;
            context.addBlockPickerRow().add(root, context.panelX(), paletteTop + 8, "Stair", editor.stairBlock(),
                    editor::stairBlock, false);
            context.addBlockPickerRow().add(root, context.panelX(), paletteTop + 42, "Slab", editor.slabBlock(),
                    editor::slabBlock, false);
            context.addBlockPickerRow().add(root, context.panelX(), paletteTop + 76, "Ladder", editor.ladderBlock(),
                    editor::ladderBlock, false);

            MKButton reset = addBottomButton(context, root, Component.literal("Use Workspace Defaults"), 180, 1);
            reset.setPressedCallback((button, mouseButton) -> {
                editor.resetOverrides();
                context.flagNeedSetup().run();
                return true;
            });

            MKButton addCopy = addBottomButton(context, root,
                    Component.translatable("mknpc.workspace.button.add_copy"), 180, 2);
            addCopy.setPressedCallback((button, mouseButton) -> {
                PacketDistributor.sendToServer(new AddWorkspaceVariantPacket(context.anchor(), baseName));
                return true;
            });
        } else {
            MKButton addCopy = addBottomButton(context, root,
                    Component.translatable("mknpc.workspace.button.add_copy"), 180, 1);
            addCopy.setPressedCallback((button, mouseButton) -> {
                PacketDistributor.sendToServer(new AddWorkspaceVariantPacket(context.anchor(), baseName));
                return true;
            });
        }

        MKButton back = addBottomButton(context, root, Component.literal("Back"), 120, 0);
        back.setPressedCallback((button, mouseButton) -> {
            editor.clearSelection();
            context.switchToExistingState().accept(WorkspaceManagePage.ID);
            return true;
        });

        return root;
    }

    private void addStairControls(WorkspacePageContext context, WorkspaceCategoryEditor editor,
                                  MKStackLayoutVertical content) {
        MKText stairModeLabel = context.makeWhiteText(Component.translatable("mknpc.workspace.field.stair_mode"));
        MKButton stairModeButton = new MKButton(getStairModeComponent(editor.stairMode()), 180,
                context.buttonHeight());
        addRow(content, stairModeLabel, stairModeButton);
        stairModeButton.setPressedCallback((button, mouseButton) -> {
            editor.stairMode(cycleStairMode(editor.stairMode(), isReverseClick(mouseButton)));
            button.buttonText = getStairModeComponent(editor.stairMode());
            return true;
        });

        MKText riseTypeLabel = context.makeWhiteText(Component.literal("Rise Type"));
        MKButton riseTypeButton = new MKButton(getStairRiseTypeComponent(editor.stairRiseType()), 180,
                context.buttonHeight());
        addRow(content, riseTypeLabel, riseTypeButton);
        riseTypeButton.setPressedCallback((button, mouseButton) -> {
            editor.stairRiseType(cycleValue(List.of(MKWorkspaceStairRiseType.values()),
                    editor.stairRiseType(), isReverseClick(mouseButton)));
            button.buttonText = getStairRiseTypeComponent(editor.stairRiseType());
            return true;
        });

        MKText widthLabel = context.makeWhiteText(Component.literal("Stair Width"));
        MKButton widthButton = new MKButton(Component.literal(Integer.toString(editor.stairWidth())), 180,
                context.buttonHeight());
        addRow(content, widthLabel, widthButton);
        widthButton.setPressedCallback((button, mouseButton) -> {
            editor.stairWidth(cycleAllowedStairWidth(editor.hallwayWidth(), editor.stairWidth(),
                    isReverseClick(mouseButton)));
            button.buttonText = Component.literal(Integer.toString(editor.stairWidth()));
            return true;
        });
    }

    private void addPieceControls(WorkspacePageContext context, WorkspaceCategoryEditor editor,
                                  MKStackLayoutVertical content, boolean stairCategory,
                                  MKWorkspacePieceDefinition piece) {
        MKText pieceText = context.makeWhiteText(Component.literal(WorkspacePieceDisplay.describePiece(piece)));
        pieceText.setWidth(context.contentWidth());
        content.addWidget(pieceText);
        content.addConstraintToWidget(MarginConstraint.LEFT, pieceText);

        if (WorkspacePieceDisplay.supportsStairGeneration(piece)) {
            MKText stairStatus = context.makeWhiteText(Component.literal("Stairs: " +
                    (WorkspacePieceDisplay.hasGeneratedStairs(piece) ? "Generated" : "Not Generated")));
            stairStatus.setWidth(context.contentWidth());
            content.addWidget(stairStatus);
            content.addConstraintToWidget(MarginConstraint.LEFT, stairStatus);
        }

        String sourcePieceName = piece.pieceName();
        String sourceBaseName = WorkspacePieceDisplay.getBaseName(piece);
        MKButton copyVariant = new MKButton(Component.literal("Copy This Variant"), 180, context.buttonHeight());
        content.addWidget(copyVariant);
        content.addConstraintToWidget(new CenterXConstraint(), copyVariant);
        copyVariant.setPressedCallback((button, mouseButton) -> {
            PacketDistributor.sendToServer(new AddWorkspaceVariantPacket(context.anchor(),
                    sourceBaseName, sourcePieceName));
            return true;
        });

        if (stairCategory && WorkspacePieceDisplay.supportsStairGeneration(piece)) {
            String pieceName = piece.pieceName();
            MKButton generateStairs = new MKButton(Component.literal("Generate Stairs"), 180, context.buttonHeight());
            content.addWidget(generateStairs);
            content.addConstraintToWidget(new CenterXConstraint(), generateStairs);
            generateStairs.setPressedCallback((button, mouseButton) -> {
                PacketDistributor.sendToServer(new GenerateWorkspaceStairsPacket(context.anchor(), pieceName,
                        editor.stairMode(), editor.stairRiseType(), editor.stairWidth(),
                        editor.stairBlock(), editor.slabBlock(), editor.ladderBlock()));
                return true;
            });

            MKButton clearStairs = new MKButton(Component.literal("Clear Stairs"), 180, context.buttonHeight());
            content.addWidget(clearStairs);
            content.addConstraintToWidget(new CenterXConstraint(), clearStairs);
            clearStairs.setPressedCallback((button, mouseButton) -> {
                PacketDistributor.sendToServer(new ClearWorkspaceStairsPacket(context.anchor(), pieceName));
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

    private int cycleAllowedStairWidth(int hallwayWidth, int currentWidth, boolean reverse) {
        List<Integer> allowedWidths = MKWorkspaceDimensions.getAllowedStairWidths(hallwayWidth);
        int snapped = MKWorkspaceDimensions.snapToNearestAllowedStairWidth(hallwayWidth, currentWidth);
        return cycleValue(allowedWidths, snapped, reverse, currentWidth);
    }
}
