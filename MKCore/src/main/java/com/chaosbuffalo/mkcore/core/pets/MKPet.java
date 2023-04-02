package com.chaosbuffalo.mkcore.core.pets;


import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public class MKPet<T extends LivingEntity & IMKPet> {

    public static class ClientMKPet implements IMKSerializable<CompoundTag> {
        @Nullable
        protected Entity entity;
        protected int duration;
        protected ResourceLocation name;
        protected boolean hasDuration;

        public ClientMKPet(ResourceLocation name, Entity entity) {
            this.entity = entity;
            this.name = name;
        }

        public void setDuration(int duration) {
            this.duration = duration;
            hasDuration = true;
        }

        public int getDuration() {
            return duration;
        }

        public boolean hasDuration() {
            return hasDuration;
        }

        public ResourceLocation getName() {
            return name;
        }

        @Nullable
        public Entity getEntity() {
            return entity;
        }

        public boolean isActive() {
            return entity != null && entity.isAlive();
        }

        @Override
        public CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putString("name", name.toString());
            if (hasDuration()) {
                tag.putInt("dur", duration);
            }
            tag.putInt("entId", entity != null ? entity.getId() : -1);
            return tag;
        }

        public void tick() {
            if (isActive() && hasDuration()) {
                duration--;
            }
        }

        @Override
        public boolean deserialize(CompoundTag tag) {
            int id = tag.getInt("entId");
            name = new ResourceLocation(tag.getString("name"));
            if (id != -1) {
                entity = ClientHandler.handleClient(id);
                if (tag.contains("dur")) {
                    setDuration(tag.getInt("dur"));
                }
            }
            return true;
        }
    }

    @Nullable
    protected T entity;
    protected int duration;
    protected boolean hasDuration;
    protected final ResourceLocation name;

    public MKPet(ResourceLocation name, T entity) {
        this.entity = entity;
        this.name = name;
        hasDuration = false;
    }

    public void setDuration(int duration) {
        this.duration = duration;
        hasDuration = true;
    }

    public int getDuration() {
        return duration;
    }

    public boolean hasDuration() {
        return hasDuration;
    }

    public ResourceLocation getName() {
        return name;
    }

    public ClientMKPet getClientPet() {
        ClientMKPet pet = new ClientMKPet(name, entity);
        if (hasDuration()) {
            setDuration(duration);
        }
        return pet;
    }

    public void addThreat(LivingEntity source, float threat, boolean propagate) {
        if (isActive()) {
            entity.addThreat(source, threat, propagate);
        }
    }

    @Nullable
    public T getEntity() {
        return entity;
    }

    public boolean tick() {
        if (isActive() && hasDuration()) {
            duration--;
            if (duration < 0) {
                entity.remove(Entity.RemovalReason.KILLED);
                entity = null;
                return true;
            }
            return false;
        } else if (!isActive()) {
            return true;
        }
        return false;
    }

    public boolean isActive() {
        return entity != null && entity.isAlive();
    }

    static class ClientHandler {
        public static Entity handleClient(int entityId) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null)
                return null;
            return mc.level.getEntity(entityId);
        }
    }

    @Nullable
    public static <T extends LivingEntity & IMKPet> MKPet<T> makePetFromEntity(Class<T> clazz,
                                                                               ResourceLocation petSlotName,
                                                                               Entity entity) {
        if (clazz.isInstance(entity)) {
            return new MKPet<>(petSlotName, clazz.cast(entity));
        }
        return new MKPet<>(petSlotName, null);
    }
}
