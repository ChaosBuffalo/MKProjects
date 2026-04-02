package com.chaosbuffalo.mkcore.sync.v2;

public interface ISyncNotifier {
    void notifyUpdate();


    ISyncNotifier NONE = () -> {

    };
}
