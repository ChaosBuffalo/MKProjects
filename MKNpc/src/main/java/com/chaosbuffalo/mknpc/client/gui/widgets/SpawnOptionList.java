package com.chaosbuffalo.mknpc.client.gui.widgets;

import com.chaosbuffalo.mknpc.spawn.SpawnList;
import com.chaosbuffalo.mknpc.spawn.SpawnOption;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public class SpawnOptionList extends ScrollingList {
    private final Font font;
    private final SpawnList spawnList;

    public SpawnOptionList(int x, int y, int width, int height, Font font,
                           SpawnList spawnList) {
        super(x, y, width, height);
        this.font = font;
        this.spawnList = spawnList;
        getStackLayout().setPaddingBot(1);
        getStackLayout().setPaddingTop(1);
        populate();
    }

    @Override
    public void preDraw(PoseStack stack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        mkFill(stack, x, y, x + width, y + height, 0x55aaaaaa);
    }

    @Override
    protected void populateList(MKStackLayoutVertical layout) {
        for (SpawnOption option : spawnList.getOptions()){
            layout.addWidget(new SpawnOptionEntry(0, 0, 20, option, font,
                    (spawnOption) -> {
                        spawnList.getOptions().remove(spawnOption);
                        populate();
                    }
            ));
        }
    }
}
