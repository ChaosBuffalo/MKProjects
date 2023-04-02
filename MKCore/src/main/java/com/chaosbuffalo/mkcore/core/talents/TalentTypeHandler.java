package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.records.IRecordTypeHandler;

public abstract class TalentTypeHandler implements IRecordTypeHandler<TalentRecord> {

    protected final MKPlayerData playerData;

    public TalentTypeHandler(MKPlayerData playerData) {
        this.playerData = playerData;
    }

}
