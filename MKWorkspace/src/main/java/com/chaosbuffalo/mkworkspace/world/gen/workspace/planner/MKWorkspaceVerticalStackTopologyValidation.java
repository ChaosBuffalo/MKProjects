package com.chaosbuffalo.mkworkspace.world.gen.workspace.planner;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceVerticalStackFloorCounts;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;

import java.util.ArrayList;
import java.util.List;

final class MKWorkspaceVerticalStackTopologyValidation {
    private MKWorkspaceVerticalStackTopologyValidation() {
    }

    static List<String> validate(MKStructureWorkspace workspace) {
        ArrayList<String> errors = new ArrayList<>();
        for (MKWorkspaceVerticalStackSettings settings : workspace.topologyProfile().verticalStackSettings()) {
            errors.addAll(MKWorkspaceVerticalStackFloorCounts.validate(settings).stream()
                    .map(error -> "vertical stack " + settings.stackId() + " " + error)
                    .toList());
        }
        for (MKWorkspaceVerticalStackSettings settings : workspace.topologyProfile().verticalStackSettings()) {
            MKWorkspaceVerticalAccessSpec stackSpec = new MKWorkspaceVerticalAccessSpec(
                    settings.shaftSize(), settings.verticalAccessPlacement(), settings.stairConfig());
            for (String error : stackSpec.validate()) {
                errors.add("vertical stack " + settings.stackId() + " " + error);
            }
            for (String error : settings.foundationPolicy().validate("vertical stack " + settings.stackId())) {
                errors.add(error);
            }
        }
        return List.copyOf(errors);
    }
}
