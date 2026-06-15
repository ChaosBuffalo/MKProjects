package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunPieceShape;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunProjection;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteOverride;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKTextFieldWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class WorkspaceFormLinearRunDetailPage extends WorkspacePageBase {
    public static final String ID = "form_linear_run_detail";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        WorkspaceDraftSession editor = screen.draftSession();
        List<MKWorkspaceLinearRunFamilyDefinition> linearRuns = editor.linearRunFamilies();
        int index = editor.selectedLinearRunIndex();
        if (index < 0 || index >= linearRuns.size()) {
            screen.switchToExistingState(WorkspaceFormLinearRunsPage.ID);
            return new WorkspaceFormLinearRunsPage().build(screen);
        }

        MKWorkspaceLinearRunFamilyDefinition linearRun = linearRuns.get(index);
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Linear Run: " + linearRun.linearRunId()));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Edit one linear run family at a time. Runs bind to topology slots and opening profiles and can be allowed on the main path, branch path, or both."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, helpText);
        MKStackLayoutVertical content = createContentStack(screen);

        addLinearRunFieldRow(screen, content, "Run Id", linearRun.linearRunId(),
                text -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(linearRun,
                        text.trim().isBlank() ? linearRun.linearRunId() : text.trim(), linearRun.topologySlotId(),
                        linearRun.kind(), linearRun.openingProfileId(), linearRun.length(), linearRun.interiorWidth(),
                        linearRun.interiorHeight(), linearRun.slopeDelta(), linearRun.allowOnMainPath(),
                        linearRun.allowOnBranchPath(), linearRun.projection(), linearRun.foundationPolicy(),
                        linearRun.paletteOverrideOpt())));
        addLinearRunFieldRow(screen, content, "Topology Slot", linearRun.topologySlotId(),
                text -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(linearRun,
                        linearRun.linearRunId(), text.trim().isBlank() ? linearRun.topologySlotId() : text.trim(),
                        linearRun.kind(), linearRun.openingProfileId(), linearRun.length(), linearRun.interiorWidth(),
                        linearRun.interiorHeight(), linearRun.slopeDelta(), linearRun.allowOnMainPath(),
                        linearRun.allowOnBranchPath(), linearRun.projection(), linearRun.foundationPolicy(),
                        linearRun.paletteOverrideOpt())));
        addButtonRow(screen, content, "Kind", formatTopologyLabel(linearRun.kind().getSerializedName()), () -> {
            editor.replaceLinearRunFamily(index, copyLinearRunFamily(linearRun, linearRun.linearRunId(),
                    linearRun.topologySlotId(), cycleValue(List.of(MKWorkspaceLinearRunKind.values()), linearRun.kind()),
                    linearRun.openingProfileId(), linearRun.length(), linearRun.interiorWidth(), linearRun.interiorHeight(),
                    linearRun.slopeDelta(), linearRun.allowOnMainPath(), linearRun.allowOnBranchPath(), linearRun.projection(),
                    linearRun.foundationPolicy(), linearRun.paletteOverrideOpt()));
            screen.flagNeedSetup();
        });
        addButtonRow(screen, content, "Projection", formatTopologyLabel(linearRun.projection().getSerializedName()), () -> {
            editor.replaceLinearRunFamily(index, copyLinearRunFamily(linearRun, linearRun.linearRunId(),
                    linearRun.topologySlotId(), linearRun.kind(), linearRun.openingProfileId(), linearRun.length(),
                    linearRun.interiorWidth(), linearRun.interiorHeight(), linearRun.slopeDelta(),
                    linearRun.allowOnMainPath(), linearRun.allowOnBranchPath(),
                    cycleValue(List.of(MKWorkspaceLinearRunProjection.values()), linearRun.projection()),
                    linearRun.foundationPolicy(), linearRun.paletteOverrideOpt()));
            screen.flagNeedSetup();
        });
        addButtonRow(screen, content, "Foundation Mode",
                formatTopologyLabel(linearRun.foundationPolicy().mode().getSerializedName()), () -> {
                    editor.replaceLinearRunFamily(index, copyLinearRunFamily(linearRun, linearRun.linearRunId(),
                            linearRun.topologySlotId(), linearRun.kind(), linearRun.openingProfileId(), linearRun.length(),
                            linearRun.interiorWidth(), linearRun.interiorHeight(), linearRun.slopeDelta(),
                            linearRun.allowOnMainPath(), linearRun.allowOnBranchPath(), linearRun.projection(),
                            foundationPolicyForMode(cycleValue(List.of(MKWorkspaceFoundationMode.values()),
                                    linearRun.foundationPolicy().mode()), linearRun.foundationPolicy()),
                            linearRun.paletteOverrideOpt()));
                    screen.flagNeedSetup();
                });
        addFoundationBlockPickerRow(screen, content, index, linearRun);
        addFoundationMaskRows(screen, content, index, linearRun);
        addLinearRunFieldRow(screen, content, "Opening Profile Id", linearRun.openingProfileId(),
                text -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(linearRun,
                        linearRun.linearRunId(), linearRun.topologySlotId(), linearRun.kind(),
                        text.trim().isBlank() ? linearRun.openingProfileId() : text.trim(),
                        linearRun.length(), linearRun.interiorWidth(), linearRun.interiorHeight(), linearRun.slopeDelta(),
                        linearRun.allowOnMainPath(), linearRun.allowOnBranchPath(), linearRun.projection(),
                        linearRun.foundationPolicy(), linearRun.paletteOverrideOpt())));
        addLinearRunFieldRow(screen, content, "Length", Integer.toString(linearRun.length()),
                text -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(linearRun,
                        linearRun.linearRunId(), linearRun.topologySlotId(), linearRun.kind(), linearRun.openingProfileId(),
                        parseInt(text, linearRun.length()),
                        linearRun.interiorWidth(), linearRun.interiorHeight(), linearRun.slopeDelta(),
                        linearRun.allowOnMainPath(), linearRun.allowOnBranchPath(), linearRun.projection(),
                        linearRun.foundationPolicy(), linearRun.paletteOverrideOpt())));
        addLinearRunFieldRow(screen, content, "Interior Width", Integer.toString(linearRun.interiorWidth()),
                text -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(linearRun,
                        linearRun.linearRunId(), linearRun.topologySlotId(), linearRun.kind(), linearRun.openingProfileId(), linearRun.length(),
                        parseInt(text, linearRun.interiorWidth()), linearRun.interiorHeight(), linearRun.slopeDelta(),
                        linearRun.allowOnMainPath(), linearRun.allowOnBranchPath(), linearRun.projection(),
                        linearRun.foundationPolicy(), linearRun.paletteOverrideOpt())));
        addLinearRunFieldRow(screen, content, "Interior Height", Integer.toString(linearRun.interiorHeight()),
                text -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(linearRun,
                        linearRun.linearRunId(), linearRun.topologySlotId(), linearRun.kind(), linearRun.openingProfileId(), linearRun.length(),
                        linearRun.interiorWidth(), parseInt(text, linearRun.interiorHeight()), linearRun.slopeDelta(),
                        linearRun.allowOnMainPath(), linearRun.allowOnBranchPath(), linearRun.projection(),
                        linearRun.foundationPolicy(), linearRun.paletteOverrideOpt())));
        addLinearRunFieldRow(screen, content, "Top Void Margin", Integer.toString(linearRun.topVoidMargin()),
                text -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(linearRun,
                        linearRun.linearRunId(), linearRun.topologySlotId(), linearRun.kind(), linearRun.openingProfileId(), linearRun.length(),
                        linearRun.interiorWidth(), linearRun.interiorHeight(), linearRun.slopeDelta(),
                        linearRun.allowOnMainPath(), linearRun.allowOnBranchPath(), linearRun.projection(),
                        linearRun.foundationPolicy(), linearRun.paletteOverrideOpt(),
                        Math.max(0, parseInt(text, linearRun.topVoidMargin())))));
        addLinearRunFieldRow(screen, content, "Slope Delta", Integer.toString(linearRun.slopeDelta()),
                text -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(linearRun,
                        linearRun.linearRunId(), linearRun.topologySlotId(), linearRun.kind(), linearRun.openingProfileId(), linearRun.length(),
                        linearRun.interiorWidth(), linearRun.interiorHeight(), parseInt(text, linearRun.slopeDelta()),
                        linearRun.allowOnMainPath(), linearRun.allowOnBranchPath(), linearRun.projection(),
                        linearRun.foundationPolicy(), linearRun.paletteOverrideOpt())));
        screen.addPaletteOverrideRows(content, "Palette Overrides",
                editor.resolveTopologyGroupPalette(linearRun.topologySlotId()),
                linearRun.paletteOverrideOpt(),
                override -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(linearRun,
                        linearRun.linearRunId(), linearRun.topologySlotId(), linearRun.kind(), linearRun.openingProfileId(),
                        linearRun.length(), linearRun.interiorWidth(), linearRun.interiorHeight(), linearRun.slopeDelta(),
                        linearRun.allowOnMainPath(), linearRun.allowOnBranchPath(), linearRun.projection(),
                        linearRun.foundationPolicy(), override)));

        addToggleRow(screen, content, "Allow On Main Path", linearRun.allowOnMainPath(), () -> {
            editor.replaceLinearRunFamily(index, copyLinearRunFamily(linearRun,
                    linearRun.linearRunId(), linearRun.topologySlotId(), linearRun.kind(), linearRun.openingProfileId(), linearRun.length(),
                    linearRun.interiorWidth(), linearRun.interiorHeight(), linearRun.slopeDelta(),
                    !linearRun.allowOnMainPath(), linearRun.allowOnBranchPath(), linearRun.projection(),
                    linearRun.foundationPolicy(), linearRun.paletteOverrideOpt()));
            screen.flagNeedSetup();
        });
        addToggleRow(screen, content, "Allow On Branch Path", linearRun.allowOnBranchPath(), () -> {
            editor.replaceLinearRunFamily(index, copyLinearRunFamily(linearRun,
                    linearRun.linearRunId(), linearRun.topologySlotId(), linearRun.kind(), linearRun.openingProfileId(), linearRun.length(),
                    linearRun.interiorWidth(), linearRun.interiorHeight(), linearRun.slopeDelta(),
                    linearRun.allowOnMainPath(), !linearRun.allowOnBranchPath(), linearRun.projection(),
                    linearRun.foundationPolicy(), linearRun.paletteOverrideOpt()));
            screen.flagNeedSetup();
        });

        finishScrollContent(screen, scrollView, content);

        MKButton remove = addBottomButton(screen, root, Component.literal("Remove Run"), 180, 1);
        remove.setPressedCallback((button, mouseButton) -> {
            editor.removeLinearRunFamily(index);
            editor.selectedLinearRunIndex(-1);
            screen.switchToExistingState(WorkspaceFormLinearRunsPage.ID);
            return true;
        });

        addBackButton(screen, root, WorkspaceFormLinearRunsPage.ID);
        return root;
    }

    private void addFoundationBlockPickerRow(MKWorkspaceScreen screen, MKStackLayoutVertical content, int linearRunIndex,
                                             MKWorkspaceLinearRunFamilyDefinition linearRun) {
        if (linearRun.foundationPolicy().mode() != MKWorkspaceFoundationMode.UNIFORM_STATE) {
            return;
        }
        ResourceLocation blockId = linearRun.foundationPolicy().foundationBlockOpt()
                .orElse(ResourceLocation.parse("minecraft:stone"));
        MKButton blockButton = new MKButton(screen.blockDisplayName(blockId), 180, 20);
        blockButton.setTooltip(Component.literal(blockId.toString()));
        blockButton.setPressedCallback((button, mouseButton) -> {
            screen.openBlockPicker("Choose Foundation Block", blockId, value -> {
                replaceLinearRunFoundationPolicy(screen, linearRunIndex, linearRun,
                        MKWorkspaceFoundationPolicy.uniformBlock(value));
            }, false);
            return true;
        });
        addRow(screen, content, "Foundation Block", blockButton);
    }

    private void addFoundationMaskRows(MKWorkspaceScreen screen, MKStackLayoutVertical content, int linearRunIndex,
                                       MKWorkspaceLinearRunFamilyDefinition linearRun) {
        if (linearRun.foundationPolicy().mode() != MKWorkspaceFoundationMode.MASKED_EXTEND_BOTTOM_BLOCKS) {
            return;
        }
        List<ResourceLocation> maskBlocks = linearRun.foundationPolicy().maskBlocks();
        for (int maskIndex = 0; maskIndex < maskBlocks.size(); maskIndex++) {
            ResourceLocation blockId = maskBlocks.get(maskIndex);
            int capturedIndex = maskIndex;
            MKButton blockButton = new MKButton(screen.blockDisplayName(blockId), 180, 20);
            blockButton.setTooltip(Component.literal(blockId + "\nLeft-click to choose. Right-click to remove."));
            blockButton.setPressedCallback((button, mouseButton) -> {
                if (mouseButton == 1) {
                    updateFoundationMaskBlock(screen, linearRunIndex, linearRun, capturedIndex, null);
                } else {
                    screen.openBlockPicker("Choose Foundation Mask Block", blockId, value ->
                            updateFoundationMaskBlock(screen, linearRunIndex, linearRun, capturedIndex, value), false);
                }
                return true;
            });
            addRow(screen, content, "Mask Block " + (maskIndex + 1), blockButton);
        }
        MKButton addButton = new MKButton(Component.literal("Add Block"), 180, 20);
        addButton.setPressedCallback((button, mouseButton) -> {
            ResourceLocation defaultBlock = ResourceLocation.parse("minecraft:stone");
            screen.openBlockPicker("Choose Foundation Mask Block", defaultBlock, value -> {
                List<ResourceLocation> updated = new java.util.ArrayList<>(linearRun.foundationPolicy().maskBlocks());
                if (!updated.contains(value)) {
                    updated.add(value);
                }
                replaceLinearRunFoundationPolicy(screen, linearRunIndex, linearRun,
                        MKWorkspaceFoundationPolicy.maskedExtendBottomBlocks(updated));
            }, false);
            return true;
        });
        addRow(screen, content, "Foundation Mask", addButton);
    }

    private void updateFoundationMaskBlock(MKWorkspaceScreen screen, int linearRunIndex,
                                           MKWorkspaceLinearRunFamilyDefinition linearRun, int maskIndex,
                                           @Nullable ResourceLocation blockId) {
        List<ResourceLocation> updated = new java.util.ArrayList<>(linearRun.foundationPolicy().maskBlocks());
        if (maskIndex < 0 || maskIndex >= updated.size()) {
            return;
        }
        if (blockId == null) {
            updated.remove(maskIndex);
        } else {
            updated.set(maskIndex, blockId);
        }
        replaceLinearRunFoundationPolicy(screen, linearRunIndex, linearRun,
                MKWorkspaceFoundationPolicy.maskedExtendBottomBlocks(updated));
    }

    private void replaceLinearRunFoundationPolicy(MKWorkspaceScreen screen, int linearRunIndex,
                                                  MKWorkspaceLinearRunFamilyDefinition linearRun,
                                                  MKWorkspaceFoundationPolicy foundationPolicy) {
        WorkspaceDraftSession editor = screen.draftSession();
        editor.replaceLinearRunFamily(linearRunIndex, copyLinearRunFamily(linearRun, linearRun.linearRunId(),
                linearRun.topologySlotId(), linearRun.kind(), linearRun.openingProfileId(), linearRun.length(),
                linearRun.interiorWidth(), linearRun.interiorHeight(), linearRun.slopeDelta(),
                linearRun.allowOnMainPath(), linearRun.allowOnBranchPath(), linearRun.projection(),
                foundationPolicy, linearRun.paletteOverrideOpt()));
        screen.refreshPreservingActiveScroll();
    }

    private void addToggleRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, String label,
                              boolean enabled, Runnable onToggle) {
        MKButton button = new MKButton(Component.literal(enabled ? "Enabled" : "Disabled"), 180,
                screen.buttonHeight());
        button.setPressedCallback((pressed, mouseButton) -> {
            onToggle.run();
            return true;
        });
        addRow(screen, root, label, button);
    }

    private void addLinearRunFieldRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, String label,
                                    String value, Consumer<String> onChange) {
        MKTextFieldWidget field = makeField(screen, label, value);
        field.setTextChangeCallback((widget, text) -> onChange.accept(text));
        addRow(screen, root, label, field);
    }

    private MKTextFieldWidget makeField(MKWorkspaceScreen screen, String label, String value) {
        MKTextFieldWidget widget = new MKTextFieldWidget(screen.font(), 0, 0, 180, 18,
                Component.literal(label));
        widget.setText(value);
        return widget;
    }

    private void addRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, String label,
                        MKTextFieldWidget field) {
        MKText labelText = screen.makeWhiteText(Component.literal(label));
        labelText.setWidth(screen.contentWidth());
        root.addWidget(labelText);
        root.addConstraintToWidget(MarginConstraint.LEFT, labelText);
        root.addWidget(field);
        root.addConstraintToWidget(new CenterXConstraint(), field);
    }

    private void addRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, String label, MKButton button) {
        MKText labelText = screen.makeWhiteText(Component.literal(label));
        labelText.setWidth(screen.contentWidth());
        root.addWidget(labelText);
        root.addConstraintToWidget(MarginConstraint.LEFT, labelText);
        root.addWidget(button);
        root.addConstraintToWidget(new CenterXConstraint(), button);
    }

    private void addButtonRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, String label,
                              String value, Runnable onClick) {
        MKButton button = new MKButton(Component.literal(value), 180, screen.buttonHeight());
        button.setPressedCallback((pressed, mouseButton) -> {
            onClick.run();
            return true;
        });
        addRow(screen, root, label, button);
    }

    private MKWorkspaceLinearRunFamilyDefinition copyLinearRunFamily(
            MKWorkspaceLinearRunFamilyDefinition linearRun,
            String linearRunId,
            String topologySlotId,
            MKWorkspaceLinearRunKind kind,
            String openingProfileId,
            int length,
            int interiorWidth,
            int interiorHeight,
            int slopeDelta,
            boolean allowOnMainPath,
            boolean allowOnBranchPath,
            MKWorkspaceLinearRunProjection projection,
            MKWorkspaceFoundationPolicy foundationPolicy,
            Optional<MKWorkspacePaletteOverride> paletteOverride) {
        return copyLinearRunFamily(linearRun, linearRunId, topologySlotId, kind, openingProfileId, length,
                interiorWidth, interiorHeight, slopeDelta, allowOnMainPath, allowOnBranchPath, projection,
                foundationPolicy, paletteOverride, linearRun.topVoidMargin());
    }

    private MKWorkspaceLinearRunFamilyDefinition copyLinearRunFamily(
            MKWorkspaceLinearRunFamilyDefinition linearRun,
            String linearRunId,
            String topologySlotId,
            MKWorkspaceLinearRunKind kind,
            String openingProfileId,
            int length,
            int interiorWidth,
            int interiorHeight,
            int slopeDelta,
            boolean allowOnMainPath,
            boolean allowOnBranchPath,
            MKWorkspaceLinearRunProjection projection,
            MKWorkspaceFoundationPolicy foundationPolicy,
            Optional<MKWorkspacePaletteOverride> paletteOverride,
            int topVoidMargin) {
        return new MKWorkspaceLinearRunFamilyDefinition(
                linearRunId,
                topologySlotId,
                kind,
                openingProfileId,
                length,
                interiorWidth,
                interiorHeight,
                slopeDelta,
                allowOnMainPath,
                allowOnBranchPath,
                projection,
                linearRun.supportedShapes().isEmpty() ? List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT) : linearRun.supportedShapes(),
                Math.min(Math.max(0, topVoidMargin), Math.max(0, interiorHeight - 1)),
                foundationPolicy,
                paletteOverride.orElse(null)
        );
    }

    private <T> T cycleValue(List<T> values, T current) {
        int index = values.indexOf(current);
        if (index < 0) {
            return values.getFirst();
        }
        return values.get((index + 1) % values.size());
    }

    private MKWorkspaceFoundationPolicy foundationPolicyForMode(MKWorkspaceFoundationMode mode,
                                                                MKWorkspaceFoundationPolicy current) {
        return switch (mode) {
            case NONE -> MKWorkspaceFoundationPolicy.none();
            case UNIFORM_STATE -> current.foundationBlockOpt()
                    .map(MKWorkspaceFoundationPolicy::uniformBlock)
                    .orElse(MKWorkspaceFoundationPolicy.uniformBlock(ResourceLocation.parse("minecraft:stone")));
            case EXTEND_BOTTOM_BLOCKS -> MKWorkspaceFoundationPolicy.extendBottomBlocks();
            case MASKED_EXTEND_BOTTOM_BLOCKS -> current.maskBlocks().isEmpty() ?
                    MKWorkspaceFoundationPolicy.maskedExtendBottomBlocks(List.of(ResourceLocation.parse("minecraft:stone"))) :
                    MKWorkspaceFoundationPolicy.maskedExtendBottomBlocks(current.maskBlocks());
        };
    }

    private String formatTopologyLabel(String key) {
        return WorkspacePieceDisplay.formatTopologyLabel(key);
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}


