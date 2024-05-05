package com.mndk.bteterrarenderer.jsonscript.expression.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.ArrayArgumentAcceptable;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonDeserialize
public class BreakLoopExpression extends JsonExpression {

    @Nullable
    private final String label;

    @JsonCreator
    @ArrayArgumentAcceptable
    public BreakLoopExpression(@Nullable @JsonProperty(value = "label") String label) {
        this.label = label;
    }

    @Nonnull
    @Override
    protected ExpressionResult runInternal(JsonScriptRuntime runtime) {
        return ExpressionResult.breakLoop(this.label);
    }
}
