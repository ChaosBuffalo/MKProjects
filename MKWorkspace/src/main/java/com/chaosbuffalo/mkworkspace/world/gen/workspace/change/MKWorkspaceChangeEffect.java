package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import java.util.Objects;
import java.util.UUID;

/** A typed, user-visible effect produced while preparing a workspace change. */
public record MKWorkspaceChangeEffect(
        Action action,
        Subject subject,
        String subjectId,
        String displayName,
        String baseName,
        int variantIndex,
        boolean physical,
        boolean derived,
        String detail
) {
    public enum Action {
        CREATE, UPDATE, MOVE, EXPAND, REBUILD, REMOVE, PATCH, RESTORE, PRESERVE
    }

    public enum Subject {
        WORKSPACE, SETTING, SLOT, FAMILY, PIECE, TEMPLATE, VARIANT, STAIR, INSERT, SOCKET, AREA, BACKUP
    }

    public MKWorkspaceChangeEffect {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(subject, "subject");
        subjectId = Objects.requireNonNullElse(subjectId, "");
        displayName = Objects.requireNonNullElse(displayName, subjectId);
        baseName = Objects.requireNonNullElse(baseName, "");
        detail = Objects.requireNonNullElse(detail, "");
    }

    public static MKWorkspaceChangeEffect workspace(Action action, UUID workspaceId, String displayName,
                                                     String detail) {
        return new MKWorkspaceChangeEffect(action, Subject.WORKSPACE,
                workspaceId == null ? "" : workspaceId.toString(), displayName, "", 0,
                true, false, detail);
    }

    public static MKWorkspaceChangeEffect setting(String field, String detail) {
        return new MKWorkspaceChangeEffect(Action.UPDATE, Subject.SETTING, field, field, "", 0,
                false, false, detail);
    }
}
