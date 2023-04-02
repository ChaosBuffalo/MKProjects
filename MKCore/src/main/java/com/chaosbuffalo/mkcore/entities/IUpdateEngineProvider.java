package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.sync.UpdateEngine;

public interface IUpdateEngineProvider {

    UpdateEngine getUpdateEngine();
}
