package com.mndk.bteterrarenderer.jsonscript.expression;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptJsonValue;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ResultTransformer<T> {

    private final ExpressionResult result;
    private final T wrapped;

    public boolean isBreakType() {
        return this.result.isBreakType();
    }

    protected <U, V extends ResultTransformer<U>> V then(Function<T, Boolean> validator, Function<T, U> function,
                                                         BiFunction<ExpressionResult, U, V> constructor,
                                                         String errorMessage, ExpressionCallerInfo info) {
        if(this.isBreakType()) {
            return constructor.apply(this.result, null);
        }

        if(!validator.apply(this.wrapped)) {
            return constructor.apply(ExpressionResult.error(errorMessage, info), null);
        }

        U result = function.apply(this.wrapped);
        return constructor.apply(this.result, result);
    }

    public static Exp of(ExpressionResult result) {
        return new Exp(result, result);
    }

    public static class Exp extends ResultTransformer<ExpressionResult> {
        private Exp(ExpressionResult result, @Nonnull ExpressionResult value) {
            super(result, value);
        }

        public Val asValue() {
            return this.then(t -> true, ExpressionResult::getValue, Val::new, "", null);
        }

        public JVal asJsonValue(String errorMessage, ExpressionCallerInfo info) {
            return this.asValue().asJsonValue(errorMessage, info);
        }
    }

    public static class Val extends ResultTransformer<JsonScriptValue> {
        private Val(ExpressionResult result, JsonScriptValue value) {
            super(result, value);
        }

        public JVal asJsonValue(String errorMessage, ExpressionCallerInfo info) {
            return this.then(t -> t instanceof JsonScriptJsonValue, t -> (JsonScriptJsonValue) t, JVal::new, errorMessage, info);
        }
    }

    public static class JVal extends ResultTransformer<JsonScriptJsonValue> {
        private JVal(ExpressionResult result, JsonScriptJsonValue value) {
            super(result, value);
        }

        public JNode asNode() {
            return this.then(t -> true, JsonScriptJsonValue::getNode, JNode::new, "", null);
        }
    }

    public static class JNode extends ResultTransformer<JsonNode> {
        private JNode(ExpressionResult result, JsonNode value) {
            super(result, value);
        }

        public Bool asBoolean(String errorMessage, ExpressionCallerInfo info) {
            return this.then(JsonNode::isBoolean, JsonNode::asBoolean, Bool::new, errorMessage, info);
        }

        public Str asText(String errorMessage, ExpressionCallerInfo info) {
            return this.then(JsonNode::isTextual, JsonNode::asText, Str::new, errorMessage, info);
        }

        public Str asCastedText(String errorMessage, ExpressionCallerInfo info) {
            return this.then(
                    t -> t.isTextual() || t.isNumber() || t.isBoolean() || t.isNull(),
                    t -> t.isTextual() ? t.asText() : t.toString(),
                    Str::new, errorMessage, info);
        }

        public JNode checkIfArray(String errorMessage, ExpressionCallerInfo info) {
            return this.then(JsonNode::isArray, t -> t, JNode::new, errorMessage, info);
        }

        public JVal asValue() {
            return this.then(t -> true, JsonScriptValue::json, JVal::new, "", null);
        }

        public Exp asExpressionResult() {
            return this.then(t -> true, ExpressionResult::ok, Exp::new, "", null);
        }
    }

    public static class Bool extends ResultTransformer<Boolean> {
        private Bool(ExpressionResult result, boolean value) {
            super(result, value);
        }

        public JNode asNode() {
            return this.then(t -> true, BooleanNode::valueOf, JNode::new, "", null);
        }
    }

    public static class Str extends ResultTransformer<String> {
        private Str(ExpressionResult result, String value) {
            super(result, value);
        }
    }

}
