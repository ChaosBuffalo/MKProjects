package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class TalentNodeDisplay {
    public static final Codec<TalentNodeDisplay> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("icon").forGetter(TalentNodeDisplay::getIcon),
            ResourceLocation.CODEC.fieldOf("filled_icon").forGetter(TalentNodeDisplay::getFilledIcon)
    ).apply(builder, TalentNodeDisplay::new));

    public static final Codec<Holder<TalentNodeDisplay>> REFERENCE_CODEC = RegistryFixedCodec.create(MKCoreRegistry.TALENT_NODE_DISPLAY_REGISTRY_KEY);
    public static final Codec<ResourceKey<TalentNodeDisplay>> KEY_CODEC = ResourceKey.codec(MKCoreRegistry.TALENT_NODE_DISPLAY_REGISTRY_KEY);
    private final ResourceLocation filledIcon;
    private final ResourceLocation icon;

    public TalentNodeDisplay(ResourceLocation icon, ResourceLocation filledIcon) {
        this.icon = icon;
        this.filledIcon = filledIcon;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public ResourceLocation getFilledIcon() {
        return filledIcon;
    }
}
