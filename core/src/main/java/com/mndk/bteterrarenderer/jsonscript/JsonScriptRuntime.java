package com.mndk.bteterrarenderer.jsonscript;

import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Stack;
import java.util.function.Consumer;

public class JsonScriptRuntime {
    @Nullable
    private final Consumer<JsonScriptValue> printStream;
    private final Stack<JsonScriptScope> scopeStack = new Stack<>();

    public JsonScriptRuntime(@Nullable Consumer<JsonScriptValue> printStream) {
        this.printStream = printStream;
        this.scopeStack.push(new JsonScriptScope(null));
    }

    public JsonScriptScope pushScope() {
        JsonScriptScope scope = new JsonScriptScope(this.getCurrentScope());
        this.scopeStack.push(scope);
        return scope;
    }

    public JsonScriptScope pushScope(@Nonnull JsonScriptScope creator) {
        JsonScriptScope scope = new JsonScriptScope(creator);
        this.scopeStack.push(scope);
        return scope;
    }

    public void popScope() {
        this.scopeStack.pop();
    }

    public JsonScriptScope getCurrentScope() {
        return this.scopeStack.peek();
    }

    public void printValue(JsonScriptValue value) {
        if(this.printStream == null) return;
        this.printStream.accept(value);
    }

}
