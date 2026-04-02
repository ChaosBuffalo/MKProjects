package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.serialization.ISerializableAttributeContainer;
import com.chaosbuffalo.mkcore.serialization.attributes.IGuiDisplayAttribute;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.OffsetConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import net.minecraft.client.gui.Font;

import java.util.function.Consumer;

public class SerializableAttributeContainerPanel extends MKStackLayoutVertical {

    public SerializableAttributeContainerPanel(int x, int y, int width, ISerializableAttributeContainer container,
                                               Font font, Consumer<IGuiDisplayAttribute> callback) {
        super(x, y, width);
        setMargins(1, 1, 4, 4);
        setPaddings(0, 0, 1, 1);
        for (var serAttr : container.getAttributesAsMap().entrySet()) {
            if (serAttr.getValue() instanceof IGuiDisplayAttribute attr) {
                SerializableAttributeEntry entry = new SerializableAttributeEntry(x, y, serAttr.getKey(), attr, font);
                entry.setCallback(callback);
                addWidget(entry);
                addConstraintToWidget(new OffsetConstraint(10, 0, true, false), entry);
            }
        }
    }
}
