package com.mndk.bteterrarenderer.jsonscript.exp.res.op;

import com.fasterxml.jackson.databind.JsonNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionRunException;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;

@FunctionalInterface
public interface NodeUnaryOperator extends ExpressionUnaryOperator {
    JsonNode run(JsonScriptRuntime runtime, JsonNode value) throws ExpressionRunException;

    @Override
    default ExpressionResult run(JsonScriptRuntime runtime, JsonExpression expression)
            throws ExpressionRunException
    {
        ExpressionResult result = expression.run(runtime);
        if(result.isBreakType()) return result;

        JsonNode value = result.getValue().getAsJsonValue();
        return ExpressionResult.ok(this.run(runtime, value));
    }
}
