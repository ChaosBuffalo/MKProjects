package com.chaosbuffalo.mkcore.sync.v1;

public interface ISyncNotifierV1 {
    void notifyUpdate(ISyncObjectV1 syncObject);


    ISyncNotifierV1 NONE = syncObject -> {

    };
}
