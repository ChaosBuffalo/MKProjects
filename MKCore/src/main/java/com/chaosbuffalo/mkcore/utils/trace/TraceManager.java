package com.chaosbuffalo.mkcore.utils.trace;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TraceManager {

    private static final List<ITraceExtensionProvider> extensionProviders = new ArrayList<>();

    public static void registerExtension(ITraceExtensionProvider provider) {
        Objects.requireNonNull(provider);
        extensionProviders.add(provider);
    }

    public static List<ITraceExtensionProvider> getExtensionProviders() {
        return extensionProviders;
    }
}
