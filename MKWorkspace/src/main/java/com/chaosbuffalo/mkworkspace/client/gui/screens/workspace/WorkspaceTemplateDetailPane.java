package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspace.network.packets.ClearWorkspaceStairsPacket;
import com.chaosbuffalo.mkworkspace.network.packets.GenerateWorkspaceStairsPacket;
import com.chaosbuffalo.mkworkspace.network.packets.TeleportToWorkspacePiecePacket;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

final class WorkspaceTemplateDetailPane {
    private WorkspaceTemplateDetailPane() {
    }

    static void addTemplateControls(MKWorkspaceScreen screen, MKStackLayoutVertical content, Runnable backAction) {
        addTemplateControls(screen, content, backAction, true);
    }

    static void addTemplateControls(MKWorkspaceScreen screen, MKStackLayoutVertical content, Runnable backAction,
                                    boolean showBackButton) {
        WorkspaceTopologySlotEditor editor = screen.topologySlotEditor();
        List<MKWorkspacePieceDefinition> pieces = editor.selectedPieces();
        if (editor.selectedTopologyKey() == null || pieces.isEmpty()) {
            if (showBackButton) {
                addBackButton(screen, content, backAction);
            }
            addText(screen, content, "No authored templates are available for this selection.");
            return;
        }

        boolean stairCategory = WorkspacePieceDisplay.supportsStairGeneration(pieces);
        MKWorkspacePieceDefinition templatePiece = pieces.stream()
                .filter(piece -> piece.variantIndex() == 0)
                .findFirst()
                .orElse(pieces.getFirst());

        editor.ensureOverridesInitialized();
        editor.stairWidth(MKWorkspaceDimensions.snapToNearestAllowedStairWidth(
                editor.shaftWidth(), editor.stairWidth()));

        if (showBackButton) {
            addBackButton(screen, content, backAction);
        }
        addSectionHeader(screen, content, WorkspacePieceDisplay.buildWorkspaceGroupLabel(templatePiece));
        addText(screen, content, stairCategory
                ? "Manage variants and generate stairs into the shaft for an exact template or variant."
                : "Manage variants for this template set.");

        String baseName = WorkspacePieceDisplay.getBaseName(templatePiece);
        MKButton addCopy = new MKButton(Component.translatable("mknpc.workspace.button.add_copy"), 180,
                screen.buttonHeight());
        content.addWidget(addCopy);
        content.addConstraintToWidget(new CenterXConstraint(), addCopy);
        addCopy.setPressedCallback((button, mouseButton) -> {
            screen.draftSession().stageVariantAddition(baseName);
            screen.flagNeedSetup();
            return true;
        });

        if (stairCategory) {
            MKButton reset = new MKButton(Component.literal("Use Workspace Defaults"), 180,
                    screen.buttonHeight());
            content.addWidget(reset);
            content.addConstraintToWidget(new CenterXConstraint(), reset);
            reset.setPressedCallback((button, mouseButton) -> {
                editor.resetOverrides();
                screen.flagNeedSetup();
                return true;
            });
            addStairControls(screen, editor, content);
        }

        for (MKWorkspacePieceDefinition piece : pieces) {
            addPieceControls(screen, editor, content, stairCategory, piece);
        }
    }

    private static void addBackButton(MKWorkspaceScreen screen, MKStackLayoutVertical content, Runnable backAction) {
        MKButton back = new MKButton(Component.literal("Back To Family"), 180, screen.buttonHeight());
        content.addWidget(back);
        content.addConstraintToWidget(new CenterXConstraint(), back);
        back.setPressedCallback((button, mouseButton) -> {
            backAction.run();
            return true;
        });
    }

    private static void addStairControls(MKWorkspaceScreen screen, WorkspaceTopologySlotEditor editor,
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

    private static void addPieceControls(MKWorkspaceScreen screen, WorkspaceTopologySlotEditor editor,
                                         MKStackLayoutVertical content, boolean stairCategory,
                                         MKWorkspacePieceDefinition piece) {
        MKText pieceText = screen.makeWhiteText(Component.literal(WorkspacePieceDisplay.describePiece(piece)));
        pieceText.setWidth(contentTextWidth(content));
        pieceText.setMultiline(true);
        content.addWidget(pieceText);
        content.addConstraintToWidget(MarginConstraint.LEFT, pieceText);

        if (WorkspacePieceDisplay.supportsStairGeneration(piece)) {
            addText(screen, content, "Stairs: " +
                    (WorkspacePieceDisplay.hasGeneratedStairs(piece) ? "Generated" : "Not Generated"));
        }

        String sourcePieceName = piece.pieceName();
        String sourceBaseName = WorkspacePieceDisplay.getBaseName(piece);
        MKButton teleport = new MKButton(Component.literal(piece.variantIndex() == 0 ?
                "Teleport To Template" : "Teleport To Variant"), 180, screen.buttonHeight());
        content.addWidget(teleport);
        content.addConstraintToWidget(new CenterXConstraint(), teleport);
        teleport.setPressedCallback((button, mouseButton) -> {
            PacketDistributor.sendToServer(new TeleportToWorkspacePiecePacket(screen.anchor(), piece.pieceId()));
            if (!screen.draftSession().dirty()) {
                screen.closeScreen();
            }
            return true;
        });

        MKButton copyVariant = new MKButton(Component.literal("Copy This Variant"), 180, screen.buttonHeight());
        content.addWidget(copyVariant);
        content.addConstraintToWidget(new CenterXConstraint(), copyVariant);
        copyVariant.setPressedCallback((button, mouseButton) -> {
            screen.draftSession().stageVariantAddition(sourceBaseName, sourcePieceName);
            screen.flagNeedSetup();
            return true;
        });

        MKButton newFamily = new MKButton(Component.literal("New Family From This"), 180,
                screen.buttonHeight());
        content.addWidget(newFamily);
        content.addConstraintToWidget(new CenterXConstraint(), newFamily);
        newFamily.setPressedCallback((button, mouseButton) -> {
            screen.draftSession().addFamilyDefinitionFromPiece(piece).ifPresent(selection -> {
                screen.draftSession().selectedTemplateFamilyId(selection.selectedFamilyId());
                screen.draftSession().selectedTemplateFamilyEditId(selection.editId());
                screen.draftSession().selectedTemplateFamilyTemplateKey(null);
                screen.draftSession().selectedFamilyExitIndex(-1);
                screen.clearSelectedTopologyKey();
            });
            screen.flagNeedSetup();
            return true;
        });

        if (piece.variantIndex() > 0) {
            MKButton deleteVariant = new MKButton(Component.literal("Delete Variant"), 180, screen.buttonHeight());
            content.addWidget(deleteVariant);
            content.addConstraintToWidget(new CenterXConstraint(), deleteVariant);
            deleteVariant.setPressedCallback((button, mouseButton) -> {
                screen.draftSession().stageVariantDeletion(piece.pieceId());
                screen.flagNeedSetup();
                return true;
            });
        }

        if (stairCategory && WorkspacePieceDisplay.supportsStairGeneration(piece)) {
            String pieceName = piece.pieceName();
            MKButton generateStairs = new MKButton(Component.literal("Generate Stairs"), 180,
                    screen.buttonHeight());
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

    private static void addRow(MKStackLayoutVertical root, MKText label, MKButton button) {
        root.addWidget(label);
        root.addConstraintToWidget(MarginConstraint.LEFT, label);
        root.addWidget(button);
        root.addConstraintToWidget(new CenterXConstraint(), button);
    }

    private static void addSectionHeader(MKWorkspaceScreen screen, MKStackLayoutVertical content, String label) {
        MKText header = screen.makeWhiteText(Component.literal(label));
        header.setWidth(contentTextWidth(content));
        header.setMultiline(true);
        content.addWidget(header);
        content.addConstraintToWidget(MarginConstraint.LEFT, header);
    }

    private static void addText(MKWorkspaceScreen screen, MKStackLayoutVertical content, String label) {
        MKText text = screen.makeWhiteText(Component.literal(label));
        text.setWidth(contentTextWidth(content));
        text.setMultiline(true);
        content.addWidget(text);
        content.addConstraintToWidget(MarginConstraint.LEFT, text);
    }

    private static Component getStairModeComponent(MKWorkspaceStairMode mode) {
        return Component.literal(WorkspacePieceDisplay.formatTopologyLabel(mode.getSerializedName()));
    }

    private static int contentTextWidth(MKStackLayoutVertical content) {
        return Math.max(120, content.getWidth() - 16);
    }

    private static Component getStairRiseTypeComponent(MKWorkspaceStairRiseType riseType) {
        return Component.literal(WorkspacePieceDisplay.formatTopologyLabel(riseType.getSerializedName()));
    }

    private static boolean isReverseClick(int mouseButton) {
        return mouseButton == 1;
    }

    private static MKWorkspaceStairMode cycleStairMode(MKWorkspaceStairMode current, boolean reverse) {
        return cycleValue(List.of(
                MKWorkspaceStairMode.AUTO,
                MKWorkspaceStairMode.RUN_PROFILE,
                MKWorkspaceStairMode.LADDER,
                MKWorkspaceStairMode.NONE
        ), current, reverse);
    }

    private static <T> T cycleValue(List<T> values, T current, boolean reverse) {
        return cycleValue(values, current, reverse, values.isEmpty() ? current : values.getFirst());
    }

    private static <T> T cycleValue(List<T> values, T current, boolean reverse, T fallback) {
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

    private static int cycleAllowedStairWidth(int shaftWidth, int currentWidth, boolean reverse) {
        List<Integer> allowedWidths = MKWorkspaceDimensions.getAllowedStairWidths(shaftWidth);
        int snapped = MKWorkspaceDimensions.snapToNearestAllowedStairWidth(shaftWidth, currentWidth);
        return cycleValue(allowedWidths, snapped, reverse, currentWidth);
    }
}
