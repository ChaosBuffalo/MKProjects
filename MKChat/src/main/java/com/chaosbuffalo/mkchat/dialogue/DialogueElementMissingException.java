package com.chaosbuffalo.mkchat.dialogue;

public class DialogueElementMissingException extends IllegalArgumentException {

    public DialogueElementMissingException() {
        super();
    }

    public DialogueElementMissingException(String message) {
        super(message);
    }

    public DialogueElementMissingException(String message, Object... args) {
        super(String.format(message, args));
    }
}
