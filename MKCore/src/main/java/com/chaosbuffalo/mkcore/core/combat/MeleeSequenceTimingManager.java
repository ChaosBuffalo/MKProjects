package com.chaosbuffalo.mkcore.core.combat;

import com.chaosbuffalo.mkcore.core.MultiAttackHelper;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MeleeSequenceTimingManager {
    private static final List<MeleeSequenceTimingResolver> RESOLVERS = new CopyOnWriteArrayList<>();

    public static void registerResolver(MeleeSequenceTimingResolver resolver) {
        RESOLVERS.add(resolver);
    }

    public static MeleeSequenceTimings resolve(LivingEntity attacker, int attackCount, int firstAttackIndex,
                                               int baseCooldownTicks, int baseSwingDurationTicks, int currentSwingCount) {
        for (MeleeSequenceTimingResolver resolver : RESOLVERS) {
            MeleeSequenceTimings timings = resolver.resolve(attacker, attackCount, firstAttackIndex,
                    baseCooldownTicks, baseSwingDurationTicks, currentSwingCount);
            if (timings != null) {
                return timings;
            }
        }
        return MultiAttackHelper.createUniformSequenceTimings(baseCooldownTicks, attackCount, firstAttackIndex,
                baseSwingDurationTicks);
    }
}
