package com.chaosbuffalo.mknpc.client.gui.widgets;

import com.chaosbuffalo.mknpc.npc.NpcDefinitionClient;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import net.minecraft.client.gui.Font;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class NpcDefinitionList extends ScrollingList {
    private final Font font;
    private final Consumer<NpcDefinitionClient> selectCallback;

    public NpcDefinitionList(int x, int y, int width, int height, Font font,
                             Consumer<NpcDefinitionClient> callback) {
        super(x, y, width, height);
        this.selectCallback = callback;
        this.font = font;
        populate();
    }

    @Override
    protected void populateList(MKStackLayoutVertical layout){
        List<NpcDefinitionClient> defs = new ArrayList<>(NpcDefinitionManager.CLIENT_DEFINITIONS.values());
        defs.sort(Comparator.comparing(NpcDefinitionClient::getName));
        for (NpcDefinitionClient clientDef : defs){
            layout.addWidget(new NpcDefinitionEntry(
                    clientDef,
                    getWidth() - layout.getMarginLeft() - layout.getMarginRight(),
                    font,
                    selectCallback));
        }
    }
}
