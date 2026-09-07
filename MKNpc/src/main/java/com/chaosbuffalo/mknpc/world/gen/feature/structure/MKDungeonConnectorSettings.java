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
        ResourceLocation topCapForward,
        ResourceLocation topCapBack
) {
    public static final Codec<MKDungeonConnectorSettings> CODEC = RecordCodecBuilder.<MKDungeonConnectorSettings>create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("main_forward").forGetter(MKDungeonConnectorSettings::mainForward),
            ResourceLocation.CODEC.fieldOf("main_back").forGetter(MKDungeonConnectorSettings::mainBack),
            ResourceLocation.CODEC.fieldOf("branch").forGetter(MKDungeonConnectorSettings::branch),
            ResourceLocation.CODEC.fieldOf("connect_down").forGetter(MKDungeonConnectorSettings::connectDown),
            ResourceLocation.CODEC.fieldOf("connect_up").forGetter(MKDungeonConnectorSettings::connectUp),
            ResourceLocation.CODEC.fieldOf("top_cap_forward").forGetter(MKDungeonConnectorSettings::topCapForward),
            ResourceLocation.CODEC.fieldOf("top_cap_back").forGetter(MKDungeonConnectorSettings::topCapBack)
    ).apply(instance, (mainForward, mainBack, branch, connectDown, connectUp, topCapForward, topCapBack) ->
            new MKDungeonConnectorSettings(
                    mainForward,
                    mainBack,
                    branch,
                    connectDown,
                    connectUp,
                    topCapForward,
                    topCapBack
            )));
}

