package com.mndk.bteterrarenderer.jsonscript.exp.res.op;

import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionRunException;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;

@FunctionalInterface
public interface ExpressionUnaryOperator {
    ExpressionResult run(JsonScriptRuntime runtime, JsonExpression expression) throws ExpressionRunException;
}
