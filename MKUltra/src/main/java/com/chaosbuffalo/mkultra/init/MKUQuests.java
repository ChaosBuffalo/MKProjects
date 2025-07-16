package com.chaosbuffalo.mkultra.init;

import com.chaosbuffalo.mknpc.quest.QuestDefinition;
import com.chaosbuffalo.mknpc.quest.QuestRegistries;
import com.chaosbuffalo.mkultra.MKUltra;
import net.minecraft.resources.ResourceKey;

public class MKUQuests {

    static ResourceKey<QuestDefinition> key(String path) {
        return ResourceKey.create(QuestRegistries.QUEST_DEFINITIONS, MKUltra.id(path));
    }

    public static final ResourceKey<QuestDefinition> INTRO_QUEST = key("intro_quest");
    public static final ResourceKey<QuestDefinition> TROOPER_ARMOR = key("trooper_armor");
    public static final ResourceKey<QuestDefinition> CLERIC_UNLOCK_CHAIN = key("cleric_unlock_chain");
    public static final ResourceKey<QuestDefinition> CLERIC_INTRO = key("cleric_intro");
    public static final ResourceKey<QuestDefinition> NETHER_MAGE_INTRO = key("nether_mage_intro");
    public static final ResourceKey<QuestDefinition> UNLOCK_THEMCROMANCERS = key("unlock_themcromancers");
    public static final ResourceKey<QuestDefinition> NECROMANCER_UNLOCK_CHAIN = key("necromancer_unlock_chain");
}
