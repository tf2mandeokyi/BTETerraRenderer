package com.mndk.bteterrarenderer.jsonscript.expression.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.ResultTransformer;
import com.mndk.bteterrarenderer.jsonscript.util.JsonArrayTuple;
import com.mndk.bteterrarenderer.jsonscript.util.JsonParserReader;
import com.mndk.bteterrarenderer.jsonscript.util.JsonParserReaderDeserializer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.List;

@JsonDeserialize
public class IfBranchesExpression extends JsonExpression {

    private final List<BranchTuple> branches;

    @JsonCreator
    public IfBranchesExpression(List<BranchTuple> branches) {
        this.branches = branches;
    }

    @Nonnull
    @Override
    public ExpressionResult runInternal(JsonScriptRuntime runtime) {
        int i = 0;
        for(BranchTuple branch : this.branches) {
            ExpressionCallerInfo info = new ExpressionCallerInfo(this, "branch #" + i);
            ExpressionCallerInfo conditionInfo = info.add("condition");

            ResultTransformer.Bool conditionTransformer = branch.getCondition().run(runtime, info.add("condition"))
                    .transformer()
                    .asJsonValue("condition value must be a boolean type", conditionInfo)
                    .asNode()
                    .asBoolean("condition value must be a boolean type", conditionInfo);
            if(conditionTransformer.isBreakType()) return conditionTransformer.getResult();

            if(conditionTransformer.getWrapped()) {
                ExpressionCallerInfo valueInfo = info.add("value");
                return branch.getValue().run(runtime, valueInfo);
            }
        }
        return ExpressionResult.ok();
    }

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @JsonDeserialize(using = TupleDeserializer.class)
    public static class BranchTuple {
        private final JsonExpression condition;
        private final JsonExpression value;
    }

    private static class TupleDeserializer extends JsonParserReaderDeserializer<BranchTuple> {
        private static final JsonParserReader<BranchTuple> READER = JsonArrayTuple.parserReaderBuilder()
                .next(JsonExpression.class)
                .next(JsonExpression.class)
                .build()
                .then(arrayTuple -> BranchTuple.builder()
                        .condition(arrayTuple.get(0))
                        .value(arrayTuple.get(1))
                        .build()
                );

        public TupleDeserializer() {
            super(READER);
        }
    }
}
