package com.mndk.bteterrarenderer.jsonscript.exp.res;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.Scope;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionRunException;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;

import javax.annotation.Nonnull;
import java.util.List;

@JsonDeserialize
public class ClosureExpression implements JsonExpression {

    private final List<JsonExpression> parameters;

    @JsonCreator
    public ClosureExpression(List<JsonExpression> parameters) {
        this.parameters = parameters;
    }

    @Nonnull
    @Override
    public ExpressionResult run(JsonScriptRuntime runtime) throws ExpressionRunException {
        try {
            Scope closureScope = runtime.pushScope(null, runtime.getCurrentScope());
            ExpressionResult result = ExpressionResult.ok();

            for (JsonExpression expression : this.parameters) {
                result = expression.run(runtime);
                if(result.isBreakType()) break;
                closureScope.nextLineNumber();
            }
            return result;
        }
        finally {
            runtime.popScope();
        }
    }
}
