package com.chaosbuffalo.mkcore.serialization;

import com.chaosbuffalo.mkcore.serialization.attributes.ISerializableAttribute;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface ISerializableAttributeContainer {

    List<ISerializableAttribute<?>> getAttributes();

    void addAttribute(ISerializableAttribute<?> attribute);

    void addAttributes(ISerializableAttribute<?>... attributes);

    default <D> void deserializeAttributeMap(Dynamic<D> dynamic, String field) {
        Map<String, Dynamic<D>> map = dynamic.get(field).asMap(d -> d.asString(""), Function.identity());
        getAttributes().forEach(attr -> {
            Dynamic<D> attrValue = map.get(attr.getName());
            if (attrValue != null) {
                attr.deserialize(attrValue);
            }
        });
    }

    default <D> D serializeAttributeMap(DynamicOps<D> ops) {
        return ops.createMap(getAttributes().stream()
                .map(attr -> Pair.of(ops.createString(attr.getName()), attr.serialize(ops)))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
    }
}
