package com.chaosbuffalo.mkweapons.data;

import com.chaosbuffalo.mkcore.data.providers.MKDataProvider;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MKWeaponTypesProvider extends MKDataProvider {

    public MKWeaponTypesProvider(DataGenerator generator) {
        super(generator, MKWeapons.MODID, "Weapon Types");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        Path outputFolder = this.generator.getPackOutput().getOutputFolder();
        return CompletableFuture.allOf(MeleeWeaponTypes.WEAPON_TYPES.entrySet().stream()
                .map(entry -> {
                    ResourceLocation key = entry.getKey();
                    JsonElement element = entry.getValue().serialize(JsonOps.INSTANCE);
                    Path path = outputFolder.resolve("data/" + key.getNamespace() + "/melee_weapon_types/" + key.getPath() + ".json");
                    return DataProvider.saveStable(pOutput, element, path);
                }).collect(Collectors.toList()).toArray(CompletableFuture[]::new));
    }
}
