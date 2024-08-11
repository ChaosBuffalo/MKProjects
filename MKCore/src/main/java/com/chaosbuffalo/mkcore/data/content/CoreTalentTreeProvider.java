package com.chaosbuffalo.mkcore.data.content;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.talents.TalentLineDefinition;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
import com.chaosbuffalo.mkcore.core.talents.nodes.AttributeTalentNode;
import com.chaosbuffalo.mkcore.data.providers.TalentTreeProvider;
import com.chaosbuffalo.mkcore.init.CoreTalents;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;

import java.util.concurrent.CompletableFuture;

public class CoreTalentTreeProvider extends TalentTreeProvider {

    public CoreTalentTreeProvider(DataGenerator generator) {
        super(generator, MKCore.MOD_ID);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        TalentTreeDefinition test = new TalentTreeDefinition(MKCore.makeRL("knight"));
        test.setVersion(1);
        TalentLineDefinition line = new TalentLineDefinition(test, "knight_1");
        line.addNode(new AttributeTalentNode(CoreTalents.MAX_HEALTH_TALENT, 1, 1.0));
        test.addLine(line);
        return writeDefinition(test, pOutput);
    }
}
