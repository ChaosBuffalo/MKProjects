package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.sync.controllers.SyncController;

public interface ISyncControllerProvider {

    SyncController getSyncController();
}
