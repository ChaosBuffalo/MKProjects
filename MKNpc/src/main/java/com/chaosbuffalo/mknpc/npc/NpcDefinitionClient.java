package com.chaosbuffalo.mknpc.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class NpcDefinitionClient {
    public static final Codec<NpcDefinitionClient> CODEC = RecordCodecBuilder.<NpcDefinitionClient>mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("def_name").forGetter(NpcDefinitionClient::getDefinitionName),
            Codec.STRING.fieldOf("name").forGetter(NpcDefinitionClient::getName),
            ResourceLocation.CODEC.fieldOf("faction").forGetter(NpcDefinitionClient::getFaction)
    ).apply(builder, NpcDefinitionClient::new)).codec();

    private final ResourceLocation defName;
    private final String name;
    private final ResourceLocation faction;

    public NpcDefinitionClient(NpcDefinition definition) {
        this(definition.getDefinitionName(), definition.getDisplayName(), definition.getFactionName());
    }

    public NpcDefinitionClient(ResourceLocation defName, String name, ResourceLocation faction) {
        this.defName = defName;
        this.name = name;
        this.faction = faction;
    }

    public String getName() {
        return name;
    }

    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(getDefinitionName());
        buffer.writeUtf(getName());
        buffer.writeResourceLocation(getFaction());
    }

    public ResourceLocation getDefinitionName() {
        return defName;
    }

    public ResourceLocation getFaction() {
        return faction;
    }

    public static NpcDefinitionClient fromBuffer(FriendlyByteBuf buffer) {
        return new NpcDefinitionClient(buffer.readResourceLocation(),
                buffer.readUtf(), buffer.readResourceLocation());
    }

}
