package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import net.minecraft.data.worldgen.Pools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Objects;

public final class MKConnectorClassifier {
    private MKConnectorClassifier() {
    }

    public static MKConnectorInfo resolve(StructureTemplate.StructureBlockInfo blockInfo, MKDungeonLayoutSettings settings) {
        CompoundTag tag = Objects.requireNonNull(blockInfo.nbt(), () -> blockInfo + " nbt was null");
        ResourceLocation name = ResourceLocation.parse(tag.getString("name"));
        ResourceLocation target = ResourceLocation.parse(tag.getString("target"));
        ResourceKey<StructureTemplatePool> poolKey = Pools.parseKey(tag.getString("pool"));
        return new MKConnectorInfo(name, target, poolKey, classify(name, poolKey, settings));
    }

    private static MKConnectorRole classify(ResourceLocation name, ResourceKey<StructureTemplatePool> poolKey, MKDungeonLayoutSettings settings) {
        if (name.equals(settings.mainForward())) {
            return MKConnectorRole.MAIN_FORWARD;
        }
        if (name.equals(settings.mainBack())) {
            return MKConnectorRole.MAIN_BACK;
        }
        if (name.equals(settings.branch())) {
            return MKConnectorRole.BRANCH;
        }
        if (name.equals(settings.connectDown()) || "stairs_down".equals(name.getPath())) {
            return MKConnectorRole.CONNECT_DOWN;
        }
        if (name.equals(settings.connectUp()) || "stairs_up".equals(name.getPath())) {
            return MKConnectorRole.CONNECT_UP;
        }
        if (name.equals(settings.bossForward())) {
            return MKConnectorRole.BOSS_FORWARD;
        }
        if (name.equals(settings.bossBack())) {
            return MKConnectorRole.BOSS_BACK;
        }
        if (poolKey.equals(Pools.EMPTY)) {
            return MKConnectorRole.TERMINAL;
        }
        return MKConnectorRole.UNKNOWN;
    }
}
