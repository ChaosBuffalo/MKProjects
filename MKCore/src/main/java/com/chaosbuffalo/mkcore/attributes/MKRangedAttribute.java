package com.chaosbuffalo.mkcore.attributes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;


public class MKRangedAttribute extends RangedAttribute {
    private boolean additionIsPercentage;
    private ResourceLocation name;
    private AttributeSyncType syncType = AttributeSyncType.None;

    public MKRangedAttribute(String name, double defaultValue, double minimumValueIn, double maximumValueIn) {
        super(name, defaultValue, minimumValueIn, maximumValueIn);
    }

    public MKRangedAttribute setAdditionIsPercentage(boolean set) {
        additionIsPercentage = set;
        return this;
    }

    public boolean displayAdditionAsPercentage() {
        return additionIsPercentage;
    }

    public MKRangedAttribute setName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    public ResourceLocation getName() {
        return name;
    }

    @Override
    public boolean isClientSyncable() {
        return super.isClientSyncable() || syncType.syncToAll();
    }

    public MKRangedAttribute setSyncType(AttributeSyncType syncType) {
        this.syncType = syncType;
        return this;
    }

    public AttributeSyncType getSyncType() {
        return syncType;
    }

    @Override
    public String toString() {
        return "MKRangedAttribute{" +
                "name=" + name +
                ", syncType=" + syncType +
                '}';
    }
}
