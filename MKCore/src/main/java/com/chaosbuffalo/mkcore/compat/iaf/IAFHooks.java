package com.chaosbuffalo.mkcore.compat.iaf;

import com.chaosbuffalo.mkcore.utils.trace.TraceManager;

public class IAFHooks {

    public static void add() {
        TraceManager.registerExtension(new IAFTraceHandler());
    }
}
