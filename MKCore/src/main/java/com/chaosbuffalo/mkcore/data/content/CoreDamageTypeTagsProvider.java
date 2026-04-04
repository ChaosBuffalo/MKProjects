package com.chaosbuffalo.mkcore.data.content;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.init.CoreTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class CoreDamageTypeTagsProvider extends DamageTypeTagsProvider {
    public CoreDamageTypeTagsProvider(PackOutput output,
                                      CompletableFuture<HolderLookup.Provider> lookupProvider,
                                      ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MKCore.MOD_ID, existingFileHelper);
    }

    @Nonnull
    @Override
    public String getName() {
        return "MKCore damage type tags";
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(CoreTags.DamageTypes.VANILLA_MELEE_DAMAGE)
                .add(DamageTypes.MOB_ATTACK, DamageTypes.PLAYER_ATTACK);
        tag(CoreTags.DamageTypes.MK_MELEE_DAMAGE)
                .addTag(CoreTags.DamageTypes.VANILLA_MELEE_DAMAGE);
        tag(CoreTags.DamageTypes.MK_PROJECTILE_DAMAGE)
                .add(DamageTypes.MOB_PROJECTILE, DamageTypes.ARROW);
    }
}
