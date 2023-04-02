package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.utils.MathUtils;

public class ColorFloatAttribute extends FloatAttribute {

    public ColorFloatAttribute(String name, float defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public void setValueFromString(String stringValue) {
        setValue(Integer.parseInt(stringValue) / 255.0f);
    }

    @Override
    public String valueAsString() {
        return Integer.toString(Math.round(value() * 255.0f));
    }

    @Override
    public boolean isEmptyStringInput(String string) {
        return string.isEmpty();
    }

    @Override
    public boolean validateString(String stringValue) {
        if (isEmptyStringInput(stringValue)) {
            return true;
        }
        if (MathUtils.isInteger(stringValue)) {
            int i = Integer.parseInt(stringValue);
            return i >= 0 && i <= 255;
        } else {
            return false;
        }
    }
}
