package com.mndk.bteterrarenderer.jsonscript.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.Scope;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionRunException;
import com.mndk.bteterrarenderer.jsonscript.exp.func.JsonParameters;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class JsonScriptFunctionValue implements JsonScriptValue {

    private final String name;
    private final JsonParameters parameters;
    private final JsonExpression expression;
    private final Scope definedAt;

    public ExpressionResult run(JsonScriptRuntime runtime, JsonNode argument) throws ExpressionRunException {
        Map<String, JsonScriptValue> arguments = new HashMap<>();

        ExpressionResult result = this.parameters.evaluate(runtime, argument, arguments);
        if(result.isBreakType()) return result;

        try {
            Scope functionScope = runtime.pushScope(this.name, this.definedAt);
            arguments.forEach(functionScope::declareVariable);

            result = this.expression.run(runtime);
            if (result.isReturn()) {
                return ExpressionResult.ok(result.getValue());
            }
            else if (result.isBreakType()) {
                throw runtime.exception("cannot break or continue outside a function");
            }
            return result;
        }
        finally {
            runtime.popScope();
        }
    }

    public Type getType() {
        return Type.FUNCTION;
    }

    @Override
    public JsonNode getAsJsonValue() throws ExpressionRunException {
        throw new ExpressionRunException("cannot convert function to json");
    }

    @Override
    public JsonScriptFunctionValue getAsFunction() {
        return this;
    }
}
