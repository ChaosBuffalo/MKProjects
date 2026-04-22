package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record MKDungeonConnectorSettings(
        ResourceLocation mainForward,
        ResourceLocation mainBack,
        ResourceLocation branch,
        ResourceLocation stairsDown,
        ResourceLocation stairsUp,
        ResourceLocation bossForward,
        ResourceLocation bossBack
) {
    public static final Codec<MKDungeonConnectorSettings> CODEC = RecordCodecBuilder.<MKDungeonConnectorSettings>create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("main_forward").forGetter(MKDungeonConnectorSettings::mainForward),
            ResourceLocation.CODEC.fieldOf("main_back").forGetter(MKDungeonConnectorSettings::mainBack),
            ResourceLocation.CODEC.fieldOf("branch").forGetter(MKDungeonConnectorSettings::branch),
            ResourceLocation.CODEC.fieldOf("stairs_down").forGetter(MKDungeonConnectorSettings::stairsDown),
            ResourceLocation.CODEC.fieldOf("stairs_up").forGetter(MKDungeonConnectorSettings::stairsUp),
            ResourceLocation.CODEC.fieldOf("boss_forward").forGetter(MKDungeonConnectorSettings::bossForward),
            ResourceLocation.CODEC.fieldOf("boss_back").forGetter(MKDungeonConnectorSettings::bossBack)
    ).apply(instance, MKDungeonConnectorSettings::new));
}
