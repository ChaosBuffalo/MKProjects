package com.chaosbuffalo.mkcore.init;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
import com.chaosbuffalo.mkcore.core.talents.nodes.AttributeTalentNode;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

public class CoreTalentTrees {

    private static ResourceKey<TalentTreeDefinition> key(String name) {
        return ResourceKey.create(MKCoreRegistry.TALENT_TREE_REGISTRY_KEY, MKCore.id(name));
    }

    public static final ResourceKey<TalentTreeDefinition> KNIGHT_TEST = key("knight_test");

    private static @NotNull TalentTreeDefinition makeKnightTestTree(BootstrapContext<TalentTreeDefinition> context) {
        var displayNodes = context.lookup(MKCoreRegistry.TALENT_NODE_DISPLAY_REGISTRY_KEY);

        var tree = new TalentTreeDefinition.Builder(Component.literal("Core Knight Test"));
        tree.setVersion(1);
        var line = tree.createLine("knight_1");
        line.addNode(new AttributeTalentNode(Attributes.MAX_HEALTH, displayNodes.getOrThrow(CoreTalentDisplayNodes.MAX_HEALTH), 1, 1.0));
        return tree.build();
    }


    public static void bootstrap(BootstrapContext<TalentTreeDefinition> context) {
        context.register(KNIGHT_TEST, makeKnightTestTree(context));
    }
}
