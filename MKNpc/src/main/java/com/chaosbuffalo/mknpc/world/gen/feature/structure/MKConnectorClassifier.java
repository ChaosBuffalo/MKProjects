package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKConnectorRole;

import net.minecraft.data.worldgen.Pools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class MKConnectorClassifier {
    private static final ConcurrentMap<MKDungeonLayoutSettings, ConnectorClassificationRules> RULES_BY_SETTINGS =
            new ConcurrentHashMap<>();

    private MKConnectorClassifier() {
    }

    public static MKConnectorInfo resolve(StructureTemplate.StructureBlockInfo blockInfo, MKDungeonLayoutSettings settings) {
        CompoundTag tag = Objects.requireNonNull(blockInfo.nbt(), () -> blockInfo + " nbt was null");
        ResourceLocation name = ResourceLocation.parse(tag.getString("name"));
        ResourceLocation target = ResourceLocation.parse(tag.getString("target"));
        ResourceKey<StructureTemplatePool> poolKey = Pools.parseKey(tag.getString("pool"));
        ConnectorClassificationRules rules = RULES_BY_SETTINGS.computeIfAbsent(settings, ConnectorClassificationRules::from);
        return new MKConnectorInfo(name, target, poolKey, rules.classify(name, poolKey));
    }

    private record ConnectorClassificationRules(
            Map<ResourceLocation, MKConnectorRole> byName
    ) {
        private static ConnectorClassificationRules from(MKDungeonLayoutSettings settings) {
            LinkedHashMap<ResourceLocation, MKConnectorRole> byName = new LinkedHashMap<>();
            register(byName, settings.mainForward(), MKConnectorRole.MAIN_FORWARD);
            register(byName, settings.mainBack(), MKConnectorRole.MAIN_BACK);
            register(byName, settings.branch(), MKConnectorRole.BRANCH);
            register(byName, settings.connectDown(), MKConnectorRole.CONNECT_DOWN);
            register(byName, settings.connectUp(), MKConnectorRole.CONNECT_UP);
            register(byName, settings.topCapForward(), MKConnectorRole.TOP_CAP_FORWARD);
            register(byName, settings.topCapBack(), MKConnectorRole.TOP_CAP_BACK);
            return new ConnectorClassificationRules(Map.copyOf(byName));
        }

        private MKConnectorRole classify(ResourceLocation name, ResourceKey<StructureTemplatePool> poolKey) {
            MKConnectorRole direct = byName.get(name);
            if (direct != null) {
                return direct;
            }

            if (poolKey.equals(Pools.EMPTY)) {
                return MKConnectorRole.TERMINAL;
            }
            return MKConnectorRole.UNKNOWN;
        }

        private static void register(Map<ResourceLocation, MKConnectorRole> byName, ResourceLocation name, MKConnectorRole role) {
            MKConnectorRole previous = byName.putIfAbsent(name, role);
            if (previous != null && previous != role) {
                throw new IllegalStateException("Duplicate connector name " + name +
                        " configured for both " + previous + " and " + role);
            }
        }
    }
}

