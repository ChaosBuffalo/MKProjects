package com.chaosbuffalo.mkcore.core.talents;

import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.records.IRecordTypeHandler;

public abstract class TalentTypeHandler implements IRecordTypeHandler<TalentRecord> {

    protected final Persona persona;

    public TalentTypeHandler(Persona persona) {
        this.persona = persona;
    }

}
