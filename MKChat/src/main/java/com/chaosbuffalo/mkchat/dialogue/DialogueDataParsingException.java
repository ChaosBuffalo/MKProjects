package com.chaosbuffalo.mkchat.dialogue;

public class DialogueDataParsingException extends IllegalArgumentException {

    public DialogueDataParsingException() {
        super();
    }

    public DialogueDataParsingException(String message) {
        super(message);
    }

    public DialogueDataParsingException(String message, Object... args) {
        super(String.format(message, args));
    }
}
