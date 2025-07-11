package com.chaosbuffalo.mkcore.data.providers;

import com.chaosbuffalo.mkcore.abilities.AbilityTranslations;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
import com.chaosbuffalo.mkcore.core.talents.talent_types.AttributeTalentType;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class MKLanguageProvider extends LanguageProvider {
    public MKLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    @Override
    protected void addTranslations() {

    }

    public void damageType(Holder<MKDamageType> type, String directName, String periodicName) {
        add(MKDamageType.nameKey(type.getKey().location()), directName);
        add(MKDamageType.periodicNameKey(type.getKey().location()), periodicName);
    }

    public void ability(Holder<MKAbility> ability, String name) {
        add(AbilityTranslations.nameKey(ability.getKey().location()), name);
    }

    public void ability(Holder<MKAbility> ability, String name, String description) {
        ability(ability, name);
        add(AbilityTranslations.descriptionKey(ability.getKey().location()), description);
    }

    public void mkEffect(Holder<MKEffect> effect, String name) {
        add(Util.makeDescriptionId("mk_effect", effect.getKey().location()), name);
    }

    public void entitlement(Holder<MKEntitlement> entitlement, String name) {
        add(MKEntitlement.nameKey(entitlement.getKey().location()), name);
    }

    public void entitlement(ResourceKey<MKEntitlement> entitlement, String name) {
        add(MKEntitlement.nameKey(entitlement.location()), name);
    }

    public void entitlement(Holder<MKEntitlement> entitlement, String name, String description) {
        add(MKEntitlement.nameKey(entitlement.getKey().location()), name);
        add(MKEntitlement.descriptionKey(entitlement.getKey().location()), description);
    }

    public void entitlement(ResourceKey<MKEntitlement> entitlement, String name, String description) {
        add(MKEntitlement.nameKey(entitlement.location()), name);
        add(MKEntitlement.descriptionKey(entitlement.location()), description);
    }

    public void talentTree(ResourceLocation talentTreeId, String name) {
        add(TalentTreeDefinition.nameKey(talentTreeId), name);
    }

    public void attribute(Holder<Attribute> attributeHolder, String name) {
        add(attributeHolder.value().getDescriptionId(), name);
    }

    public void attributeTalent(Holder<Attribute> attributeHolder, String name, String desc) {
        add(AttributeTalentType.nameKey(attributeHolder.getKey().location()), name);
        add(AttributeTalentType.descriptionKey(attributeHolder.getKey().location()), desc);
    }

}
