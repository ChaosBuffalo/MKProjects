package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.client.render.styling.MKUHumans;
import com.chaosbuffalo.mkultra.init.MKUAbilities;
import com.chaosbuffalo.mkultra.init.MKUEntities;
import com.chaosbuffalo.mkultra.init.MKUFactions;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class NecrotideNpcs {
    static NpcDefinition generateNecrotideCultist() {

        return new NpcDefinitionBuilder(MKUltra.id("necrotide_cultist"))
                .type(MKUEntities.HUMAN_TYPE)
                .faction(MKUFactions.NECROTIDE_CULTISTS_NAME)
                .renderGroup(MKUHumans.NECROTIDE_CULTIST_SKULL_1_NAME)
                .size(1.05f)
                .attribute(Attributes.MAX_HEALTH, 125.0)
                .attribute(MKAttributes.MAX_MANA, 125.0)
                .attribute(MKAttributes.MANA_REGEN, 3.0)
                .titledFactionName("Cultist")
                .ability(MKUAbilities.SHADOW_BOLT, 1, 1.0)
                .ability(MKUAbilities.DROWN, 2, 1.0)
                .ability(MKUAbilities.SHADOW_PULSE, 3, 1.0)
                .ability(MKUAbilities.NECROTIDE_WARRIOR_SUMMON, 4, 1.0)
                .skillClass(NpcGenUtils.NpcSkillClass.NECROMANCER)
                .battlecry()
                .build();
    }
}
