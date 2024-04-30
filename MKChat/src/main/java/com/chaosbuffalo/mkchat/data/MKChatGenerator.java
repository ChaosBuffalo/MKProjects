package com.chaosbuffalo.mkchat.data;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.*;
import com.chaosbuffalo.mkchat.dialogue.conditions.HasFlagCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.InvertCondition;
import com.chaosbuffalo.mkchat.dialogue.effects.AddFlagEffect;
import com.chaosbuffalo.mkchat.dialogue.effects.AddLevelEffect;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKChatGenerator {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        gen.addProvider(event.includeServer(), new MKChatDialogueProvider(gen));
    }

    public static class MKChatDialogueProvider extends DialogueProvider {

        public MKChatDialogueProvider(DataGenerator generator) {
            super(generator, MKChat.MODID);
        }

        @Override
        public CompletableFuture<?> run(CachedOutput pOutput) {
            return CompletableFuture.allOf(writeDialogue(getTestTree(), pOutput));
        }

        private DialogueTree getTestTree() {
            DialogueTree tree = new DialogueTree(new ResourceLocation(MKChat.MODID, "test"));
            DialogueNode grantLevel = new DialogueNode("grant_level", "Here is 1 level.");
            grantLevel.addEffect(new AddLevelEffect(1));
            ResourceLocation levelFlag = new ResourceLocation(MKChat.MODID, "grant_level");
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
