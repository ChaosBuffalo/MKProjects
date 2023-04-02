package com.chaosbuffalo.mkweapons.data;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.types.IMeleeWeaponType;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.DataProvider;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;

public class MKWeaponTypesProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final DataGenerator generator;

    public MKWeaponTypesProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void run(@Nonnull HashCache cache) {
        Path outputFolder = this.generator.getOutputFolder();
        MeleeWeaponTypes.WEAPON_TYPES.keySet().forEach(key -> {
            IMeleeWeaponType type = MeleeWeaponTypes.getWeaponType(key);
            MKWeapons.LOGGER.info("Dumping weapon {}", key);
            Path path = outputFolder.resolve("data/" + key.getNamespace() + "/melee_weapon_types/" + key.getPath() + ".json");
            try {
                JsonElement element = type.serialize(JsonOps.INSTANCE);
                DataProvider.save(GSON, cache, element, path);
            } catch (IOException e){
                MKWeapons.LOGGER.error("Couldn't write weapon type {}", path, e);
            }
        });
    }

    @Nonnull
    @Override
    public String getName() {
        return "MKWeapons Weapon Types";
    }
}
