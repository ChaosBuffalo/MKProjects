package com.chaosbuffalo.mkcore.client.rendering.animations.spell;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationManager;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.MeleeAnimationPose;
import com.chaosbuffalo.mkcore.client.rendering.animations.melee.ModelPoseAnimator;
import com.chaosbuffalo.mkcore.client.rendering.skeleton.MCSkeleton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;

public class SpellAnimationManager {
    public static final ResourceLocation BIPED_FAMILY = MKCore.id("biped");
    public static final ResourceLocation DEFAULT_CATEGORY = MKCore.id("default");
    public static final ResourceLocation MISSING = MKCore.id("missing_spell_animation");
    public static final String PROFILE_FOLDER = "spell_cast_animations";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<ResourceLocation, SpellAnimationProfile> PROFILES = new HashMap<>();
    private static final Map<ProfileKey, SpellAnimationProfile> PROFILES_BY_CATEGORY = new HashMap<>();
    private static final CopyOnWriteArrayList<SpellAnimationProfileResolver> RESOLVERS = new CopyOnWriteArrayList<>();
    private static final Set<ResourceLocation> LOGGED_MISSING_PROFILES = new HashSet<>();
    private static final SpellAnimationProfile MISSING_PROFILE = new SpellAnimationProfile(MISSING, MKCore.id("any"),
            DEFAULT_CATEGORY, MeleeAnimationManager.MISSING, MeleeAnimationManager.MISSING);

    public static void registerResolver(SpellAnimationProfileResolver resolver) {
        RESOLVERS.add(resolver);
    }

    @Nullable
    public static SpellAnimationProfile getProfile(ResourceLocation id) {
        return PROFILES.get(id);
    }

    public static SpellAnimationProfile resolveProfile(LivingEntity entity, ResourceLocation family, ResourceLocation category) {
        SpellAnimationProfile resolved = resolveProfileOverride(entity, family);
        if (resolved != null) {
            return resolved;
        }

        SpellAnimationProfile categoryMatch = PROFILES_BY_CATEGORY.get(new ProfileKey(family, category));
        if (categoryMatch != null) {
            return categoryMatch;
        }

        SpellAnimationProfile defaultMatch = PROFILES_BY_CATEGORY.get(new ProfileKey(family, DEFAULT_CATEGORY));
        if (defaultMatch != null) {
            return defaultMatch;
        }

        logMissingProfile(family, category);
        return MISSING_PROFILE;
    }

    @Nullable
    public static SpellAnimationProfile resolveProfileOverride(LivingEntity entity, ResourceLocation family) {
        for (SpellAnimationProfileResolver resolver : RESOLVERS) {
            ResourceLocation id = resolver.resolve(entity);
            SpellAnimationProfile profile = id == null ? null : getProfile(id);
            if (profile != null && profile.family().equals(family)) {
                return profile;
            }
        }
        return null;
    }

    public static boolean applyCastingPose(MCSkeleton skeleton, LivingEntity entity, ResourceLocation family,
                                           ResourceLocation category, ModelPoseAnimator.Context context) {
        return applyCastingPose(skeleton, entity, family, category, context, (originalTarget, resolvedTarget) -> true);
    }

    public static boolean applyCastingPose(MCSkeleton skeleton, LivingEntity entity, ResourceLocation family,
                                           ResourceLocation category, ModelPoseAnimator.Context context,
                                           BiPredicate<String, String> targetFilter) {
        SpellAnimationProfile profile = resolveProfile(entity, family, category);
        return applyPose(skeleton, profile.family(), profile.castingPose(), context, targetFilter);
    }

    public static boolean applyReleasePose(MCSkeleton skeleton, LivingEntity entity, ResourceLocation family,
                                           ResourceLocation category, ModelPoseAnimator.Context context) {
        return applyReleasePose(skeleton, entity, family, category, context, (originalTarget, resolvedTarget) -> true);
    }

    public static boolean applyReleasePose(MCSkeleton skeleton, LivingEntity entity, ResourceLocation family,
                                           ResourceLocation category, ModelPoseAnimator.Context context,
                                           BiPredicate<String, String> targetFilter) {
        SpellAnimationProfile profile = resolveProfile(entity, family, category);
        return applyPose(skeleton, profile.family(), profile.releasePose(), context, targetFilter);
    }

    private static boolean applyPose(MCSkeleton skeleton, ResourceLocation family, ResourceLocation poseId,
                                     ModelPoseAnimator.Context context) {
        return applyPose(skeleton, family, poseId, context, (originalTarget, resolvedTarget) -> true);
    }

    private static boolean applyPose(MCSkeleton skeleton, ResourceLocation family, ResourceLocation poseId,
                                     ModelPoseAnimator.Context context, BiPredicate<String, String> targetFilter) {
        MeleeAnimationPose pose = MeleeAnimationManager.getPose(poseId);
        if (pose == null) {
            return false;
        }
        ModelPoseAnimator.apply(skeleton, family, pose, context, targetFilter);
        return true;
    }

    private static void logMissingProfile(ResourceLocation family, ResourceLocation category) {
        ResourceLocation key = MKCore.id(family.getNamespace() + "." + family.getPath() + "." + category.getNamespace() + "." + category.getPath());
        if (LOGGED_MISSING_PROFILES.add(key)) {
            MKCore.LOGGER.error("Missing spell animation profile for family {} and category {}", family, category);
        }
    }

    private static SpellAnimationProfile parseProfile(ResourceLocation id, JsonElement json) {
        return SpellAnimationProfile.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow().withId(id);
    }

    private static void validateLoadedData() {
        for (SpellAnimationProfile profile : PROFILES.values()) {
            if (MeleeAnimationManager.getPose(profile.castingPose()) == null) {
                MKCore.LOGGER.error("Spell animation profile {} references missing casting pose {}", profile.id(), profile.castingPose());
            }
            if (MeleeAnimationManager.getPose(profile.releasePose()) == null) {
                MKCore.LOGGER.error("Spell animation profile {} references missing release pose {}", profile.id(), profile.releasePose());
            }
        }
    }

    private record ProfileKey(ResourceLocation family, ResourceLocation category) {
    }

    public static class ProfileReloadListener extends SimpleJsonResourceReloadListener {
        public ProfileReloadListener() {
            super(GSON, PROFILE_FOLDER);
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
            PROFILES.clear();
            PROFILES_BY_CATEGORY.clear();
            LOGGED_MISSING_PROFILES.clear();
            for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
                try {
                    SpellAnimationProfile profile = parseProfile(entry.getKey(), entry.getValue());
                    PROFILES.put(entry.getKey(), profile);
                    PROFILES_BY_CATEGORY.put(new ProfileKey(profile.family(), profile.category()), profile);
                } catch (Exception e) {
                    MKCore.LOGGER.error("Failed to load spell animation profile {}", entry.getKey(), e);
                }
            }
            MKCore.LOGGER.info("Loaded {} spell animation profiles", PROFILES.size());
            validateLoadedData();
        }
    }
}
