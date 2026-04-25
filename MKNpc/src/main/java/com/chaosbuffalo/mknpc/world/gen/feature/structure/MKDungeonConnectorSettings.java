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
        ResourceLocation stairInsertDown,
        ResourceLocation stairInsertUp,
        ResourceLocation bossForward,
        ResourceLocation bossBack
) {
    public static final Codec<MKDungeonConnectorSettings> CODEC = RecordCodecBuilder.<MKDungeonConnectorSettings>create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("main_forward").forGetter(MKDungeonConnectorSettings::mainForward),
            ResourceLocation.CODEC.fieldOf("main_back").forGetter(MKDungeonConnectorSettings::mainBack),
            ResourceLocation.CODEC.fieldOf("branch").forGetter(MKDungeonConnectorSettings::branch),
            ResourceLocation.CODEC.fieldOf("connect_down").forGetter(MKDungeonConnectorSettings::connectDown),
            ResourceLocation.CODEC.fieldOf("connect_up").forGetter(MKDungeonConnectorSettings::connectUp),
            ResourceLocation.CODEC.optionalFieldOf("stair_insert_down").forGetter(settings ->
                    settings.stairInsertDown().equals(settings.connectDown()) ? java.util.Optional.empty() : java.util.Optional.of(settings.stairInsertDown())),
            ResourceLocation.CODEC.optionalFieldOf("stair_insert_up").forGetter(settings ->
                    settings.stairInsertUp().equals(settings.connectUp()) ? java.util.Optional.empty() : java.util.Optional.of(settings.stairInsertUp())),
            ResourceLocation.CODEC.fieldOf("boss_forward").forGetter(MKDungeonConnectorSettings::bossForward),
            ResourceLocation.CODEC.fieldOf("boss_back").forGetter(MKDungeonConnectorSettings::bossBack)
    ).apply(instance, (mainForward, mainBack, branch, connectDown, connectUp, stairInsertDown, stairInsertUp, bossForward, bossBack) ->
            new MKDungeonConnectorSettings(
                    mainForward,
                    mainBack,
                    branch,
                    connectDown,
                    connectUp,
                    stairInsertDown.orElse(connectDown),
                    stairInsertUp.orElse(connectUp),
                    bossForward,
                    bossBack
            )));
}
