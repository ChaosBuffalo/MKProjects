package com.chaosbuffalo.mkchat.data;

import com.chaosbuffalo.mkchat.ChatRegistries;
import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.*;
import com.chaosbuffalo.mkchat.dialogue.conditions.HasFlagCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.InvertCondition;
import com.chaosbuffalo.mkchat.dialogue.effects.AddFlagEffect;
import com.chaosbuffalo.mkchat.dialogue.effects.AddLevelEffect;
import net.minecraft.DetectedVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber
public class MKChatGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        gen.addProvider(event.includeServer(), new MKChatRegistrySets(gen.getPackOutput(), event.getLookupProvider()));

        // pack.mcmeta
        gen.addProvider(true, new PackMetadataGenerator(gen.getPackOutput())
                .add(PackMetadataSection.TYPE, new PackMetadataSection(
                        Component.literal("MKChat resources"),
                        DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA),
                        Optional.of(new InclusiveRange<>(0, Integer.MAX_VALUE))
                ))
        );
    }


    static class MKChatRegistrySets extends DatapackBuiltinEntriesProvider {
        public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
                .add(ChatRegistries.DIALOGUE_TREES, Dialogues::bootstrap);

        public MKChatRegistrySets(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries, BUILDER, Set.of(MKChat.MODID));
        }
    }

    public static class Dialogues {

        static ResourceKey<DialogueTree> key(String path) {
            return ResourceKey.create(ChatRegistries.DIALOGUE_TREES, MKChat.id(path));
        }

        public static final ResourceKey<DialogueTree> TEST_TREE = key("test");

        public static void bootstrap(BootstrapContext<DialogueTree> context) {
            context.register(TEST_TREE, getTestTree(TEST_TREE));
        }

        private static DialogueTree getTestTree(ResourceKey<DialogueTree> key) {
            DialogueTree tree = new DialogueTree(key);
            DialogueNode grantLevel = new DialogueNode("grant_level", "Here is 1 level.");
            grantLevel.addEffect(new AddLevelEffect(1));
            ResourceLocation levelFlag = MKChat.id("grant_level");
            grantLevel.addEffect(new AddFlagEffect(levelFlag));

            tree.addNode(grantLevel);

            DialogueNode alreadyGranted = new DialogueNode("already_granted",
                    "You already got a level, don't be greedy.");

            DialogueNode cantHelp = new DialogueNode("cant_help",
                    "I have already helped you as much as I can.");

            tree.addNode(alreadyGranted);
            tree.addNode(cantHelp);

            DialoguePrompt needXp = new DialoguePrompt("need_xp", "need xp",
                    "I need xp.", "need some xp");
            needXp.addResponse(new DialogueResponse("grant_level")
                    .addCondition(new InvertCondition(new HasFlagCondition(levelFlag))));
            needXp.addResponse(new DialogueResponse("already_granted")
                    .addCondition(new HasFlagCondition(levelFlag)));

            tree.addPrompt(needXp);

            DialoguePrompt hail = new DialoguePrompt("hail", "", "", "")
                    .addResponse(new DialogueResponse("root")
                            .addCondition(new InvertCondition(new HasFlagCondition(levelFlag))))
                    .addResponse(new DialogueResponse("cant_help")
                            .addCondition(new HasFlagCondition(levelFlag))
                    );


            DialogueNode root = new DialogueNode("root", String.format("Hello %s, I am %s. Do you %s",
                    DialogueContexts.PLAYER_NAME_CONTEXT, DialogueContexts.ENTITY_NAME_CONTEXT, needXp.getPromptEmbed()));

            tree.addNode(root);
            tree.addPrompt(hail);

            tree.setHailPrompt(hail);

            return tree;
        }

    }
}
