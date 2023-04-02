package com.chaosbuffalo.mknpc.quest.dialogue;

import com.chaosbuffalo.mknpc.npc.MKStructureEntry;
import com.chaosbuffalo.mknpc.npc.NotableNpcEntry;
import com.chaosbuffalo.mknpc.quest.QuestChainInstance;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class NpcDialogueUtils {

    public static class QuestDialogueParse {
        public final String args;
        public final Map<ResourceLocation, List<MKStructureEntry>> questStructures;
        public final QuestChainInstance questChain;

        public QuestDialogueParse(String args, Map<ResourceLocation, List<MKStructureEntry>> questStructures, QuestChainInstance instance){
            this.args = args;
            this.questStructures = questStructures;
            this.questChain = instance;
        }
    }

    public static final Map<String, Function<QuestDialogueParse, String>> QuestDialogueHandlers = new HashMap<>();

    public static void putDialogueHandler(String name, Function<QuestDialogueParse, String> handler){
        QuestDialogueHandlers.put(name, handler);
    }

    public static final Function<QuestDialogueParse, String> NotableHandler = (parseData) -> {
        String[] splitArgs = parseData.args.split("#");
        ResourceLocation structureName = new ResourceLocation(splitArgs[0]);
        int index = Integer.parseInt(splitArgs[1]);
        ResourceLocation defName = new ResourceLocation(splitArgs[2]);
        Optional<NotableNpcEntry> npc = parseData.questStructures.get(structureName).get(index)
                .getFirstNotableOfType(defName);
        return npc.map(x -> String.format("{notable:%s}", x.getNotableId())).orElse("#notable.not_found#");
    };

    public static void setupMKNpcHandlers(){
        putDialogueHandler("mk_quest_notable", NotableHandler);
    }

    public static String getNotableNpcRaw(ResourceLocation structureName, int index, ResourceLocation defName){
        return String.format("{mk_quest_notable:%s#%s#%s}", structureName.toString(), index, defName.toString());
    }

    public static String parseQuestDialogueMessage(String text,
                                                   Map<ResourceLocation, List<MKStructureEntry>> questStructures,
                                                   QuestChainInstance questChain) {
        String parsing = text;
        StringBuilder ret = new StringBuilder();
        while (!parsing.isEmpty()) {
            if (parsing.contains("{") && parsing.contains("}")) {
                int index = parsing.indexOf("{");
                int endIndex = parsing.indexOf("}");
                ret.append(parsing, 0, index);
                String parsee = parsing.substring(index, endIndex + 1);
                if (parsee.startsWith("{mk_quest")){
                    ret.append(handleMKQuestEntry(parsee, questStructures, questChain));
                } else {
                    ret.append(parsee);
                }
                parsing = parsing.substring(endIndex + 1);
            } else {
                ret.append(parsing);
                parsing = "";
            }
        }
        return ret.toString();
    }


    public static String handleMKQuestEntry(String parsee,
                                            Map<ResourceLocation, List<MKStructureEntry>> questStructures,
                                            QuestChainInstance questChain){
        String request = parsee.replace("{", "").replace("}", "");
        if (request.contains(":")) {
            String[] requestSplit = request.split(":", 2);
            String reqName = requestSplit[0];
            String args = requestSplit[1];
            if (QuestDialogueHandlers.containsKey(reqName)){
                return QuestDialogueHandlers.get(reqName).apply(new QuestDialogueParse(args, questStructures, questChain));
            } else {
                return parsee;
            }
        } else {
            return parsee;
        }
    }
}
