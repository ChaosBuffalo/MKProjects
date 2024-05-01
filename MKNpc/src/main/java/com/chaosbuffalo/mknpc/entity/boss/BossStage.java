package com.chaosbuffalo.mknpc.entity.boss;

import com.chaosbuffalo.mkcore.fx.MKParticles;
import com.chaosbuffalo.mkcore.utils.SoundUtils;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.options.NpcDefinitionOption;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class BossStage {
    public static final Codec<BossStage> CODEC = RecordCodecBuilder.<BossStage>mapCodec(builder -> {
        return builder.group(
                NpcDefinitionOption.CODEC.listOf().fieldOf("options").forGetter(i -> i.options),
                ResourceLocation.CODEC.optionalFieldOf("transitionParticles").forGetter(i -> Optional.ofNullable(i.transitionParticles)),
                ResourceLocation.CODEC.optionalFieldOf("transitionSound").forGetter(i -> Optional.ofNullable(i.transitionSound)),
                ParticleMode.CODEC.optionalFieldOf("particleMode", ParticleMode.MIDDLE).forGetter(i -> i.particleMode)
        ).apply(builder, BossStage::new);
    }).codec();

    public enum ParticleMode implements StringRepresentable {
        MIDDLE("middle"),
        LINE_HEIGHT("line_height");

        public static final Codec<ParticleMode> CODEC = StringRepresentable.fromEnum(ParticleMode::values);

        private final String name;

        ParticleMode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    private final List<NpcDefinitionOption> options = new ArrayList<>();
    private ParticleMode particleMode;

    @Nullable
    private NpcDefinition def;
    @Nullable
    private ResourceLocation transitionParticles;
    @Nullable
    private ResourceLocation transitionSound;

    private BossStage(List<NpcDefinitionOption> options, Optional<ResourceLocation> transitionParticles, Optional<ResourceLocation> transitionSound, ParticleMode particleMode) {
        this.options.addAll(options);
        this.transitionParticles = transitionParticles.orElse(null);
        this.transitionSound = transitionSound.orElse(null);
        this.particleMode = particleMode;
    }

    public BossStage() {
        particleMode = ParticleMode.MIDDLE;
    }

    public void setDefinition(NpcDefinition def) {
        this.def = def;
    }

    public void setTransitionParticles(@Nullable ResourceLocation transitionParticles) {
        this.transitionParticles = transitionParticles;
    }

    public BossStage withTransitionParticles(@Nullable ResourceLocation transitionParticles) {
        setTransitionParticles(transitionParticles);
        return this;
    }

    public BossStage withOption(NpcDefinitionOption option) {
        addOption(option);
        return this;
    }

    public BossStage withTransitionSound(@Nullable ResourceLocation soundLoc) {
        transitionSound = soundLoc;
        return this;
    }

    public void addOption(NpcDefinitionOption option) {
        if (option.canBeBossStage()) {
            options.add(option);
        } else {
            MKNpc.LOGGER.error("Option of type {} can't be part of a boss stage", option.getName());
        }
    }

    public void apply(MKEntity entity) {
        for (NpcDefinitionOption option : options) {
            option.applyToEntity(def, entity, 0.0f);
        }
    }

    public BossStage copy() {
        Tag tag = CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow(false, MKNpc.LOGGER::error);
        return CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow(false, MKNpc.LOGGER::error);
    }

    public void setParticleMode(ParticleMode particleMode) {
        this.particleMode = particleMode;
    }

    public BossStage withParticleMode(ParticleMode particleMode) {
        setParticleMode(particleMode);
        return this;
    }

    public void transition(MKEntity entity) {
        if (transitionParticles != null) {
            switch (particleMode) {
                case LINE_HEIGHT:
                    MKParticles.spawn(entity, new Vec3(0.0, 0.0, 0.0), transitionParticles,
                            p -> p.addLoc(new Vec3(0.0, entity.getBbHeight() * 4.0, 0.0)));
                    break;
                case MIDDLE:
                default:
                    MKParticles.spawn(entity, new Vec3(0.0, entity.getBbHeight() * 0.5, 0.0),
                            transitionParticles);
                    break;
            }
            if (transitionSound != null) {
                SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(transitionSound);
                if (event != null) {
                    SoundUtils.serverPlaySoundAtEntity(entity, event, entity.getSoundSource());
                }
            }
        }
    }

}
