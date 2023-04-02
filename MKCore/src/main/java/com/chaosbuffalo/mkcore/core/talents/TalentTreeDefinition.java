package com.chaosbuffalo.mkcore.core.talents;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TalentTreeDefinition {

    private final Map<String, TalentLineDefinition> talentLines = new HashMap<>();
    private final ResourceLocation treeId;
    private boolean isDefault;
    private int version;

    public TalentTreeDefinition(ResourceLocation name) {
        treeId = name;
        version = -1;
        isDefault = false;
    }

    public ResourceLocation getTreeId() {
        return treeId;
    }

    public void setDefault(boolean value) {
        isDefault = value;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public Map<String, TalentLineDefinition> getTalentLines() {
        return Collections.unmodifiableMap(talentLines);
    }

    public BaseComponent getName() {
        return new TranslatableComponent(String.format("%s.%s.name", treeId.getNamespace(), treeId.getPath()));
    }

    public TalentLineDefinition getLine(String name) {
        return talentLines.get(name);
    }

    public boolean hasLine(String name) {
        return talentLines.containsKey(name);
    }

    public boolean containsIndex(String lineName, int index) {
        return getNode(lineName, index) != null;
    }

    public TalentNode getNode(String lineName, int index) {
        TalentLineDefinition line = getLine(lineName);
        if (line == null)
            return null;
        return line.getNode(index);
    }

    public void addLine(TalentLineDefinition line) {
        talentLines.put(line.getName(), line);
    }

    public TalentTreeRecord createRecord() {
        return new TalentTreeRecord(this);
    }

    public static <T> TalentTreeDefinition deserialize(ResourceLocation treeId, Dynamic<T> dynamic) {
        TalentTreeDefinition tree = new TalentTreeDefinition(treeId);
        tree.deserialize(dynamic);
        return tree;
    }

    public <T> void deserialize(Dynamic<T> dynamic) {
        version = dynamic.get("version").asInt(1);
        isDefault = dynamic.get("isDefault").asBoolean(false);
        dynamic.get("lines")
                .asList(d -> TalentLineDefinition.deserialize(this, d))
                .stream().filter(Objects::nonNull)
                .forEach(this::addLine);
    }

    public <T> T serialize(DynamicOps<T> ops) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(ops.createString("version"), ops.createInt(getVersion()));
        builder.put(ops.createString("isDefault"), ops.createBoolean(isDefault()));
        builder.put(ops.createString("lines"), ops.createList(talentLines.values().stream().map(d -> d.serialize(ops))));
        return ops.createMap(builder.build());
    }
}
