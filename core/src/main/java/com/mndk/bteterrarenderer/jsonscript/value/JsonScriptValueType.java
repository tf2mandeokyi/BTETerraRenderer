package com.mndk.bteterrarenderer.jsonscript.value;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum JsonScriptValueType {
    NULL("null"),
    BOOLEAN("boolean"),
    INT("int"),
    FLOAT("float"),
    STRING("string"),
    ARRAY("array"),
    OBJECT("object"),
    FUNCTION("function");

    private final String value;

    @Override
    public String toString() {
        return this.value;
    }
}
