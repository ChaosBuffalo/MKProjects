package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record MKDungeonConnectorSettings(
        ResourceLocation mainForward,
        ResourceLocation mainBack,
        ResourceLocation branch,
        ResourceLocation connectDown,
        ResourceLocation connectUp,
        ResourceLocation bossForward,
        ResourceLocation bossBack
) {
    public static final Codec<MKDungeonConnectorSettings> CODEC = RecordCodecBuilder.<MKDungeonConnectorSettings>create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("main_forward").forGetter(MKDungeonConnectorSettings::mainForward),
            ResourceLocation.CODEC.fieldOf("main_back").forGetter(MKDungeonConnectorSettings::mainBack),
            ResourceLocation.CODEC.fieldOf("branch").forGetter(MKDungeonConnectorSettings::branch),
            ResourceLocation.CODEC.fieldOf("connect_down").forGetter(MKDungeonConnectorSettings::connectDown),
            ResourceLocation.CODEC.fieldOf("connect_up").forGetter(MKDungeonConnectorSettings::connectUp),
            ResourceLocation.CODEC.fieldOf("boss_forward").forGetter(MKDungeonConnectorSettings::bossForward),
            ResourceLocation.CODEC.fieldOf("boss_back").forGetter(MKDungeonConnectorSettings::bossBack)
    ).apply(instance, (mainForward, mainBack, branch, connectDown, connectUp, bossForward, bossBack) ->
            new MKDungeonConnectorSettings(
                    mainForward,
                    mainBack,
                    branch,
                    connectDown,
                    connectUp,
                    bossForward,
                    bossBack
            )));
}
