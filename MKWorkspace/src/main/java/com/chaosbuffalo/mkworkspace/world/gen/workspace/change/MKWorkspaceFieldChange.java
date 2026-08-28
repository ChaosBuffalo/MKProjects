package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import java.util.Objects;

public record MKWorkspaceFieldChange(String field, String beforeValue, String afterValue) {
    public MKWorkspaceFieldChange {
        field = Objects.requireNonNullElse(field, "");
        beforeValue = Objects.requireNonNullElse(beforeValue, "");
        afterValue = Objects.requireNonNullElse(afterValue, "");
    }
}
