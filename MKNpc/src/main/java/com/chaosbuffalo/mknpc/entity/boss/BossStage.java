package com.chaosbuffalo.mknpc.entity.boss;

import com.chaosbuffalo.mkcore.network.MKParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import com.chaosbuffalo.mknpc.npc.options.NpcDefinitionOption;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class BossStage {

    private final List<NpcDefinitionOption> options = new ArrayList<>();

    public enum ParticleMode {
        MIDDLE,
        LINE_HEIGHT
    }

    private ParticleMode particleMode;

    @Nullable
    private NpcDefinition def;
    @Nullable
    private ResourceLocation transitionParticles;
    @Nullable
    private ResourceLocation transitionSound;

    public BossStage(){
        particleMode = ParticleMode.MIDDLE;
    }

    public void setDefinition(NpcDefinition def) {
        this.def = def;
    }

    public void setTransitionParticles(@Nullable ResourceLocation transitionParticles) {
        this.transitionParticles = transitionParticles;
    }

    public BossStage withTransitionParticles(@Nullable ResourceLocation transitionParticles){
        setTransitionParticles(transitionParticles);
        return this;
    }

    public BossStage withOption(NpcDefinitionOption option){
        addOption(option);
        return this;
    }

    public BossStage withTransitionSound(@Nullable ResourceLocation soundLoc){
        transitionSound = soundLoc;
        return this;
    }

    public void addOption(NpcDefinitionOption option){
        if (option.canBeBossStage()){
            options.add(option);
        } else {
            MKNpc.LOGGER.error("Option of type {} can't be part of a boss stage", option.getName());
        }
    }

    public void apply(MKEntity entity){
        for (NpcDefinitionOption option : options){
            option.applyToEntity(def, entity, 0.0f);
        }
    }

    public BossStage copy(){
        BossStage newStage = new BossStage();
        Tag nbt = serialize(NbtOps.INSTANCE);
        newStage.deserialize(new Dynamic<>(NbtOps.INSTANCE, nbt));
        return newStage;
    }

    public void setParticleMode(ParticleMode particleMode) {
        this.particleMode = particleMode;
    }

    public BossStage withParticleMode(ParticleMode particleMode){
        setParticleMode(particleMode);
        return this;
    }

    public void transition(MKEntity entity){
        if (transitionParticles != null){
            MKParticleEffectSpawnPacket spawnPacket;
            switch (particleMode){
                case LINE_HEIGHT:
                    spawnPacket = new MKParticleEffectSpawnPacket(new Vec3(0.0, 0.0, 0.0), transitionParticles, entity.getId());
                    spawnPacket.addLoc(new Vec3(0.0, entity.getBbHeight() * 4.0, 0.0));
                    break;
                case MIDDLE:
                default:
                    spawnPacket = new MKParticleEffectSpawnPacket(new Vec3(0.0, entity.getBbHeight() * 0.5, 0.0),
                            transitionParticles, entity.getId());
                    break;
            }
            PacketHandler.sendToTrackingAndSelf(spawnPacket, entity);
            if (transitionSound != null){
                SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(transitionSound);
                if (event != null){
                    SoundUtils.serverPlaySoundAtEntity(entity, event, entity.getSoundSource());
                }
            }
        }
    }

    public <D> D serialize(DynamicOps<D> ops){
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("options"), ops.createList(options.stream().map(entry -> entry.serialize(ops))));
        if (transitionParticles != null){
            builder.put(ops.createString("transitionParticles"), ops.createString(transitionParticles.toString()));
        }
        builder.put(ops.createString("particleMode"), ops.createInt(particleMode.ordinal()));
        if (transitionSound != null){
            builder.put(ops.createString("transitionSound"), ops.createString(transitionSound.toString()));
        }
        return ops.createMap(builder.build());

    }

    public <D> void deserialize(Dynamic<D> dynamic){
        List<Optional<NpcDefinitionOption>> newOptions = dynamic.get("options").asList(valueD -> {
            ResourceLocation type = NpcDefinitionOption.getType(valueD);
            NpcDefinitionOption opt = NpcDefinitionManager.getNpcOption(type);
            if (opt != null){
                opt.deserialize(valueD);
            }
            return opt != null ? Optional.of(opt) : Optional.empty();
        });
        options.clear();
        for (Optional<NpcDefinitionOption> option : newOptions){
            option.ifPresent(npcDefinitionOption -> options.add(npcDefinitionOption));
        }
        dynamic.get("transitionParticles").asString().result().ifPresent(x -> transitionParticles = new ResourceLocation(x));
        dynamic.get("transitionSound").asString().result().ifPresent(x -> transitionSound = new ResourceLocation(x));
        particleMode = ParticleMode.values()[dynamic.get("particleMode").asInt(0)];
    }

}
