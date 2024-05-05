package com.mndk.bteterrarenderer.jsonscript.expression.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonDeserialize
public class ForLoopExpression extends JsonExpression {

    private static final ExpressionCallerInfo INFO = new ExpressionCallerInfo(ForLoopExpression.class);
    private static final ExpressionCallerInfo INITIAL_INFO = INFO.add("initial statement");
    private static final ExpressionCallerInfo CONDITION_INFO = INFO.add("condition statement");
    private static final ExpressionCallerInfo INCREMENT_INFO = INFO.add("increment statement");

    @Nullable
    private final String label;
    private final JsonExpression initial, condition, increment, content;

    @JsonCreator
    @ArrayArgumentAcceptable
    public ForLoopExpression(@Nullable @JsonProperty(value = "label") String label,
                             @JsonProperty(value = "initial", required = true) JsonExpression initial,
                             @JsonProperty(value = "condition", required = true) JsonExpression condition,
                             @JsonProperty(value = "increment", required = true) JsonExpression increment,
                             @JsonProperty(value = "content", required = true) JsonExpression content) {
        this.label = label;
        this.initial = initial;
        this.condition = condition;
        this.increment = increment;
        this.content = content;
    }

    @Nonnull
    @Override
    protected ExpressionResult runInternal(JsonScriptRuntime runtime) {
        try {
            runtime.pushScope();

            ExpressionResult result = this.initial.run(runtime, INITIAL_INFO);
            if(result.isBreakType()) return result;

            while(true) {
                ResultTransformer.Bool conditionTransformer = this.condition.run(runtime, CONDITION_INFO)
                        .transformer()
                        .asJsonValue(ErrorMessages.valueMustBeJson("condition"), CONDITION_INFO)
                        .asBoolean(ErrorMessages.nodeMustBeBoolean("condition"), CONDITION_INFO);
                if(conditionTransformer.isBreakType()) return conditionTransformer.getResult();
                if(!conditionTransformer.getWrapped()) break;

                result = this.content.run(runtime, INFO);
                if(result.isLoopBreak(this.label)) break;
                else if(!result.isLoopContinue(this.label) && result.isBreakType()) {
                    return result;
                }

                result = this.increment.run(runtime, INCREMENT_INFO);
                if(result.isBreakType()) return result;
            }

            return ExpressionResult.ok();
        }
        finally {
            runtime.popScope();
        }
    }
}
