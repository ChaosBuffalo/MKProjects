package com.chaosbuffalo.mknpc.quest.rewards;

import com.chaosbuffalo.mkcore.serialization.IDynamicMapTypedSerializer;
import com.chaosbuffalo.mkcore.serialization.ISerializableAttributeContainer;
import com.chaosbuffalo.mkcore.serialization.attributes.ISerializableAttribute;
import com.chaosbuffalo.mknpc.MKNpc;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class QuestReward implements ISerializableAttributeContainer, IDynamicMapTypedSerializer {
    private static final String TYPE_NAME_FIELD = "rewardType";
    public final static ResourceLocation INVALID_OPTION = new ResourceLocation(MKNpc.MODID, "quest_reward.invalid");
    protected static final MutableComponent defaultDescription = new TextComponent("Placeholder");
    private MutableComponent description;
    private final ResourceLocation typeName;
    private final List<ISerializableAttribute<?>> attributes = new ArrayList<>();

    public QuestReward(ResourceLocation typeName, MutableComponent description) {
        this.description = description;
        this.typeName = typeName;
    }

    public MutableComponent getDescription() {
        return description;
    }

    protected boolean hasPersistentDescription() {
        return true;
    }

    @Override
    public List<ISerializableAttribute<?>> getAttributes() {
        return attributes;
    }

    @Override
    public void addAttribute(ISerializableAttribute<?> iSerializableAttribute) {
        this.attributes.add(iSerializableAttribute);
    }

    @Override
    public void addAttributes(ISerializableAttribute<?>... iSerializableAttributes) {
        this.attributes.addAll(Arrays.asList(iSerializableAttributes));
    }

    @Override
    public ResourceLocation getTypeName() {
        return typeName;
    }

    @Override
    public String getTypeEntryName() {
        return TYPE_NAME_FIELD;
    }

    public static <D> ResourceLocation getType(Dynamic<D> dynamic) {
        return IDynamicMapTypedSerializer.getType(dynamic, TYPE_NAME_FIELD).orElse(INVALID_OPTION);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        if (hasPersistentDescription()) {
            builder.put(ops.createString("description"), ops.createString(Component.Serializer.toJson(description)));
        }
        builder.put(ops.createString("attributes"), serializeAttributeMap(ops));
    }

    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        if (hasPersistentDescription()) {
            description = dynamic.get("description").asString()
                    .resultOrPartial(MKNpc.LOGGER::error)
                    .map(Component.Serializer::fromJson)
                    .orElse(defaultDescription);
        }

        deserializeAttributeMap(dynamic, "attributes");
    }

    public abstract void grantReward(Player player);
}
