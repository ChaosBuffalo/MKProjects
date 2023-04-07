package com.chaosbuffalo.mknpc.data;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawStructure;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.Optional;

public class MKJigsawBuilder {

    private final ResourceLocation name;
    private final Structure.StructureSettings settings;
    private final Holder<StructureTemplatePool> templatePool;
    private Optional<ResourceLocation> startJigsawName;
    private int maxDepth;
    private HeightProvider heightProvider;
    private boolean useExpansionHack;
    private Optional<Heightmap.Types> heightmapTypes;
    private int maxDistFromCenter;

    public MKJigsawBuilder(ResourceLocation name, Structure.StructureSettings settings,
                           Holder<StructureTemplatePool> templatePool) {
        this.name = name;
        this.settings = settings;
        this.templatePool = templatePool;
        startJigsawName = Optional.empty();
        maxDepth = 7;
        heightProvider = ConstantHeight.of(VerticalAnchor.absolute(0));
        useExpansionHack = true;
        heightmapTypes = Optional.of(Heightmap.Types.WORLD_SURFACE_WG);
        maxDistFromCenter = 80;
    }

    public MKJigsawBuilder setStartJigsawName(Optional<ResourceLocation> startJigsawName) {
        this.startJigsawName = startJigsawName;
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

    public MKJigsawBuilder setAllowSpawns(boolean allowSpawns) {
        return this;
    }

    public MKJigsawBuilder setHeightmapTypes(Optional<Heightmap.Types> heightmapTypes) {
        this.heightmapTypes = heightmapTypes;
        return this;
    }

    public MKJigsawBuilder setMaxDistFromCenter(int maxDistFromCenter) {
        this.maxDistFromCenter = maxDistFromCenter;
        return this;
    }

    public MKJigsawStructure build() {
        return new MKJigsawStructure(settings, templatePool, startJigsawName, maxDepth, heightProvider,
                useExpansionHack, heightmapTypes, maxDistFromCenter, name);
    }
}
