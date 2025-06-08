package com.chaosbuffalo.mknpc.client.gui.widgets;

import com.chaosbuffalo.mknpc.npc.NpcDefinitionClient;
import com.chaosbuffalo.mknpc.npc.NpcDefinitionManager;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKTextFieldWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NpcDefinitionList extends ScrollingList {
    private final Font font;
    private final Consumer<NpcDefinitionClient> selectCallback;
    private String searchString = "";

    public NpcDefinitionList(int x, int y, int width, int height, Font font,
                             Consumer<NpcDefinitionClient> callback) {
        super(x, y, width, height);
        this.selectCallback = callback;
        this.font = font;
        populate();
    }

    @Override
    protected void populateList(MKStackLayoutVertical layout) {
        var text = new MKTextFieldWidget(font, getX(), getY(), getWidth()-20, font.lineHeight + 2,
                Component.translatable("mknpc.ui.search"));
        text.setText(searchString);
        text.setTextChangeCallback((wid, content) -> {
            searchString = content;
            populate();
        });
        layout.addWidget(text);

        List<NpcDefinitionClient> defs = new ArrayList<>(NpcDefinitionManager.CLIENT_DEFINITIONS.values());
        if (!searchString.isEmpty()) {
            defs = defs.stream().filter(x -> x.getDefinitionName().getNamespace().startsWith(searchString)
                    || x.getDefinitionName().getPath().startsWith(searchString) || x.getName().startsWith(searchString)
                    || x.getDefinitionName().toString().startsWith(searchString))
                    .collect(Collectors.toList());
        }

        defs.sort(Comparator.comparing(NpcDefinitionClient::getName));
        for (NpcDefinitionClient clientDef : defs) {
            layout.addWidget(new NpcDefinitionEntry(
                    clientDef,
                    getWidth() - layout.getMarginLeft() - layout.getMarginRight(),
                    font,
                    selectCallback));
        }
    }
}
