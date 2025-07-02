package com.chaosbuffalo.mkcore.data.providers;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public abstract class MKDataProvider implements DataProvider {

    protected final DataGenerator generator;
    private final String modId;
    private final String providerName;
    protected CompletableFuture<HolderLookup.Provider> registries;

    public MKDataProvider(DataGenerator generator, String modId, String providerName) {
        this.generator = generator;
        this.modId = modId;
        this.providerName = providerName;
    }

    public MKDataProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> registries,  String modId, String providerName) {
        this.generator = generator;
        this.registries = registries;
        this.modId = modId;
        this.providerName = providerName;
    }

    @Nonnull
    @Override
    public String getName() {
        return String.format("%s: %s", providerName, modId);
    }

    public String getModId() {
        return modId;
    }
}
