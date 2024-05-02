package com.mndk.bteterrarenderer.jsonscript;

import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Scope {
    @Nullable
    private final String name;
    @Nullable
    private final Scope creator;
    private final Map<String, JsonScriptValue> variables = new HashMap<>();
    private int lineNumber = 1;

    public Optional<JsonScriptValue> getVariableValue(String name) {
        Scope scope = this;
        while(scope != null) {
            if(scope.variables.containsKey(name)) {
                return Optional.of(scope.variables.get(name));
            }
            scope = scope.getCreator();
        }
        return Optional.empty();
    }

    public boolean changeVariableValue(String name, JsonScriptValue value) {
        Scope scope = this;
        while(scope != null) {
            if(scope.variables.containsKey(name)) {
                this.variables.put(name, value);
                return true;
            }
            scope = scope.getCreator();
        }
        return false;
    }

    public void declareVariable(String name, JsonScriptValue value) {
        this.variables.put(name, value);
    }

    public void nextLineNumber() {
        this.lineNumber++;
    }
}
