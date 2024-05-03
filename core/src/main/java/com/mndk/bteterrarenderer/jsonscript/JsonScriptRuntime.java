package com.mndk.bteterrarenderer.jsonscript;

import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;

import javax.annotation.Nullable;
import java.util.Stack;
import java.util.function.Consumer;

public class JsonScriptRuntime {
    @Nullable
    private final Consumer<JsonScriptValue> printStream;
    private final Stack<JsonScriptScope> callStack = new Stack<>();

    public JsonScriptRuntime(@Nullable Consumer<JsonScriptValue> printStream) {
        this.printStream = printStream;
        this.callStack.push(new JsonScriptScope("<top-level scope>", null));
    }

    public JsonScriptScope pushScope(@Nullable String name, @Nullable JsonScriptScope creator) {
        JsonScriptScope scope = new JsonScriptScope(name, creator);
        this.callStack.push(scope);
        return scope;
    }

    public void popScope() {
        this.callStack.pop();
    }

    public JsonScriptScope getCurrentScope() {
        return this.callStack.peek();
    }

    public void printValue(JsonScriptValue value) {
        if(this.printStream == null) return;
        this.printStream.accept(value);
    }

}
