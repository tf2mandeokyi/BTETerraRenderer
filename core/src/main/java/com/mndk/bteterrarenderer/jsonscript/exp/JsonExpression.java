package com.mndk.bteterrarenderer.jsonscript.exp;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;

import javax.annotation.Nonnull;

@JsonDeserialize(using = ExpressionDeserializer.class)
public abstract class JsonExpression {
    /**
     * Only {@link JsonExpression#run(JsonScriptRuntime, ExpressionCallerInfo)} should run this method!<br>
     * Use that method instead.
     */
    @Nonnull
    protected abstract ExpressionResult runInternal(JsonScriptRuntime runtime);

    public final ExpressionResult run(JsonScriptRuntime runtime, ExpressionCallerInfo callerInfo) {
        return this.runInternal(runtime).passedBy(callerInfo);
    }
}
