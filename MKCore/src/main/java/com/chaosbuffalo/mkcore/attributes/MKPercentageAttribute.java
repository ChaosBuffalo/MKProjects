package com.chaosbuffalo.mkcore.attributes;

import net.neoforged.neoforge.common.PercentageAttribute;

public class MKPercentageAttribute extends PercentageAttribute implements IMKAttribute {
    private AttributeSyncType syncType = AttributeSyncType.None;

    public MKPercentageAttribute(String pDescriptionId, double pDefaultValue, double pMin, double pMax, double scaleFactor) {
        super(pDescriptionId, pDefaultValue, pMin, pMax, scaleFactor);
    }

    public MKPercentageAttribute(String pDescriptionId, double pDefaultValue, double pMin, double pMax) {
        super(pDescriptionId, pDefaultValue, pMin, pMax);
    }

    @Override
    public boolean isClientSyncable() {
        return super.isClientSyncable() || syncType.syncToAll();
    }

    public MKPercentageAttribute setSyncType(AttributeSyncType syncType) {
        this.syncType = syncType;
        return this;
    }

    @Override
    public AttributeSyncType getSyncType() {
        return syncType;
    }

    @Override
    public String toString() {
        return "MKPercentageAttribute{" +
                "name=" + getDescriptionId() +
                ", syncType=" + syncType +
                ", scaleFactor=" + scaleFactor +
                '}';
    }
}
