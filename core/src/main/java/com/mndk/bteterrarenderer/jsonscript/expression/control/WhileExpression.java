package com.mndk.bteterrarenderer.jsonscript.expression.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonDeserialize
public class WhileExpression extends JsonExpression {

    private static final ExpressionCallerInfo INFO = new ExpressionCallerInfo(WhileExpression.class);
    private static final ExpressionCallerInfo CONDITION_INFO = INFO.add("condition");
    private static final ExpressionCallerInfo CONTENT_INFO = INFO.add("content");

    @Nullable
    private final String label;
    private final JsonExpression condition;
    private final JsonExpression content;

    @JsonCreator
    @ArrayArgumentAcceptable
    public WhileExpression(@Nullable @JsonProperty(value = "label") String label,
                           @JsonProperty(value = "condition", required = true) JsonExpression condition,
                           @JsonProperty(value = "content", required = true) JsonExpression content) {
        this.label = label;
        this.condition = condition;
        this.content = content;
    }

    @Nonnull
    @Override
    protected ExpressionResult runInternal(JsonScriptRuntime runtime) {
        while(true) {
            ResultTransformer.Bool conditionTransformer = this.condition.run(runtime, CONDITION_INFO)
                    .transformer()
                    .asJsonValue(ErrorMessages.valueMustBeJson("condition"), CONDITION_INFO)
                    .asBoolean(ErrorMessages.nodeMustBeBoolean("condition"), CONDITION_INFO);
            if(conditionTransformer.isBreakType()) return conditionTransformer.getResult();
            if(!conditionTransformer.getWrapped()) break;

            ExpressionResult contentResult = this.content.run(runtime, CONTENT_INFO);
            if(contentResult.isLoopBreak(this.label)) break;
            else if(!contentResult.isLoopContinue(this.label) && contentResult.isBreakType()) {
                return contentResult;
            }
        }

        return ExpressionResult.ok();
    }
}
