package com.chaosbuffalo.mkcore.compat;

import com.chaosbuffalo.mkcore.compat.iaf.IAFHooks;
import net.neoforged.fml.ModList;

public class CoreCompatHooks {


    public static void registerCompat() {
        if (ModList.get().isLoaded("iceandfire")) {
            IAFHooks.add();
        }
    }
}
