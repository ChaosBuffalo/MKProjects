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

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class WorkspaceFormHallwayDetailPage extends WorkspacePageBase {
    public static final String ID = "form_hallway_detail";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        WorkspaceDraftSession editor = screen.draftSession();
        List<MKWorkspaceLinearRunFamilyDefinition> hallways = editor.linearRunFamilies();
        int index = editor.selectedHallwayIndex();
        if (index < 0 || index >= hallways.size()) {
            screen.switchToExistingState(WorkspaceFormHallwaysPage.ID);
            return new WorkspaceFormHallwaysPage().build(screen);
        }

        MKWorkspaceLinearRunFamilyDefinition hallway = hallways.get(index);
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Linear Run: " + hallway.linearRunId()));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Edit one linear run family at a time. Runs bind to topology slots and opening profiles and can be allowed on the main path, branch path, or both."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, helpText);
        MKStackLayoutVertical content = createContentStack(screen);

        addHallwayFieldRow(screen, content, "Run Id", hallway.linearRunId(),
                text -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(hallway,
                        text.trim().isBlank() ? hallway.linearRunId() : text.trim(), hallway.topologySlotId(),
                        hallway.kind(), hallway.openingProfileId(), hallway.length(), hallway.interiorWidth(),
                        hallway.interiorHeight(), hallway.slopeDelta(), hallway.allowOnMainPath(),
                        hallway.allowOnBranchPath(), hallway.projection(), hallway.foundationPolicy(),
                        hallway.paletteOverrideOpt())));
        addHallwayFieldRow(screen, content, "Topology Slot", hallway.topologySlotId(),
                text -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(hallway,
                        hallway.linearRunId(), text.trim().isBlank() ? hallway.topologySlotId() : text.trim(),
                        hallway.kind(), hallway.openingProfileId(), hallway.length(), hallway.interiorWidth(),
                        hallway.interiorHeight(), hallway.slopeDelta(), hallway.allowOnMainPath(),
                        hallway.allowOnBranchPath(), hallway.projection(), hallway.foundationPolicy(),
                        hallway.paletteOverrideOpt())));
        addButtonRow(screen, content, "Kind", formatTopologyLabel(hallway.kind().getSerializedName()), () -> {
            editor.replaceLinearRunFamily(index, copyLinearRunFamily(hallway, hallway.linearRunId(),
                    hallway.topologySlotId(), cycleValue(List.of(MKWorkspaceLinearRunKind.values()), hallway.kind()),
                    hallway.openingProfileId(), hallway.length(), hallway.interiorWidth(), hallway.interiorHeight(),
                    hallway.slopeDelta(), hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.projection(),
                    hallway.foundationPolicy(), hallway.paletteOverrideOpt()));
            screen.flagNeedSetup();
        });
        addButtonRow(screen, content, "Projection", formatTopologyLabel(hallway.projection().getSerializedName()), () -> {
            editor.replaceLinearRunFamily(index, copyLinearRunFamily(hallway, hallway.linearRunId(),
                    hallway.topologySlotId(), hallway.kind(), hallway.openingProfileId(), hallway.length(),
                    hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                    hallway.allowOnMainPath(), hallway.allowOnBranchPath(),
                    cycleValue(List.of(MKWorkspaceLinearRunProjection.values()), hallway.projection()),
                    hallway.foundationPolicy(), hallway.paletteOverrideOpt()));
            screen.flagNeedSetup();
        });
        addButtonRow(screen, content, "Foundation Mode",
                formatTopologyLabel(hallway.foundationPolicy().mode().getSerializedName()), () -> {
                    editor.replaceLinearRunFamily(index, copyLinearRunFamily(hallway, hallway.linearRunId(),
                            hallway.topologySlotId(), hallway.kind(), hallway.openingProfileId(), hallway.length(),
                            hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                            hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.projection(),
                            foundationPolicyForMode(cycleValue(List.of(MKWorkspaceFoundationMode.values()),
                                    hallway.foundationPolicy().mode()), hallway.foundationPolicy()),
                            hallway.paletteOverrideOpt()));
                    screen.flagNeedSetup();
                });
        addHallwayFieldRow(screen, content, "Foundation Block",
                hallway.foundationPolicy().foundationBlockOpt().map(ResourceLocation::toString).orElse(""),
                text -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(hallway, hallway.linearRunId(),
                        hallway.topologySlotId(), hallway.kind(), hallway.openingProfileId(), hallway.length(),
                        hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.projection(),
                        new MKWorkspaceFoundationPolicy(MKWorkspaceFoundationMode.UNIFORM_STATE,
                                parseResourceLocation(text).orElse(null), List.of()),
                        hallway.paletteOverrideOpt())));
        addHallwayFieldRow(screen, content, "Foundation Mask Blocks",
                hallway.foundationPolicy().maskBlocks().stream()
                        .map(ResourceLocation::toString)
                        .collect(java.util.stream.Collectors.joining(",")),
                text -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(hallway, hallway.linearRunId(),
                        hallway.topologySlotId(), hallway.kind(), hallway.openingProfileId(), hallway.length(),
                        hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.projection(),
                        MKWorkspaceFoundationPolicy.maskedExtendBottomBlocks(parseResourceLocationList(text)),
                        hallway.paletteOverrideOpt())));
        addHallwayFieldRow(screen, content, "Opening Profile Id", hallway.openingProfileId(),
                text -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(hallway,
                        hallway.linearRunId(), hallway.topologySlotId(), hallway.kind(),
                        text.trim().isBlank() ? hallway.openingProfileId() : text.trim(),
                        hallway.length(), hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.projection(),
                        hallway.foundationPolicy(), hallway.paletteOverrideOpt())));
        addHallwayFieldRow(screen, content, "Length", Integer.toString(hallway.length()),
                text -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(hallway,
                        hallway.linearRunId(), hallway.topologySlotId(), hallway.kind(), hallway.openingProfileId(),
                        parseInt(text, hallway.length()),
                        hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.projection(),
                        hallway.foundationPolicy(), hallway.paletteOverrideOpt())));
        addHallwayFieldRow(screen, content, "Interior Width", Integer.toString(hallway.interiorWidth()),
                text -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(hallway,
                        hallway.linearRunId(), hallway.topologySlotId(), hallway.kind(), hallway.openingProfileId(), hallway.length(),
                        parseInt(text, hallway.interiorWidth()), hallway.interiorHeight(), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.projection(),
                        hallway.foundationPolicy(), hallway.paletteOverrideOpt())));
        addHallwayFieldRow(screen, content, "Interior Height", Integer.toString(hallway.interiorHeight()),
                text -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(hallway,
                        hallway.linearRunId(), hallway.topologySlotId(), hallway.kind(), hallway.openingProfileId(), hallway.length(),
                        hallway.interiorWidth(), parseInt(text, hallway.interiorHeight()), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.projection(),
                        hallway.foundationPolicy(), hallway.paletteOverrideOpt())));
        addHallwayFieldRow(screen, content, "Top Void Margin", Integer.toString(hallway.topVoidMargin()),
                text -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(hallway,
                        hallway.linearRunId(), hallway.topologySlotId(), hallway.kind(), hallway.openingProfileId(), hallway.length(),
                        hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.projection(),
                        hallway.foundationPolicy(), hallway.paletteOverrideOpt(),
                        Math.max(0, parseInt(text, hallway.topVoidMargin())))));
        addHallwayFieldRow(screen, content, "Slope Delta", Integer.toString(hallway.slopeDelta()),
                text -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(hallway,
                        hallway.linearRunId(), hallway.topologySlotId(), hallway.kind(), hallway.openingProfileId(), hallway.length(),
                        hallway.interiorWidth(), hallway.interiorHeight(), parseInt(text, hallway.slopeDelta()),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.projection(),
                        hallway.foundationPolicy(), hallway.paletteOverrideOpt())));
        screen.addPaletteOverrideRows(content, "Palette Overrides", editor.draftBasePalette(),
                hallway.paletteOverrideOpt(),
                override -> editor.replaceLinearRunFamily(index, copyLinearRunFamily(hallway,
                        hallway.linearRunId(), hallway.topologySlotId(), hallway.kind(), hallway.openingProfileId(),
                        hallway.length(), hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.projection(),
                        hallway.foundationPolicy(), override)));

        addToggleRow(screen, content, "Allow On Main Path", hallway.allowOnMainPath(), () -> {
            editor.replaceLinearRunFamily(index, copyLinearRunFamily(hallway,
                    hallway.linearRunId(), hallway.topologySlotId(), hallway.kind(), hallway.openingProfileId(), hallway.length(),
                    hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                    !hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.projection(),
                    hallway.foundationPolicy(), hallway.paletteOverrideOpt()));
            screen.flagNeedSetup();
        });
        addToggleRow(screen, content, "Allow On Branch Path", hallway.allowOnBranchPath(), () -> {
            editor.replaceLinearRunFamily(index, copyLinearRunFamily(hallway,
                    hallway.linearRunId(), hallway.topologySlotId(), hallway.kind(), hallway.openingProfileId(), hallway.length(),
                    hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                    hallway.allowOnMainPath(), !hallway.allowOnBranchPath(), hallway.projection(),
                    hallway.foundationPolicy(), hallway.paletteOverrideOpt()));
            screen.flagNeedSetup();
        });

        finishScrollContent(screen, scrollView, content);

        MKButton remove = addBottomButton(screen, root, Component.literal("Remove Hallway"), 180, 1);
        remove.setPressedCallback((button, mouseButton) -> {
            editor.removeLinearRunFamily(index);
            editor.selectedHallwayIndex(-1);
            screen.switchToExistingState(WorkspaceFormHallwaysPage.ID);
            return true;
        });

        addBackButton(screen, root, WorkspaceFormHallwaysPage.ID);
        return root;
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

    private void addHallwayFieldRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, String label,
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

    private Optional<ResourceLocation> parseResourceLocation(String value) {
        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ResourceLocation.tryParse(trimmed));
    }

    private List<ResourceLocation> parseResourceLocationList(String value) {
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .map(ResourceLocation::tryParse)
                .filter(java.util.Objects::nonNull)
                .toList();
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


