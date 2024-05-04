package com.mndk.bteterrarenderer.jsonscript.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptScope;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.parameter.JsonParameters;

import java.util.HashMap;
import java.util.Map;

public class JsonScriptFunctionValue implements JsonScriptValue {

    private final String name;
    private final JsonParameters parameters;
    private final JsonExpression expression;
    private final JsonScriptScope definedAt;
    private final ExpressionCallerInfo parameterCallerInfo, expressionCallerInfo;

    public JsonScriptFunctionValue(String name, JsonParameters parameters, JsonExpression expression,
                                   JsonScriptScope definedAt) {
        this.name = name;
        this.parameters = parameters;
        this.expression = expression;
        this.definedAt = definedAt;

        ExpressionCallerInfo info = new ExpressionCallerInfo(name);
        this.parameterCallerInfo = info.add("parameter");
        this.expressionCallerInfo = info.add("content");
    }

    public ExpressionResult run(JsonScriptRuntime runtime, JsonNode argument, ExpressionCallerInfo callerInfo) {
        return this.run(runtime, argument).passedBy(callerInfo);
    }

    private ExpressionResult run(JsonScriptRuntime runtime, JsonNode argument) {
        Map<String, JsonScriptValue> arguments = new HashMap<>();

        ExpressionResult result = this.parameters.evaluate(runtime, argument, arguments, this.parameterCallerInfo);
        if(result.isBreakType()) return result;

        try {
            JsonScriptScope functionScope = runtime.pushScope(this.name, this.definedAt);
            arguments.forEach(functionScope::declareVariable);

            result = this.expression.run(runtime, this.expressionCallerInfo);
            if (result.isReturn()) {
                return ExpressionResult.ok(result.getValue());
            }
            else if (result.isLoopBreak() || result.isLoopContinue()) {
                return ExpressionResult.error("cannot break or continue outside a function", this.expressionCallerInfo);
            }
            return result;
        }
        finally {
            runtime.popScope();
        }
    }
}
