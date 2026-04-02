package com.chaosbuffalo.mkultra.data.generators.npc;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mknpc.data.NpcDefinitionBuilder;
import com.chaosbuffalo.mknpc.data.NpcGenUtils;
import com.chaosbuffalo.mknpc.init.MKNpcEntityTypes;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mkultra.init.*;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

public class FireShrineNpcs {

    public static final ResourceKey<NpcDefinition> fire_sprite = MKUNpcs.key("fire_sprite");


    public static void bootstrap(BootstrapContext<NpcDefinition> context) {
        context.register(fire_sprite, generateFireSprite(fire_sprite, context));
    }


    static NpcDefinition generateFireSprite(ResourceKey<NpcDefinition> key, BootstrapContext<NpcDefinition> context) {
        var entitlements = context.lookup(MKCoreRegistry.ENTITLEMENT_REGISTRY_KEY);

        return new NpcDefinitionBuilder(key, MKNpcEntityTypes.BLAZE_TYPE)
                .faction(MKUFactions.FIRE_SHRINE_GUARDIANS_NAME)
                .size(0.9f)
                .name("Fire Sprite")
                .health(50.0)
                .mana(50.0)
                .manaRegen(5.0)
                .ability(MKUAbilities.FIREBALL, 1, 1.0)
                .ability(MKUAbilities.FIREBALL_BURST, 2, 0.3)
                .skillClass(NpcGenUtils.NpcSkillClass.MAGE)
                .xp(25)
                .build();
    }
}
