package com.chaosbuffalo.mknpc.data;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawStructure;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKDungeonLayoutSettings;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.StructureEvent;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MKJigsawBuilder {

    private final Structure.StructureSettings settings;
    private final Holder<StructureTemplatePool> templatePool;
    @Nullable
    private ResourceLocation startJigsawName;
    private int maxDepth;
    private HeightProvider heightProvider;
    private boolean useExpansionHack;
    @Nullable
    private Heightmap.Types heightmapTypes;
    private int maxDistFromCenter;
    private boolean fillFloor;
    @Nullable
    private BlockState fillState;
    @Nullable
    private MKDungeonLayoutSettings dungeonLayout;

    private final HashMap<String, StructureEvent> events = new HashMap<>();

    public MKJigsawBuilder(Structure.StructureSettings settings,
                           Holder<StructureTemplatePool> templatePool) {
        this.settings = settings;
        this.templatePool = templatePool;
        startJigsawName = null;
        maxDepth = 7;
        heightProvider = ConstantHeight.of(VerticalAnchor.absolute(0));
        useExpansionHack = true;
        heightmapTypes = Heightmap.Types.WORLD_SURFACE_WG;
        maxDistFromCenter = 80;
        fillState = null;
        fillFloor = false;
        dungeonLayout = null;

    }

    public MKJigsawBuilder setStartJigsawName(Optional<ResourceLocation> startJigsawName) {
        this.startJigsawName = startJigsawName.orElse(null);
        return this;
    }

    public MKJigsawBuilder setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public MKJigsawBuilder setHeightProvider(HeightProvider heightProvider) {
        this.heightProvider = heightProvider;
        return this;
    }

    public MKJigsawBuilder setUseExpansionHack(boolean useExpansionHack) {
        this.useExpansionHack = useExpansionHack;
        return this;
    }

    public MKJigsawBuilder addEvent(StructureEvent event) {
        events.put(event.getEventName(), event);
        return this;
    }

    public MKJigsawBuilder setAllowSpawns(boolean allowSpawns) {
        return this;
    }

    public MKJigsawBuilder setHeightmapTypes(Optional<Heightmap.Types> heightmapTypes) {
        this.heightmapTypes = heightmapTypes.orElse(null);
        return this;
    }

    public MKJigsawBuilder setMaxDistFromCenter(int maxDistFromCenter) {
        this.maxDistFromCenter = maxDistFromCenter;
        return this;
    }

    public MKJigsawBuilder setFillFloor(boolean fillFloor) {
        this.fillFloor = fillFloor;
        return this;
    }

    public MKJigsawBuilder setFillState(Optional<BlockState> fillState) {
        this.fillState = fillState.orElse(null);
        return this;
    }

    public MKJigsawBuilder setDungeonLayout(MKDungeonLayoutSettings dungeonLayout) {
        this.dungeonLayout = dungeonLayout;
        return this;
    }

    public MKJigsawStructure build() {
        var struct = new MKJigsawStructure(settings, templatePool, startJigsawName, maxDepth, heightProvider,
                useExpansionHack, heightmapTypes, maxDistFromCenter, List.of(),
                JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS, dungeonLayout, new CompoundTag(),
                fillFloor, Optional.ofNullable(fillState));
        for (var entry : events.entrySet()) {
            struct.addEvent(entry.getValue());
        }
        return struct;
    }
}
