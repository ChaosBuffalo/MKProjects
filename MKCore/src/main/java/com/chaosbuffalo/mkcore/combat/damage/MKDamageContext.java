package com.chaosbuffalo.mkcore.combat.damage;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.damage.MKDamageSource;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.init.CoreDamageTypes;
import com.chaosbuffalo.mkcore.utils.DamageUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MKDamageContext {
    private final LivingDamageEvent.Pre event;
    private final DamageSource source;
    private final LivingEntity target;
    @Nullable
    private final LivingEntity attacker;
    @Nullable
    private final Entity directEntity;
    private final IMKEntityData targetData;
    @Nullable
    private final IMKEntityData attackerData;
    private final MKDamageCategory category;
    @Nullable
    private final MKDamageType damageType;
    private final boolean blocked;
    private final boolean fullyBlocked;
    private final float incomingDamage;
    private float workingDamage;
    private final List<String> audit = new ArrayList<>();
    private final Map<String, Object> metadata = new HashMap<>();

    private MKDamageContext(LivingDamageEvent.Pre event) {
        this.event = event;
        this.source = event.getSource();
        this.target = event.getEntity();
        this.directEntity = source.getDirectEntity();
        this.attacker = source.getEntity() instanceof LivingEntity living ? living : null;
        this.targetData = MKCore.getEntityDataOrThrow(target);
        this.attackerData = attacker != null ? MKCore.getEntityDataOrThrow(attacker) : null;
        this.category = resolveCategory(source);
        this.damageType = resolveDamageType(source, category);
        this.blocked = DamageUtils.wasAlreadyPartiallyBlocked(source);
        this.fullyBlocked = DamageUtils.isFullyBlockedDamage(source, event.getNewDamage());
        this.incomingDamage = event.getNewDamage();
        this.workingDamage = incomingDamage;
    }

    public static MKDamageContext from(LivingDamageEvent.Pre event) {
        return new MKDamageContext(event);
    }

    private static MKDamageCategory resolveCategory(DamageSource source) {
        if (source.is(DamageTypes.FALL)) {
            return MKDamageCategory.FALL;
        }
        if (DamageUtils.isProjectileDamage(source)) {
            return MKDamageCategory.PROJECTILE;
        }
        if (source instanceof MKDamageSource mkDamageSource) {
            return mkDamageSource.isMeleeDamage() ? MKDamageCategory.MELEE : MKDamageCategory.SPELL;
        }
        if (DamageUtils.isMinecraftPhysicalDamage(source)) {
            return MKDamageCategory.MELEE;
        }
        return MKDamageCategory.OTHER;
    }

    @Nullable
    private static MKDamageType resolveDamageType(DamageSource source, MKDamageCategory category) {
        if (source instanceof MKDamageSource mkDamageSource) {
            return mkDamageSource.getMKDamageType();
        }
        return switch (category) {
            case MELEE -> CoreDamageTypes.MeleeDamage.get();
            case PROJECTILE -> CoreDamageTypes.RangedDamage.get();
            default -> null;
        };
    }

    public LivingDamageEvent.Pre getEvent() {
        return event;
    }

    public DamageSource getSource() {
        return source;
    }

    public LivingEntity getTarget() {
        return target;
    }

    @Nullable
    public LivingEntity getAttacker() {
        return attacker;
    }

    @Nullable
    public Entity getDirectEntity() {
        return directEntity;
    }

    public IMKEntityData getTargetData() {
        return targetData;
    }

    @Nullable
    public IMKEntityData getAttackerData() {
        return attackerData;
    }

    public MKDamageCategory getCategory() {
        return category;
    }

    @Nullable
    public MKDamageType getDamageType() {
        return damageType;
    }

    public boolean wasBlocked() {
        return blocked;
    }

    public boolean isFullyBlocked() {
        return fullyBlocked;
    }

    public float getIncomingDamage() {
        return incomingDamage;
    }

    public float getWorkingDamage() {
        return workingDamage;
    }

    public void setWorkingDamage(float workingDamage, String reason) {
        this.workingDamage = workingDamage;
        audit.add(reason + "=" + workingDamage);
    }

    public void syncToEvent() {
        event.setNewDamage(workingDamage);
    }

    public void syncFromEvent() {
        workingDamage = event.getNewDamage();
    }

    public void putMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getMetadata(String key, Class<T> type) {
        Object value = metadata.get(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new IllegalStateException("Metadata key " + key + " was not of expected type " + type.getName());
        }
        return (T) value;
    }

    public List<String> getAudit() {
        return audit;
    }
}
