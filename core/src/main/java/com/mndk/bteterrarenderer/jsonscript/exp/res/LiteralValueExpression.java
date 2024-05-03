package com.mndk.bteterrarenderer.jsonscript.exp.res;

import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class LiteralValueExpression extends JsonExpression {

    private final JsonScriptValue value;

    @Nonnull
    @Override
    protected ExpressionResult runInternal(JsonScriptRuntime runtime) {
        return ExpressionResult.ok(value);
    }
}
