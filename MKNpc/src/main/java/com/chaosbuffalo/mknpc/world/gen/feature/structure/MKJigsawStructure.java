package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.init.MKNpcWorldGen;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.StructureEvent;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.StructureEventManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class MKJigsawStructure extends Structure {

    public static final Codec<MKJigsawStructure> CODEC = RecordCodecBuilder.<MKJigsawStructure>mapCodec(builder ->
            builder.group(settingsCodec(builder),
                    StructureTemplatePool.CODEC.fieldOf("start_pool")
                            .forGetter(s -> s.startPool),
                    ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name")
                            .forGetter(s -> s.startJigsawName),
                    Codec.intRange(0, 7).fieldOf("size")
                            .forGetter(s -> s.maxDepth),
                    HeightProvider.CODEC.fieldOf("start_height")
                            .forGetter(s -> s.startHeight),
                    Codec.BOOL.fieldOf("use_expansion_hack")
                            .forGetter(s -> s.useExpansionHack),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
                            .forGetter(s -> s.projectStartToHeightmap),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center")
                            .forGetter(s -> s.maxDistanceFromCenter),
                    ResourceLocation.CODEC.fieldOf("structureName")
                            .forGetter(MKJigsawStructure::getStructureName),
                    CompoundTag.CODEC.fieldOf("structure_events")
                            .forGetter(MKJigsawStructure::getNbt)
            ).apply(builder, MKJigsawStructure::new)).flatXmap(verifyRange(), verifyRange()).codec();

    private static Function<MKJigsawStructure, DataResult<MKJigsawStructure>> verifyRange() {
        return structure -> {
            int i = switch (structure.terrainAdaptation()) {
                case NONE -> 0;
                case BURY, BEARD_THIN, BEARD_BOX -> 12;
            };
            return structure.maxDistanceFromCenter + i > 128 ?
                    DataResult.error(() -> "Structure size including terrain adaptation must not exceed 128") :
                    DataResult.success(structure);
        };
    }

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;

    @Nullable
    private Component enterMessage;
    @Nullable
    private Component exitMessage;
    private final Map<String, StructureEvent> events = new HashMap<>();
    private final ResourceLocation structureName;

    public MKJigsawStructure(StructureSettings pSettings, Holder<StructureTemplatePool> templatePool,
                             Optional<ResourceLocation> startJigsawName, int maxDepth, HeightProvider heightProvider,
                             boolean useExpansionHack, Optional<Heightmap.Types> heightmapTypes, int maxDistanceFromCenter,
                             ResourceLocation structureName, CompoundTag structureNbt) {
        super(pSettings);
        this.structureName = structureName;
        this.startPool = templatePool;
        this.startJigsawName = startJigsawName;
        this.maxDepth = maxDepth;
        this.startHeight = heightProvider;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = heightmapTypes;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        loadNbt(structureNbt);
    }

    public CompoundTag getNbt() {
        CompoundTag tag = new CompoundTag();
        if (!events.isEmpty()) {
            ListTag structureEvents = new ListTag();
            for (StructureEvent event : events.values()) {
                structureEvents.add(event.serialize(NbtOps.INSTANCE));
            }
            tag.put("structure_events", structureEvents);
        }
        return tag;
    }

    public void loadNbt(CompoundTag tag) {
        if (tag.contains("structure_events")) {
            ListTag evList = tag.getList("structure_events", Tag.TAG_COMPOUND);
            for (int i = 0; i < evList.size(); i++) {
                CompoundTag evTag = evList.getCompound(i);
                Dynamic<?> dyn = new Dynamic<>(NbtOps.INSTANCE, evTag);
                ResourceLocation type = StructureEvent.getType(dyn);
                Supplier<StructureEvent> supplier = StructureEventManager.getEventDeserializer(type);
                if (supplier != null) {
                    StructureEvent ev = supplier.get();
                    ev.deserialize(dyn);
                    addEvent(ev.getEventName(), ev);
                }
            }
        }
    }



    @Override
    public StructureType<?> type() {
        return MKNpcWorldGen.MK_STRUCTURE_TYPE.get();
    }

    public ResourceLocation getStructureName() {
        return structureName;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext pContext) {
        ChunkPos chunkpos = pContext.chunkPos();
        int startY = this.startHeight.sample(pContext.random(), new WorldGenerationContext(pContext.chunkGenerator(), pContext.heightAccessor()));
        BlockPos startPos = new BlockPos(chunkpos.getMinBlockX(), startY, chunkpos.getMinBlockZ());
        return JigsawPlacement.addPieces(pContext, startPool, startJigsawName, maxDepth, startPos,
                useExpansionHack, projectStartToHeightmap, maxDistanceFromCenter);
    }


    public MKJigsawStructure addEvent(String name, StructureEvent event) {
        event.setEventName(name);
        events.put(name, event);
        return this;
    }

    public MKJigsawStructure setEnterMessage(Component msg) {
        this.enterMessage = msg;
        return this;
    }

    public MKJigsawStructure setExitMessage(Component msg) {
        this.exitMessage = msg;
        return this;
    }


    public void onStructureActivate(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, Level world) {
        MKNpc.LOGGER.debug("Activating structure {} (ID: {})", entry.getStructureName(), entry.getStructureId());
        for (Map.Entry<String, StructureEvent> ev : events.entrySet()) {
            if (ev.getValue().meetsRequirements(entry, activeStructure, world)) {
                entry.addActiveEvent(ev.getKey());
            }
        }
        for (String key : entry.getActiveEvents()) {
            StructureEvent ev = events.get(key);
            if (ev != null && ev.canTrigger(StructureEvent.EventTrigger.ON_ACTIVATE)) {
                checkAndExecuteEvent(ev, entry, activeStructure, world);
            }
        }
    }

    public void onStructureDeactivate(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, Level world) {
        MKNpc.LOGGER.debug("Deactivating structure {} (ID: {})", entry.getStructureName(), entry.getStructureId());
        for (String key : entry.getActiveEvents()) {
            StructureEvent ev = events.get(key);
            if (ev != null && ev.canTrigger(StructureEvent.EventTrigger.ON_DEACTIVATE)) {
                checkAndExecuteEvent(ev, entry, activeStructure, world);
            }
        }
        entry.clearActiveEvents();
    }

    public void onActiveTick(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, Level world) {
        for (String key : entry.getActiveEvents()) {
            StructureEvent ev = events.get(key);
            if (ev != null && ev.canTrigger(StructureEvent.EventTrigger.ON_TICK)) {
                checkAndExecuteEvent(ev, entry, activeStructure, world);
            }
        }
    }

    protected void checkAndExecuteEvent(StructureEvent ev, MKStructureEntry entry,
                                        WorldStructureManager.ActiveStructure activeStructure, Level world) {
        if (!entry.getCooldownTracker().hasTimer(ev.getTimerName()) && ev.meetsConditions(entry, activeStructure, world)) {
            ev.execute(entry, activeStructure, world);
            if (ev.startsCooldownImmediately()) {
                entry.getCooldownTracker().setTimer(ev.getTimerName(), ev.getCooldown());
            }

        }
    }

    public void onTrackedEntityDeath(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, IEntityNpcData npcData,
                                     String eventName) {
        StructureEvent ev = events.get(eventName);
        if (ev != null) {
            ev.onTrackedEntityDeath(entry, activeStructure, npcData);
        }
    }

    public void onNpcDeath(MKStructureEntry entry, WorldStructureManager.ActiveStructure activeStructure, IEntityNpcData npcData) {
        for (String key : entry.getActiveEvents()) {
            StructureEvent ev = events.get(key);
            if (ev != null && ev.canTrigger(StructureEvent.EventTrigger.ON_DEATH)) {
                checkAndExecuteEvent(ev, entry, activeStructure, npcData.getEntity().getCommandSenderWorld());
            }
        }
    }

    public void onPlayerEnter(ServerPlayer player, MKStructureEntry structureEntry,
                              WorldStructureManager.ActiveStructure activeStructure) {
        if (getEnterMessage() != null) {
            player.sendSystemMessage(getEnterMessage());
        }
    }

    public void onPlayerExit(ServerPlayer player, MKStructureEntry structureEntry,
                             WorldStructureManager.ActiveStructure activeStructure) {
        if (getExitMessage() != null) {
            player.sendSystemMessage(getExitMessage());
        }
    }

    @Nullable
    public Component getEnterMessage() {
        return enterMessage;
    }

    @Nullable
    public Component getExitMessage() {
        return exitMessage;
    }
}
