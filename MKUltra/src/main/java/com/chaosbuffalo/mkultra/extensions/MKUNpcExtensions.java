package com.chaosbuffalo.mkultra.extensions;


import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.IMKNpcExtension;
import net.minecraftforge.fml.InterModComms;

public class MKUNpcExtensions implements IMKNpcExtension {

    public static void sendExtension() {
        InterModComms.sendTo(MKNpc.MODID, MKNpc.REGISTER_NPC_OPTIONS_EXTENSION,
                MKUNpcExtensions::new);
    }


    @Override
    public void registerNpcExtension() {

    }
}
