package com.chaosbuffalo.mkcore.sync;

public interface ISyncNotifier {
    void notifyUpdate(ISyncObject syncObject);


    ISyncNotifier NONE = syncObject -> {

    };
}
