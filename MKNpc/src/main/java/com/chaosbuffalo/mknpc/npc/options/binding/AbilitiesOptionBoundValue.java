package com.chaosbuffalo.mknpc.npc.options.binding;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.capabilities.CoreCapabilities;
import com.chaosbuffalo.mknpc.npc.NpcAbilityEntry;
import com.chaosbuffalo.mknpc.npc.options.AbilitiesOption;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class AbilitiesOptionBoundValue implements IBoundNpcOptionValue {
    public static final Codec<AbilitiesOptionBoundValue> CODEC = Codec.list(NpcAbilityEntry.CODEC)
            .xmap(AbilitiesOptionBoundValue::new, i -> i.abilities);

    private final List<NpcAbilityEntry> abilities;

    public AbilitiesOptionBoundValue(List<NpcAbilityEntry> entries) {
        this.abilities = entries;
    }

    @Override
    public ResourceLocation getOptionId() {
        return AbilitiesOption.NAME;
    }

    @Override
    public void applyToEntity(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.getCapability(CoreCapabilities.ENTITY_CAPABILITY).ifPresent((cap) -> {
                List<ResourceLocation> toUnlearn = new ArrayList<>();
                for (MKAbilityInfo ability : cap.getAbilities().getAllAbilities()) {
                    toUnlearn.add(ability.getId());
                }
                for (ResourceLocation loc : toUnlearn) {
                    cap.getAbilities().unlearnAbility(loc, AbilitySource.TRAINED);
                }
                for (NpcAbilityEntry entry : abilities) {
                    MKAbility ability = MKCoreRegistry.getAbility(entry.getAbilityId());
                    if (ability != null) {
                        cap.getAbilities().learnAbility(ability, entry.getPriority());
                    }
                }
            });
        }
    }
}
