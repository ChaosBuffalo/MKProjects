package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TalentLineDefinition {
    private final TalentTreeDefinition tree;
    private final String name;
    private final List<TalentNode> nodes;

    public TalentLineDefinition(TalentTreeDefinition tree, String name) {
        this.tree = tree;
        this.name = name;
        nodes = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public TalentTreeDefinition getTree() {
        return tree;
    }

    public int getLength() {
        return nodes.size();
    }

    public TalentNode getNode(int index) {
        if (index < nodes.size()) {
            return nodes.get(index);
        }
        return null;
    }

    public void addNode(TalentNode node) {
        node.link(this, nodes.size());
        nodes.add(node);
    }

    public List<TalentNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public static <T> TalentLineDefinition deserialize(TalentTreeDefinition tree, Dynamic<T> dynamic) {
        Optional<String> nameOpt = dynamic.get("name").asString()
                .resultOrPartial(error -> MKCore.LOGGER.error("Failed to deserialize talent line in tree {}: {}", tree.getTreeId(), error));
        if (!nameOpt.isPresent())
            return null;

        TalentLineDefinition line = new TalentLineDefinition(tree, nameOpt.get());
        for (DataResult<TalentNode> nodeResult : dynamic.get("talents")
                .asListOpt(line::deserializeNode)
                .resultOrPartial(error ->
                        MKCore.LOGGER.error("Failed to deserialize talent line entry {}:{}: {}", tree.getTreeId(), line.getName(), error))
                .orElse(Collections.emptyList())) {

            Optional<TalentNode> node = nodeResult.resultOrPartial(error ->
                    MKCore.LOGGER.error("Stopping parsing talent line {}:{} at index {}: {}", tree.getTreeId(), line.getName(), line.getNodes().size(), error));
            if (node.isPresent()) {
                line.addNode(node.get());
            } else {
                break;
            }
        }

        return line;
    }

    private <T> DataResult<TalentNode> deserializeNode(Dynamic<T> entry) {
        Optional<String> nameOpt = entry.get("name").asString()
                .resultOrPartial(error -> MKCore.LOGGER.error("Failed to deserialize talent node: {}", error));
        if (!nameOpt.isPresent()) {
            return DataResult.error("Node did not have a name");
        }

        ResourceLocation nodeType = new ResourceLocation(nameOpt.get());
        MKTalent talentType = MKCoreRegistry.TALENTS.getValue(nodeType);
        if (talentType == null) {
            return DataResult.error("Node referenced unknown talent " + nodeType);
        }

        return DataResult.success(talentType.createNode(entry));
    }

    public <T> T serialize(DynamicOps<T> ops) {
        ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        builder.put(ops.createString("name"), ops.createString(name));
        builder.put(ops.createString("talents"), ops.createList(nodes.stream().map(n -> n.serialize(ops))));
        return ops.createMap(builder.build());
    }
}
