package com.mndk.bteterrarenderer.jsonscript.exp.res.op;

import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;

public interface JsonOperator {
    String getSymbol();
    OperatorType getType();
    int getArgumentCount();
    ExpressionResult run(JsonScriptRuntime runtime, JsonExpression... expressions);

    default ExpressionResult run(JsonScriptRuntime runtime, ExpressionCallerInfo callerInfo, JsonExpression... expressions) {
        return this.run(runtime, expressions).passedBy(callerInfo);
    }
}
