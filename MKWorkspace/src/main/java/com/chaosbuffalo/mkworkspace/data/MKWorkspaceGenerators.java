package com.chaosbuffalo.mkworkspace.data;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mkworkspace.data.content.MKWorkspaceBlockStateProvider;
import com.chaosbuffalo.mkworkspace.data.content.MKWorkspaceItemModelProvider;
import com.chaosbuffalo.mkworkspace.data.content.MKWorkspaceLanguageProvider;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = MKWorkspace.MODID)
public class MKWorkspaceGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(),
                new MKWorkspaceLanguageProvider(generator.getPackOutput(), "en_us"));
        generator.addProvider(event.includeClient(),
                new MKWorkspaceBlockStateProvider(generator.getPackOutput(), helper));
        generator.addProvider(event.includeClient(),
                new MKWorkspaceItemModelProvider(generator.getPackOutput(), helper));
    }
}
