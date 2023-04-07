package com.chaosbuffalo.mkcore.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;

import javax.annotation.Nonnull;

public abstract class MKDataProvider implements DataProvider {

    protected final DataGenerator generator;
    private final String modId;
    private final String providerName;

    public MKDataProvider(DataGenerator generator, String modId, String providerName) {
        this.generator = generator;
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
