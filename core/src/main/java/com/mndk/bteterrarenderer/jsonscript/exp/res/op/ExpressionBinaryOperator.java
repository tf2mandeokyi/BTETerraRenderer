package com.mndk.bteterrarenderer.jsonscript.exp.res.op;

import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionRunException;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;

@FunctionalInterface
public interface ExpressionBinaryOperator {
    ExpressionResult run(JsonScriptRuntime runtime, JsonExpression left, JsonExpression right)
            throws ExpressionRunException;
}
