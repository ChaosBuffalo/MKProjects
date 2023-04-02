package com.chaosbuffalo.mkcore.abilities.training;

import com.chaosbuffalo.mkcore.abilities.AbilityManager;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.serialization.IDynamicMapTypedSerializer;
import com.chaosbuffalo.mkcore.serialization.ISerializableAttributeContainer;
import com.chaosbuffalo.mkcore.serialization.attributes.ISerializableAttribute;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class AbilityTrainingRequirement implements ISerializableAttributeContainer, IDynamicMapTypedSerializer {
    private static final String TYPE_ENTRY_NAME = "reqType";
    private final List<ISerializableAttribute<?>> attributes = new ArrayList<>();
    private final ResourceLocation typeName;

    public interface Deserializer extends Function<Dynamic<?>, AbilityTrainingRequirement> {

    }

    public AbilityTrainingRequirement(ResourceLocation typeName) {
        this.typeName = typeName;
        setupAttributes();
    }

    protected void setupAttributes() {

    }

    public <D> AbilityTrainingRequirement(ResourceLocation typeName, Dynamic<D> dynamic) {
        this(typeName);
        deserialize(dynamic);
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

    public abstract boolean check(MKPlayerData playerData, MKAbility ability);

    public abstract void onLearned(MKPlayerData playerData, MKAbility ability);

    public abstract MutableComponent describe(MKPlayerData playerData);

    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        deserializeAttributeMap(dynamic, "attributes");
    }

    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        builder.put(ops.createString("attributes"), serializeAttributeMap(ops));
    }

    @Override
    public String getTypeEntryName() {
        return TYPE_ENTRY_NAME;
    }

    public ResourceLocation getTypeName() {
        return typeName;
    }

    public static <D> Optional<ResourceLocation> getType(Dynamic<D> dynamic) {
        return IDynamicMapTypedSerializer.getType(dynamic, TYPE_ENTRY_NAME);
    }

    public static <D> DataResult<AbilityTrainingRequirement> fromDynamic(Dynamic<D> dynamic) {
        Optional<ResourceLocation> optType = getType(dynamic);
        if (!optType.isPresent()) {
            return DataResult.error(String.format("Unable to determine AbilityTrainingRequirement type (Raw: '%s')", dynamic));
        }
        ResourceLocation typeName = optType.get();
        AbilityTrainingRequirement.Deserializer deserializer = AbilityManager.getTrainingRequirementDeserializer(typeName);
        if (deserializer != null) {
            AbilityTrainingRequirement requirement = deserializer.apply(dynamic);
            if (requirement != null) {
                return DataResult.success(requirement);
            } else {
                return DataResult.error(String.format("Failed to decode AbilityTrainingRequirement of type '%s' (Raw: '%s')", typeName, dynamic));
            }
        } else {
            return DataResult.error(String.format("Failed to find AbilityTrainingRequirement decoder for type '%s'", typeName));
        }
    }
}
