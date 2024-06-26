package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.core.player.IPlayerSyncComponentProvider;
import com.chaosbuffalo.mkcore.core.player.PlayerSyncComponent;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import com.chaosbuffalo.mkcore.sync.adapters.SyncMapUpdater;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;


import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityRiderModule implements IPlayerSyncComponentProvider {
    private final PlayerSyncComponent sync = new PlayerSyncComponent("riderModule");
    protected final IMKEntityData entityData;
    protected final Map<Integer, EntityRider> riders = new HashMap<>();
    protected final SyncMapUpdater<Integer, EntityRider> riderSync = new SyncMapUpdater<>("riders", riders, i -> Integer.toString(i), Integer::valueOf, EntityRider::createRider);;

    public EntityRiderModule(IMKEntityData entityData) {
        this.entityData = entityData;
        riderSync.setOnRemoveCallback(this::onClientRemove);
        addSyncPublic(riderSync);
    }

    public void addRider(Entity rider) {
        Vec3 offset = rider.position().subtract(entityData.getEntity().position()).yRot(entityData.getEntity().getYRot() * ((float)Math.PI / 180F));
        addRider(rider, offset);
    }

    public Collection<EntityRider> getRiders() {
        return riders.values();
    }

    public void addRider(Entity rider, Vec3 offset) {
        riders.put(rider.getId(), new EntityRider(rider, offset, rider.getXRot() - entityData.getEntity().getXRot()));
        riderSync.markDirty(rider.getId());
        rider.startRiding(entityData.getEntity(), true);
    }

    public void onClientRemove(int entityId) {
        EntityRider rider = riders.get(entityId);
        if (rider != null && rider.entity != null) {
            rider.entity.stopRiding();
        }
    }

    public void removeRider(Entity rider) {
        if (hasRider(rider)) {
            rider.stopRiding();
            riders.remove(rider.getId());
            riderSync.markDirty(rider.getId());
        }
    }

    public boolean hasRider(Entity rider) {
        return riders.containsKey(rider.getId());
    }

    public EntityRider getRider(Entity rider) {
        return riders.get(rider.getId());
    }

    @Override
    public PlayerSyncComponent getSyncComponent() {
        return sync;
    }

    public static class EntityRider implements IMKSerializable<CompoundTag> {
        protected Entity entity;
        protected Vec3 offset;
        protected float yawOffset;

        public EntityRider(Entity entity, Vec3 offset, float yawOffset) {
            this.entity = entity;
            this.offset = offset;
            this.yawOffset = yawOffset;
        }

        public Vec3 getOffset() {
            return offset;
        }

        public float getYawOffset() {
            return yawOffset;
        }

        public Entity getEntity() {
            return entity;
        }

        @Override
        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("offsetX", offset.x);
            tag.putDouble("offsetY", offset.y);
            tag.putDouble("offsetZ", offset.z);
            tag.putFloat("yawOffset", yawOffset);
            return tag;
        }

        @Override
        public boolean deserialize(CompoundTag tag) {
            double x = tag.getDouble("offsetX");
            double y = tag.getDouble("offsetY");
            double z = tag.getDouble("offsetZ");
            offset = new Vec3(x, y, z);
            yawOffset = tag.getFloat("yawOffset");
            return true;
        }

        public static EntityRider createRider(int id) {
            Entity entity = ClientHandler.handleClient(id);
            return new EntityRider(entity, Vec3.ZERO, 0.0f);
        }
    }

    static class ClientHandler {
        public static Entity handleClient(int entityId) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null)
                return null;
            return mc.level.getEntity(entityId);
        }
    }
}
