package com.chaosbuffalo.mkcore.fx.particles;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleAnimationTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleRenderScaleAnimationTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.colors.ParticleLerpColorAnimationTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.colors.ParticleStaticColorAnimationTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.*;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.BoneEffectInstance;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.ParticleEffectInstance;
import com.chaosbuffalo.mkcore.fx.particles.spawn_patterns.*;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleAnimationsSyncPacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ParticleAnimationManager extends SimpleJsonResourceReloadListener {
    public static final String DEFINITION_FOLDER = "particle_animations";

    public static final ResourceLocation RAW_EFFECT = new ResourceLocation(MKCore.MOD_ID, "particle_anim.raw_effect");
    public static final ResourceLocation INVALID_EFFECT = new ResourceLocation(MKCore.MOD_ID, "particle_anim.invalid");
    private MinecraftServer server;
    private boolean serverStarted = false;

    public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static final Map<ResourceLocation, ParticleType<MKParticleData>> PARTICLE_TYPES_FOR_EDITOR = new HashMap<>();

    public static class TrackDeserializerEntry {
        private final Supplier<ParticleAnimationTrack> trackSupplier;
        private final ParticleAnimationTrack.AnimationTrackType trackType;

        public TrackDeserializerEntry(Supplier<ParticleAnimationTrack> trackSupplier,
                                      ParticleAnimationTrack.AnimationTrackType trackType) {
            this.trackSupplier = trackSupplier;
            this.trackType = trackType;
        }

        public ParticleAnimationTrack.AnimationTrackType getTrackType() {
            return trackType;
        }

        public Supplier<ParticleAnimationTrack> getTrackSupplier() {
            return trackSupplier;
        }
    }

    public static final Map<ResourceLocation, TrackDeserializerEntry> TRACK_DESERIALIZERS = new HashMap<>();
    public static final Map<ResourceLocation, ParticleAnimation> ANIMATIONS = new HashMap<>();
    public static final Map<ResourceLocation, Supplier<ParticleSpawnPattern>> SPAWN_PATTERN_DESERIALIZERS = new HashMap<>();
    public static final Map<ResourceLocation, Supplier<ParticleEffectInstance>> EFFECT_INSTANCE_DESERIALIZERS = new HashMap<>();

    public ParticleAnimationManager() {
        super(GSON, DEFINITION_FOLDER);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void putParticleTypeForEditor(ResourceLocation name,
                                                RegistryObject<ParticleType<MKParticleData>> particleType) {
        if (particleType.isPresent()) {
            PARTICLE_TYPES_FOR_EDITOR.put(name, particleType.get());
        } else {
            MKCore.LOGGER.warn("Provided null particle type for particle editor with name {}", name);
        }
    }

    @Nullable
    public static ParticleAnimation getAnimation(ResourceLocation name) {
        return ANIMATIONS.get(name);
    }

    public static void setupDeserializers() {
        putTrackDeserializer(ParticleRenderScaleAnimationTrack.TYPE_NAME, ParticleRenderScaleAnimationTrack::new,
                ParticleAnimationTrack.AnimationTrackType.SCALE);
        putTrackDeserializer(ParticleLerpColorAnimationTrack.TYPE_NAME, ParticleLerpColorAnimationTrack::new,
                ParticleAnimationTrack.AnimationTrackType.COLOR);
        putTrackDeserializer(BrownianMotionTrack.TYPE_NAME, BrownianMotionTrack::new,
                ParticleAnimationTrack.AnimationTrackType.MOTION);
        putTrackDeserializer(InheritMotionTrack.TYPE_NAME, InheritMotionTrack::new,
                ParticleAnimationTrack.AnimationTrackType.MOTION);
        putTrackDeserializer(LinearMotionTrack.TYPE_NAME, LinearMotionTrack::new,
                ParticleAnimationTrack.AnimationTrackType.MOTION);
        putTrackDeserializer(OrbitingInPlaneMotionTrack.TYPE_NAME, OrbitingInPlaneMotionTrack::new,
                ParticleAnimationTrack.AnimationTrackType.MOTION);
        putTrackDeserializer(ParticleStaticColorAnimationTrack.TYPE_NAME, ParticleStaticColorAnimationTrack::new,
                ParticleAnimationTrack.AnimationTrackType.COLOR);
        putTrackDeserializer(FlipMotionTrack.TYPE_NAME, FlipMotionTrack::new,
                ParticleAnimationTrack.AnimationTrackType.MOTION);

        putSpawnPatternDeserializer(CircleSpawnPattern.TYPE, CircleSpawnPattern::new);
        putSpawnPatternDeserializer(SphereSpawnPattern.TYPE, SphereSpawnPattern::new);
        putSpawnPatternDeserializer(PillarSpawnPattern.TYPE, PillarSpawnPattern::new);
        putSpawnPatternDeserializer(SpiralSpawnPattern.TYPE, SpiralSpawnPattern::new);
        putSpawnPatternDeserializer(LineSpawnPattern.TYPE, LineSpawnPattern::new);
        putSpawnPatternDeserializer(ConeSpawnPattern.TYPE, ConeSpawnPattern::new);
        putSpawnPatternDeserializer(SinglePositionSpawnPattern.TYPE, SinglePositionSpawnPattern::new);
        putSpawnPatternDeserializer(AdvancedLineSpawnPattern.TYPE, AdvancedLineSpawnPattern::new);

        putEffectInstanceDeserializer(BoneEffectInstance.TYPE, BoneEffectInstance::new);
    }

    public static void putEffectInstanceDeserializer(ResourceLocation name, Supplier<ParticleEffectInstance> supplier) {
        EFFECT_INSTANCE_DESERIALIZERS.put(name, supplier);
    }

    public static void putSpawnPatternDeserializer(ResourceLocation name, Supplier<ParticleSpawnPattern> supplier) {
        SPAWN_PATTERN_DESERIALIZERS.put(name, supplier);
    }

    public static void putTrackDeserializer(ResourceLocation name, Supplier<ParticleAnimationTrack> supplier,
                                            ParticleAnimationTrack.AnimationTrackType type) {
        TRACK_DESERIALIZERS.put(name, new TrackDeserializerEntry(supplier, type));
    }

    public static List<ResourceLocation> getTypeNamesForTrackType(ParticleAnimationTrack.AnimationTrackType trackType) {
        return TRACK_DESERIALIZERS.entrySet().stream().filter(x -> x.getValue().trackType == trackType)
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    @Nullable
    public static ParticleEffectInstance getEffectInstance(ResourceLocation name) {
        if (!EFFECT_INSTANCE_DESERIALIZERS.containsKey(name)) {
            MKCore.LOGGER.error("Failed to deserialize effect instance {}", name);
            return null;
        }
        return EFFECT_INSTANCE_DESERIALIZERS.get(name).get();
    }

    @Nullable
    public static ParticleAnimationTrack getAnimationTrack(ResourceLocation name) {

        if (!TRACK_DESERIALIZERS.containsKey(name)) {
            MKCore.LOGGER.error("Failed to deserialize animation track {}", name);
            return null;
        }
        return TRACK_DESERIALIZERS.get(name).getTrackSupplier().get();
    }

    @Nullable
    public static ParticleSpawnPattern getSpawnPattern(ResourceLocation name) {
        if (!SPAWN_PATTERN_DESERIALIZERS.containsKey(name)) {
            MKCore.LOGGER.error("Failed to deserialize spawn pattern {}", name);
            return null;
        }
        return SPAWN_PATTERN_DESERIALIZERS.get(name).get();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        ANIMATIONS.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            MKCore.LOGGER.info("Particle Animation Definition file: {}", resourcelocation);
            if (resourcelocation.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata.
            ParticleAnimation anim = ParticleAnimation.deserializeFromDynamic(entry.getKey(),
                    new Dynamic<>(JsonOps.INSTANCE, entry.getValue()));
            ANIMATIONS.put(entry.getKey(), anim);
        }
        if (serverStarted) {
            handleWorldGenerated();
        }
    }

    @SubscribeEvent
    public void serverStop(ServerStoppingEvent event) {
        serverStarted = false;
        server = null;
    }

    @SubscribeEvent
    public void serverStart(ServerAboutToStartEvent event) {
        server = event.getServer();
        serverStarted = true;
        handleWorldGenerated();
    }

    @SubscribeEvent
    public void onDataPackSync(OnDatapackSyncEvent event) {
        MKCore.LOGGER.debug("ParticleAnimationManager.onDataPackSync");
        ParticleAnimationsSyncPacket updatePacket = new ParticleAnimationsSyncPacket(ANIMATIONS);
        if (event.getPlayer() != null) {
            // sync to single player
            MKCore.LOGGER.info("Sending {} particle animation sync packet", event.getPlayer());
            PacketHandler.sendMessage(updatePacket, event.getPlayer());
        } else {
            // sync to playerlist
            PacketHandler.sendToAll(updatePacket);
        }
    }

    public void syncAnimations(Map<ResourceLocation, ParticleAnimation> anims) {
        if (server != null) {
            ParticleAnimationsSyncPacket updatePacket = new ParticleAnimationsSyncPacket(anims);
            PacketHandler.sendToAll(updatePacket);
        }
    }

    private void handleWorldGenerated() {
        Path dataPath = server.storageSource.getLevelPath(LevelResource.GENERATED_DIR).normalize();
        loadAnimationsFromWorldGenerated(dataPath);
    }

    public void writeAnimationToWorldGenerated(ResourceLocation location, ParticleAnimation animation) {
        ANIMATIONS.put(location, animation);
        Map<ResourceLocation, ParticleAnimation> updateMap = new HashMap<>();
        updateMap.put(location, animation);
        syncAnimations(updateMap);
        Path dataPath = server.storageSource.getLevelPath(LevelResource.GENERATED_DIR).normalize();
        Path loc = Paths.get(dataPath.toString(), location.getNamespace(), "particle_animations", location.getPath() + ".json");
        try {
            JsonElement element = animation.serialize(JsonOps.INSTANCE);
            try {
                Files.createDirectories(loc.getParent());
            } catch (IOException e) {
                MKCore.LOGGER.error("Failed to create directors for animation save {}", e.getMessage());
            }
            String s = GSON.toJson(element);
            MKCore.LOGGER.info("Writing file: {}", loc);
            try (BufferedWriter bufferedwriter = Files.newBufferedWriter(loc)) {
                bufferedwriter.write(s);
            }
        } catch (Exception e) {
            MKCore.LOGGER.error("Failed to serialize ParticleAnimation {}", e.getMessage());
        }
    }

    private void loadAnimationsFromWorldGenerated(Path path) {
        File dir = path.toFile();
        String[] rawDirectories = dir.list((dir1, name) -> new File(dir1, name).isDirectory());
        Map<ResourceLocation, JsonElement> serverOverrides = new HashMap<>();
        if (rawDirectories != null) {
            Collection<String> directories = Arrays.stream(rawDirectories).collect(Collectors.toList());
            for (String modid : directories) {
                MKCore.LOGGER.info("Found particle anim generated : {}", modid);
                Path modPath = Paths.get(path.toString(), modid, "particle_animations");
                MKCore.LOGGER.info("Looking for data in: {}", modPath);
                File modFile = modPath.toFile();
                if (modFile.exists() && modFile.isDirectory()) {
                    String[] sources = modFile.list((modDir, name) -> name.endsWith(".json") && !name.startsWith("_"));
                    if (sources != null) {
                        Collection<String> files = Arrays.stream(sources).collect(Collectors.toList());
                        for (String file : files) {
                            Path filePath = Paths.get(modPath.toString(), file);
                            ResourceLocation overrideName = new ResourceLocation(modid, file.substring(0, file.length() - ".json".length()));
                            try {
                                InputStream fileStream = new FileInputStream(filePath.toFile());
                                Reader reader = new BufferedReader(new InputStreamReader(fileStream, StandardCharsets.UTF_8));
                                JsonElement jsonelement = GsonHelper.fromJson(GSON, reader, JsonElement.class);
                                serverOverrides.put(overrideName, jsonelement);
                            } catch (FileNotFoundException e) {
                                MKCore.LOGGER.error("Failed to decode {}: {}", filePath, e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        for (Map.Entry<ResourceLocation, JsonElement> entry : serverOverrides.entrySet()) {
            ParticleAnimation anim = ParticleAnimation.deserializeFromDynamic(entry.getKey(),
                    new Dynamic<>(JsonOps.INSTANCE, entry.getValue()));
            ANIMATIONS.put(entry.getKey(), anim);
        }
    }
}
