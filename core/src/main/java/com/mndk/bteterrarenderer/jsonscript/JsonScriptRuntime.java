package com.mndk.bteterrarenderer.jsonscript;

import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionRunException;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;

import javax.annotation.Nullable;
import java.util.Stack;
import java.util.function.Consumer;

public class JsonScriptRuntime {
    @Nullable
    private final Consumer<JsonScriptValue> printStream;
    private final Stack<Scope> callStack = new Stack<>();

    public JsonScriptRuntime(@Nullable Consumer<JsonScriptValue> printStream) {
        this.printStream = printStream;
        this.callStack.push(new Scope("<top-level scope>", null));
    }

    public Scope pushScope(@Nullable String name, @Nullable Scope creator) {
        Scope scope = new Scope(name, creator);
        this.callStack.push(scope);
        return scope;
    }

    public void popScope() {
        this.callStack.pop();
    }

    public Scope getCurrentScope() {
        return this.callStack.peek();
    }

    public void declareVariable(String name, JsonScriptValue value) {
        this.getCurrentScope().declareVariable(name, value);
    }

    public void printValue(JsonScriptValue value) {
        if(this.printStream == null) return;
        this.printStream.accept(value);
    }

    public ExpressionRunException exception(String s) {
        StringBuilder callStackString = new StringBuilder();
        for(int i = this.callStack.size() - 1; i >= 0; i--) {
            Scope scope = this.callStack.get(i);
            String name = scope.getName();
            int lineNumber = scope.getLineNumber();

            callStackString.append("    ")
                    .append(name != null ? name : "<anonymous>")
                    .append(":")
                    .append(lineNumber);
            if(i != 0) {
                callStackString.append("\n");
            }
        }
        return new ExpressionRunException("Message: \"" + s + "\", Call stack:\n" + callStackString);
    }

}
