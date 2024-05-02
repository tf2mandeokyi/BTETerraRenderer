package com.mndk.bteterrarenderer.jsonscript.exp.res.op;

import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionRunException;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;

public interface JsonOperator {
    String getSymbol();
    OperatorType getType();
    int getArgumentCount();
    ExpressionResult run(JsonScriptRuntime runtime, JsonExpression... expressions) throws ExpressionRunException;
}
