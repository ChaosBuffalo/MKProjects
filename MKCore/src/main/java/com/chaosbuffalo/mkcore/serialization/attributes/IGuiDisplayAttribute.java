package com.chaosbuffalo.mkcore.serialization.attributes;

public interface IGuiDisplayAttribute {

    void reset();

    void setValueFromString(String stringValue);

    boolean validateString(String stringValue);

    boolean isEmptyStringInput(String string);

    String valueAsString();
}
