package com.mndk.bteterrarenderer.jsonscript.expression.operator;

import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;

public interface JsonOperator {
    String getSymbol();
    OperatorType getType();
    int getArgumentCount();
    ExpressionResult run(JsonScriptRuntime runtime, JsonExpression... expressions);

    default ExpressionResult run(JsonScriptRuntime runtime, ExpressionCallerInfo callerInfo, JsonExpression... expressions) {
        return this.run(runtime, expressions).passedBy(callerInfo);
    }
}
