package com.mndk.bteterrarenderer.jsonscript;

import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class JsonScriptScope {
    @Nullable
    private final String name;
    @Nullable
    private final JsonScriptScope creator;
    private final Map<String, JsonScriptValue> variables = new HashMap<>();

    @Nullable
    public JsonScriptValue getVariableValue(String name) {
        JsonScriptScope scope = this;
        while (scope != null) {
            if (scope.variables.containsKey(name)) {
                return scope.variables.get(name);
            }
            scope = scope.getCreator();
        }
        return null;
    }

    public void assignToVariable(String name, JsonScriptValue value) {
        JsonScriptScope scope = this;
        while (scope != null) {
            if (scope.variables.containsKey(name)) {
                this.variables.put(name, value);
            }
            scope = scope.getCreator();
        }
    }

    public void declareVariable(String name, JsonScriptValue value) {
        this.variables.put(name, value);
    }
}
