package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspace.network.packets.GenerateWorkspaceSamplePreviewPacket;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeRequests;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations.MKWorkspaceSimpleChangeOperation;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceSamplePreviewState;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.LinkedHashSet;
import java.util.List;

public class WorkspaceUtilitiesPage extends WorkspacePageBase {
    public static final String ID = "utilities";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Utilities"));
        MKText summary = addHeaderText(screen, root, Component.literal(
                "Workspace-wide tools for live workspace maintenance."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, summary);

        MKStackLayoutVertical content = createContentStack(screen);

        MKButton blockSwap = new MKButton(Component.literal("Block Swap"), 180, 20);
        content.addWidget(blockSwap);
        content.addConstraintToWidget(new CenterXConstraint(), blockSwap);
        blockSwap.setPressedCallback((button, mouseButton) -> {
            screen.pushState("block_swap");
            screen.flagNeedSetup();
            return true;
        });

        MKButton backups = new MKButton(Component.literal("Backups (" +
                screen.backupManifestFiles().size() + ")"), 180, 20);
        content.addWidget(backups);
        content.addConstraintToWidget(new CenterXConstraint(), backups);
        backups.setPressedCallback((button, mouseButton) -> {
            screen.pushState(WorkspaceBackupPage.ID);
            screen.flagNeedSetup();
            return true;
        });

        if (screen.workspace().pieces().stream().anyMatch(screen::supportsStairGeneration)) {
            MKButton generateAllStairs = new MKButton(Component.literal("Generate All Stairs"), 180, 20);
            content.addWidget(generateAllStairs);
            content.addConstraintToWidget(new CenterXConstraint(), generateAllStairs);
            generateAllStairs.setPressedCallback((button, mouseButton) -> {
                screen.requestWorkspaceChange(MKWorkspaceChangeRequests.simple(
                        MKWorkspaceSimpleChangeOperation.Kind.GENERATE_ALL_STAIRS, screen.anchor()));
                return true;
            });
        }

        MKButton addCopyForAll = new MKButton(Component.literal("Add Variant For All"), 180, 20);
        content.addWidget(addCopyForAll);
        content.addConstraintToWidget(new CenterXConstraint(), addCopyForAll);
        addCopyForAll.setPressedCallback((button, mouseButton) -> {
            stageVariantAdditions(screen, physicalTemplateBasePieceNames(screen));
            return true;
        });

        MKButton addMissingVariants = new MKButton(Component.literal("Add Missing Variants"), 180, 20);
        content.addWidget(addMissingVariants);
        content.addConstraintToWidget(new CenterXConstraint(), addMissingVariants);
        addMissingVariants.setPressedCallback((button, mouseButton) -> {
            stageVariantAdditions(screen, basePieceNamesWithoutPhysicalVariants(screen));
            return true;
        });

        boolean[] lockSeed = new boolean[]{
                screen.samplePreviewState()
                        .map(MKWorkspaceSamplePreviewState::seedLocked)
                        .orElse(false)
        };
        MKButton lockSampleSeed = new MKButton(sampleSeedText(lockSeed[0]), 180, 20);
        content.addWidget(lockSampleSeed);
        content.addConstraintToWidget(new CenterXConstraint(), lockSampleSeed);
        lockSampleSeed.setPressedCallback((button, mouseButton) -> {
            lockSeed[0] = !lockSeed[0];
            button.buttonText = sampleSeedText(lockSeed[0]);
            return true;
        });

        MKButton generateSample = new MKButton(Component.literal("Generate Sample Preview"), 180, 20);
        content.addWidget(generateSample);
        content.addConstraintToWidget(new CenterXConstraint(), generateSample);
        generateSample.setPressedCallback((button, mouseButton) -> {
            PacketDistributor.sendToServer(new GenerateWorkspaceSamplePreviewPacket(screen.anchor(), lockSeed[0]));
            return true;
        });

        screen.samplePreviewState().ifPresent(state -> addText(screen, content,
                "Last sample preview: " + state.placedPieceCount() + " piece(s), seed " + state.seed() +
                        (state.seedLocked() ? " (locked)" : "") +
                        (state.templateFallbackCount() > 0
                                ? ", " + state.templateFallbackCount() + " template fallback(s)"
                                : "")));

        MKButton deleteWorkspace = new MKButton(Component.literal("Delete Workspace"), 180, 20);
        content.addWidget(deleteWorkspace);
        content.addConstraintToWidget(new CenterXConstraint(), deleteWorkspace);
        deleteWorkspace.setPressedCallback((button, mouseButton) -> {
            screen.requestWorkspaceChange(MKWorkspaceChangeRequests.simple(
                    MKWorkspaceSimpleChangeOperation.Kind.DELETE, screen.anchor()));
            return true;
        });

        finishScrollContent(screen, scrollView, content);

        addBackButton(screen, root, "workspace");
        return root;
    }

    private Component sampleSeedText(boolean locked) {
        return Component.literal("Lock Seed: " + (locked ? "On" : "Off"));
    }

    private void stageVariantAdditions(MKWorkspaceScreen screen, List<String> baseNames) {
        for (String baseName : baseNames) {
            screen.draftSession().stageVariantAddition(baseName);
        }
        screen.flagNeedSetup();
    }

    private List<String> physicalTemplateBasePieceNames(MKWorkspaceScreen screen) {
        return screen.workspace().pieces().stream()
                .filter(piece -> piece.variantIndex() == 0)
                .filter(WorkspacePieceDisplay::isAuthoredTemplatePiece)
                .map(WorkspacePieceDisplay::getBaseName)
                .distinct()
                .toList();
    }

    private List<String> basePieceNamesWithoutPhysicalVariants(MKWorkspaceScreen screen) {
        LinkedHashSet<String> basesWithVariants = screen.workspace().pieces().stream()
                .filter(piece -> piece.variantIndex() > 0)
                .filter(WorkspacePieceDisplay::isAuthoredTemplatePiece)
                .map(WorkspacePieceDisplay::getBaseName)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        return screen.workspace().pieces().stream()
                .filter(piece -> piece.variantIndex() == 0)
                .filter(WorkspacePieceDisplay::isAuthoredTemplatePiece)
                .map(WorkspacePieceDisplay::getBaseName)
                .distinct()
                .filter(baseName -> !basesWithVariants.contains(baseName))
                .toList();
    }
}
