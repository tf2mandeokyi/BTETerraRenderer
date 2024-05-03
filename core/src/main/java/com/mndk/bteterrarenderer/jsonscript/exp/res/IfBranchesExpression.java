package com.mndk.bteterrarenderer.jsonscript.exp.res;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.util.JsonArrayTuple;
import com.mndk.bteterrarenderer.jsonscript.util.JsonParserReader;
import com.mndk.bteterrarenderer.jsonscript.util.JsonParserReaderDeserializer;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptJsonValue;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;
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

            ExpressionResult result = branch.getCondition().run(runtime, info.add("condition"));
            if(result.isBreakType()) return result;

            JsonScriptValue conditionValue = result.getValue();
            if(!(conditionValue instanceof JsonScriptJsonValue)) {
                return ExpressionResult.error("condition value must be a boolean type", info.add("condition"));
            }

            JsonNode condition = ((JsonScriptJsonValue) conditionValue).getNode();
            if(!condition.isBoolean()) {
                return ExpressionResult.error("condition value must be a boolean type", info.add("condition"));
            }
            if(condition.asBoolean()) {
                return branch.getValue().run(runtime, info.add("value"));
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
