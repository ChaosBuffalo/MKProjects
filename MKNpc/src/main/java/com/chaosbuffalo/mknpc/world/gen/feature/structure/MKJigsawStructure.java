package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import com.chaosbuffalo.mknpc.capabilities.WorldStructureManager;
import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.events.StructureEvent;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.JigsawFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class MKJigsawStructure extends JigsawFeature implements IControlNaturalSpawns {

    private final boolean allowSpawns;
    @Nullable
    private Component enterMessage;
    @Nullable
    private Component exitMessage;
    private final Map<String, StructureEvent> events = new HashMap<>();
    private final PieceGeneratorSupplier<JigsawConfiguration> pieceGenerator;


    public MKJigsawStructure(Codec<JigsawConfiguration> codec, int groundLevel, boolean offsetVertical,
                             boolean offsetFromWorldSurface,
                             Predicate<PieceGeneratorSupplier.Context<JigsawConfiguration>> pieceSupplier,
                             boolean allowSpawns) {
        super(codec, groundLevel, offsetVertical, offsetFromWorldSurface, pieceSupplier);
        this.allowSpawns = allowSpawns;
        enterMessage = null;
        exitMessage = null;
        pieceGenerator = makePieceGenerator(groundLevel, offsetVertical, offsetFromWorldSurface, pieceSupplier);
    }

    @NotNull
    private PieceGeneratorSupplier<JigsawConfiguration> makePieceGenerator(int groundLevel, boolean offsetVertical, boolean offsetFromWorldSurface, Predicate<PieceGeneratorSupplier.Context<JigsawConfiguration>> pieceSupplier) {
        return (p_197102_) -> {
            if (!pieceSupplier.test(p_197102_)) {
                return Optional.empty();
            } else {
                ResourceLocation featureName = ForgeRegistries.STRUCTURE_FEATURES.getKey(this);
                BlockPos blockpos = new BlockPos(p_197102_.chunkPos().getMinBlockX(), groundLevel, p_197102_.chunkPos().getMinBlockZ());
                return JigsawPlacement.addPieces(p_197102_,
                        (structureManager, poolElement, blockPos, groundLevelData, rot, boundingBox) ->
                                new MKPoolElementPiece(structureManager, poolElement, blockPos, groundLevelData, rot, boundingBox, featureName), blockpos, offsetVertical, offsetFromWorldSurface);
            }
        };
    }

    @Override
    public PieceGeneratorSupplier<JigsawConfiguration> pieceGeneratorSupplier() {
        return pieceGenerator;
    }

    public MKJigsawStructure addEvent(String name, StructureEvent event) {
        event.setEventName(name);
        events.put(name, event);
        return this;
    }

    @Override
    public boolean doesAllowSpawns(){
        return allowSpawns;
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

    @Override
    public boolean canGenerate(RegistryAccess p_197172_, ChunkGenerator p_197173_, BiomeSource p_197174_, StructureManager p_197175_, long p_197176_, ChunkPos p_197177_, JigsawConfiguration p_197178_, LevelHeightAccessor p_197179_, Predicate<Holder<Biome>> p_197180_) {
        return this.pieceGenerator.createGenerator(new PieceGeneratorSupplier.Context<>(p_197173_, p_197174_, p_197176_, p_197177_, p_197178_, p_197179_, p_197180_, p_197175_, p_197172_)).isPresent();
    }

    protected void checkAndExecuteEvent(StructureEvent ev, MKStructureEntry entry,
                                        WorldStructureManager.ActiveStructure activeStructure, Level world) {
        if (!entry.getCooldownTracker().hasTimer(ev.getTimerName()) && ev.meetsConditions(entry, activeStructure, world)) {
            ev.execute(entry, activeStructure, world);
            entry.getCooldownTracker().setTimer(ev.getTimerName(), ev.getCooldown());
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
            player.sendMessage(getEnterMessage(), Util.NIL_UUID);
        }
    }

    public void onPlayerExit(ServerPlayer player, MKStructureEntry structureEntry,
                             WorldStructureManager.ActiveStructure activeStructure) {
        if (getExitMessage() != null) {
            player.sendMessage(getExitMessage(), Util.NIL_UUID);
        }
    }

    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
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
