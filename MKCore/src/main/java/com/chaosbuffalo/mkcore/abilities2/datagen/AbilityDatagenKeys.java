package com.chaosbuffalo.mkcore.abilities2.datagen;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities2.definition.AbilityTargetResolverDefinition;
import net.minecraft.resources.ResourceLocation;

public final class AbilityDatagenKeys {
    public static final ResourceLocation SLOT_FAMILY_BASIC = MKCore.makeRL("slot_family.basic");
    public static final ResourceLocation SLOT_FAMILY_PASSIVE = MKCore.makeRL("slot_family.passive");
    public static final ResourceLocation SLOT_FAMILY_ULTIMATE = MKCore.makeRL("slot_family.ultimate");

    public static final ResourceLocation TAG_SPELL = MKCore.makeRL("spell");
    public static final ResourceLocation TAG_PROJECTILE = MKCore.makeRL("projectile");
    public static final ResourceLocation TAG_PASSIVE = MKCore.makeRL("passive");
    public static final ResourceLocation TAG_CHANNEL = MKCore.makeRL("channel");
    public static final ResourceLocation TAG_AURA = MKCore.makeRL("aura");
    public static final ResourceLocation TAG_TOGGLE = MKCore.makeRL("toggle");
    public static final ResourceLocation TAG_HEAL = MKCore.makeRL("heal");
    public static final ResourceLocation TAG_FIRE = MKCore.makeRL("fire");

    public static final ResourceLocation SCHOOL_RESTORATION = MKCore.makeRL("restoration");
    public static final ResourceLocation SCHOOL_EVOCATION = MKCore.makeRL("evocation");

    public static final AbilityTargetResolverDefinition TARGET_NONE = new AbilityTargetResolverDefinition("none");
    public static final AbilityTargetResolverDefinition TARGET_SELF = new AbilityTargetResolverDefinition("self");
    public static final AbilityTargetResolverDefinition TARGET_EVENT_TARGET = new AbilityTargetResolverDefinition("event_target");
    public static final AbilityTargetResolverDefinition TARGET_EVENT_ACTOR = new AbilityTargetResolverDefinition("event_actor");
    public static final AbilityTargetResolverDefinition TARGET_RESOLVED = new AbilityTargetResolverDefinition("resolved");

    private AbilityDatagenKeys() {
    }
}
