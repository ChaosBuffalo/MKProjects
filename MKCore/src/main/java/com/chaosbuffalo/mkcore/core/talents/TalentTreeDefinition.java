package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TalentTreeDefinition {
    public static final Codec<TalentTreeDefinition> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ComponentSerialization.CODEC.fieldOf("display_name").forGetter(TalentTreeDefinition::getName),
            Codec.INT.fieldOf("version").forGetter(TalentTreeDefinition::getVersion),
            Codec.BOOL.fieldOf("is_default").forGetter(TalentTreeDefinition::isDefault),
            TalentLineDefinition.CODEC.listOf(1, 3).fieldOf("lines").forGetter(i -> i.talentLineList)
    ).apply(builder, TalentTreeDefinition::new));

    public static final Codec<Holder<TalentTreeDefinition>> REFERENCE_CODEC = RegistryFixedCodec.create(MKCoreRegistry.TALENT_TREE_REGISTRY_KEY);

    private final List<TalentLineDefinition> talentLineList;
    private final Component displayName;
    private final boolean isDefault;
    private final int version;

    private TalentTreeDefinition(Component displayName, int version, boolean isDefault, List<TalentLineDefinition> talentLineList) {
        this.displayName = displayName;
        this.version = version;
        this.isDefault = isDefault;
        this.talentLineList = talentLineList;
        talentLineList.forEach(line -> line.link(this));
    }

    public boolean isDefault() {
        return isDefault;
    }

    public int getVersion() {
        return version;
    }

    public Map<String, TalentLineDefinition> getTalentLines() {
        return talentLineList.stream().collect(Collectors.toMap(
                TalentLineDefinition::getName,
                Function.identity()
        ));
    }

    public Component getName() {
        return displayName;
    }

    @Nullable
    public TalentLineDefinition getLine(String name) {
        for (var line : talentLineList) {
            if (line.getName().equals(name)) {
                return line;
            }
        }
        return null;
    }

    public TalentTreeRecord createRecord(ResourceLocation treeId) {
        var treeKey = ResourceKey.create(MKCoreRegistry.TALENT_TREE_REGISTRY_KEY, treeId);
        return new TalentTreeRecord(this, treeKey);
    }

    public static String nameKey(ResourceLocation treeId) {
        return treeId.toLanguageKey("talent_tree", "name");
    }


    public static class Builder {

        private final Component name;
        private int version;
        private boolean isDefault;
        private final List<TalentLineDefinition.Builder> pendingLines = new ArrayList<>();

        public Builder(Component name) {
            this.name = name;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public void setDefault(boolean isDefault) {
            this.isDefault = isDefault;
        }

        public TalentLineDefinition.Builder createLine(String name) {
            var builder = new TalentLineDefinition.Builder(name);
            pendingLines.add(builder);
            return builder;
        }

        public TalentTreeDefinition build() {
            List<TalentLineDefinition> lines = new ArrayList<>();
            for (var pendingLine : pendingLines) {
                lines.add(pendingLine.build());
            }
            return new TalentTreeDefinition(name, version, isDefault, lines);
        }
    }
}
