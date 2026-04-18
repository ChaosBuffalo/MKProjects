package com.chaosbuffalo.mknpc.data.content;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.data.providers.NpcLanguageProvider;
import net.minecraft.data.PackOutput;

public class MKNpcLanguageProvider extends NpcLanguageProvider {
    public MKNpcLanguageProvider(PackOutput output, String locale) {
        super(output, MKNpc.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        addGui();
        addQuestRewards();
        addQuestObjectives();
        addQuestText();
        addCommands();
        addMisc();
        addBlocks();
        addKeys();
    }

    private void addGui() {
        add("mknpc.gui.objectives.name", "Objectives:");
        add("mknpc.gui.select_quest", "Select a quest to see details.");
        add("mknpc.gui.page.quests.name", "Quests");
        add("mknpc.gui.rewards.name", "Rewards:");
        add("mknpc.ui.search", "Search");
    }

    private void addQuestRewards() {
        add("mknpc.quest_reward.xp.name", "Grants %d xp.");
        add("mknpc.quest_reward.xp.message", "You gained %d experience points from completing a quest.");
        add("mknpc.grant_entitlement.message", "You gained an entitlement: %s");
        add("mknpc.quest_reward.entitlement.message", "Grants Entitlement: %s");
        add("mknpc.quest_reward.talent_tree_grant", "Unlocked Talent Tree: %s");
        add("mknpc.quest_reward.talent_tree_grant.message", "Unlocks the %s Talent Tree");
        add("mknpc.quest_reward.talent_tree_grant.message.error", "Unlocks the [Not Found] Talent Tree");
        add("mknpc.quest_reward.faction.name", "Grants %d faction to %s.");
        add("mknpc.quest_reward.faction.message", "You gained %d faction with %s from completing a quest.");
        add("mknpc.quest_reward.notable_faction_override.name", "Sets faction with a quest notable to %d.");
        add("mknpc.quest_reward.notable_faction_override.message", "%s now reacts to you as if your faction score were %d.");
    }

    private void addQuestObjectives() {
        add("mknpc.objective.kill_npc_def.desc", "Kill %s (%d / %d)");
        add("mknpc.objective.kill_notable.desc", "Kill %s");
        add("mknpc.objective.kill_notable.complete", "You Killed %s");
        add("mknpc.objective.quest_loot_npc.desc", "Loot %s from %s (%s / %s)");
        add("mknpc.objective.quest_loot_npc.progress", "You found %s on the corpse of %s (%s / %s).");
        add("mknpc.objective.kill_w_ability.desc", "Land Killing Blows with %s (%s / %s)");
        add("mknpc.objective.kill_type_tag.desc", "Kill Creatures of Type: %s (%s / %s)");
        add("mknpc.objective.quest_loot_type_tag.desc", "Loot %s from %s (%s / %s)");
        add("mknpc.objective.quest_loot_type_tag.progress", "You found %s on the corpse of %s (%s / %s).");
    }

    private void addQuestText() {
        add("mknpc.quest.trade_container", "Trading With %s");
        add("mknpc.quest.trade.dont_need", "%s %s, I do not need this %d %s you can have it back.");
        add("mknpc.quest.trade.accepted", "%s accepted your trade.");
        add("mknpc.trade.item_needed", "Trade %d %s");
        add("mknpc.trade.desc", "Trade (Shift Right-Click on Npc) the Following:");
        add("mknpc.quest.start_quest", "Started Quest: %s");
        add("mknpc.quest.cant_start_quest", "Unable to Start Quest: %s");
        add("mknpc.quest.complete_chain", "Completed Quest: %s");
    }

    private void addCommands() {
        add("mknpc.command.in_struct", "You are in a structure: %s with ID: %s");
        add("mknpc.command.not_in_struct", "You are not in a structure");
        add("mknpc.command.cant_find_cap", "Can't find mknpc World Capability, Command Failed");
        add("mknpc.command.pois_for_struct", "POIs for Structure: %s (ID: %s): ");
        add("mknpc.command.pois_struct_not_found", "No Data Found For Structure %s (ID: %s)");
        add("mknpc.command.pois_struct_no_poi", "No POI Found For Structure %s (ID: %s)");
        add("mknpc.command.pois_struct_desc", "POI: %s at %s");
        add("mknpc.command.reset_struct", "Resetting structure %s (ID: %s)");
    }

    private void addMisc() {
        add("mknpc.debug.enter_structure", "Entered structure %s (ID: %s)");
        add("mknpc.debug.exit_structure", "Exited structure %s (ID: %s)");
        add("entity.mknpc.fire_elemental", "Fire Elemental");
    }

    private void addBlocks() {
        add("block.mknpc.mk_spawner", "MK Spawner");
    }

    private void addKeys() {
        add("key.mknpc.category", "MKNpc");
        add("key.hud.questmenu", "Open Quest Journal");
    }
}
