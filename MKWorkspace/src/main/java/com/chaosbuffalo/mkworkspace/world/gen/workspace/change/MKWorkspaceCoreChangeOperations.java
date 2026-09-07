package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations.MKWorkspaceDefinitionChangeOperation;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations.MKWorkspaceSimpleChangeOperation;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations.MKWorkspaceInsertSocketChangeOperation;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations.MKWorkspaceContentSelectionChangeOperation;

public final class MKWorkspaceCoreChangeOperations {
    private static boolean registered;

    private MKWorkspaceCoreChangeOperations() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        MKWorkspaceChangeRegistry.shared().register(new MKWorkspaceDefinitionChangeOperation());
        MKWorkspaceChangeRegistry.shared().register(new MKWorkspaceInsertSocketChangeOperation());
        MKWorkspaceChangeRegistry.shared().register(new MKWorkspaceContentSelectionChangeOperation());
        for (MKWorkspaceSimpleChangeOperation.Kind kind : MKWorkspaceSimpleChangeOperation.Kind.values()) {
            MKWorkspaceChangeRegistry.shared().register(new MKWorkspaceSimpleChangeOperation(kind));
        }
    }
}
