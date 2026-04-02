package com.chaosbuffalo.mkcore.core.talents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class TalentLineDefinition {
    public static final Codec<TalentLineDefinition> CODEC = RecordCodecBuilder.<TalentLineDefinition>create(builder -> builder.group(
            Codec.STRING.fieldOf("line_name").forGetter(i -> i.name),
            TalentNode.CODEC.listOf().fieldOf("talents").forGetter(i -> i.nodes)
    ).apply(builder, TalentLineDefinition::new)).validate(line -> {
        if (ResourceLocation.isValidPath(line.name)) {
            return DataResult.success(line);
        }
        return DataResult.error(() -> "Talent line '%s' must contain only lowercase letters in the name".formatted(line.name));
    });

    private final String name;
    private final List<TalentNode> nodes;
    private TalentTreeDefinition tree;

    private TalentLineDefinition(String name, List<TalentNode> nodes) {
        this.name = name;
        this.nodes = nodes;
        IntStream.range(0, nodes.size()).forEach(i -> this.nodes.get(i).link(this, i));
    }

    void link(TalentTreeDefinition tree) {
        this.tree = tree;
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

    public List<TalentNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public static class Builder {
        private final String name;
        private final List<TalentNode> nodes;

        public Builder(String name) {
            this.name = name;
            this.nodes = new ArrayList<>();
        }

        public void addNode(TalentNode node) {
            nodes.add(node);
        }

        public TalentLineDefinition build() {
            return new TalentLineDefinition(name, nodes);
        }
    }
}
