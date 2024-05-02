package com.mndk.bteterrarenderer.jsonscript.exp;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;

import javax.annotation.Nonnull;

@JsonDeserialize(using = ExpressionDeserializer.class)
public interface JsonExpression {
    @Nonnull
    ExpressionResult run(JsonScriptRuntime runtime) throws ExpressionRunException;
}
