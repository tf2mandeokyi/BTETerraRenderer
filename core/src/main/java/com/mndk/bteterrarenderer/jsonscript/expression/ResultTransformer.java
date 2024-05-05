package com.mndk.bteterrarenderer.jsonscript.expression;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptJsonValue;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValueType;
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
                                                         Function<T, String> message, ExpressionCallerInfo info) {
        if(this.isBreakType()) {
            return constructor.apply(this.result, null);
        }

        if(!validator.apply(this.wrapped)) {
            return constructor.apply(ExpressionResult.error(message.apply(this.wrapped), info), null);
        }

        U result = function.apply(this.wrapped);
        return constructor.apply(this.result, result);
    }

    public static Exp of(ExpressionResult result) {
        return new Exp(result, result);
    }

    public static Val of(JsonScriptValue value) {
        return new Val(ExpressionResult.ok(), value);
    }

    public static class Exp extends ResultTransformer<ExpressionResult> {
        private Exp(ExpressionResult result, @Nonnull ExpressionResult value) {
            super(result, value);
        }

        public Val asValue() {
            return this.then(t -> true, ExpressionResult::getValue, Val::new, t -> "", null);
        }

        public JVal asJsonValue(Function<JsonScriptValueType, String> message, ExpressionCallerInfo info) {
            return this.asValue().asJsonValue(message, info);
        }
    }

    public static class Val extends ResultTransformer<JsonScriptValue> {
        private Val(ExpressionResult result, JsonScriptValue value) {
            super(result, value);
        }

        public JVal asJsonValue(Function<JsonScriptValueType, String> message, ExpressionCallerInfo info) {
            return this.then(t -> t instanceof JsonScriptJsonValue, t -> (JsonScriptJsonValue) t, JVal::new,
                    val -> message.apply(val.getType()), info);
        }
    }

    public static class JVal extends ResultTransformer<JsonScriptJsonValue> {
        private JVal(ExpressionResult result, JsonScriptJsonValue value) {
            super(result, value);
        }

        public JNode asNode() {
            return this.then(t -> true, JsonScriptJsonValue::getNode, JNode::new, t -> "", null);
        }

        public Bool asBoolean(Function<JsonNodeType, String> message, ExpressionCallerInfo info) {
            return this.asNode().asBoolean(message, info);
        }

        public JArr asArrayNode(Function<JsonNodeType, String> message, ExpressionCallerInfo info) {
            return this.asNode().asArray(message, info);
        }
    }

    public static class JNode extends ResultTransformer<JsonNode> {
        private JNode(ExpressionResult result, JsonNode value) {
            super(result, value);
        }

        public Bool asBoolean(Function<JsonNodeType, String> message, ExpressionCallerInfo info) {
            return this.then(JsonNode::isBoolean, JsonNode::asBoolean, Bool::new, node -> message.apply(node.getNodeType()), info);
        }

        public Str asText(Function<JsonNodeType, String> message, ExpressionCallerInfo info) {
            return this.then(JsonNode::isTextual, JsonNode::asText, Str::new, node -> message.apply(node.getNodeType()), info);
        }

        public Str asCastedText(Function<JsonNodeType, String> message, ExpressionCallerInfo info) {
            return this.then(
                    t -> t.isTextual() || t.isNumber() || t.isBoolean() || t.isNull(),
                    t -> t.isTextual() ? t.asText() : t.toString(),
                    Str::new, node -> message.apply(node.getNodeType()), info);
        }

        public JNode checkIfArray(Function<JsonNodeType, String> message, ExpressionCallerInfo info) {
            return this.then(JsonNode::isArray, t -> t, JNode::new, node -> message.apply(node.getNodeType()), info);
        }

        public JArr asArray(Function<JsonNodeType, String> message, ExpressionCallerInfo info) {
            return this.then(JsonNode::isArray, t -> (ArrayNode) t, JArr::new, node -> message.apply(node.getNodeType()), info);
        }

        public JVal asValue() {
            return this.then(t -> true, JsonScriptValue::json, JVal::new, t -> "", null);
        }

        public Exp asExpressionResult() {
            return this.then(t -> true, ExpressionResult::ok, Exp::new, t -> "", null);
        }
    }

    public static class Bool extends ResultTransformer<Boolean> {
        private Bool(ExpressionResult result, boolean value) {
            super(result, value);
        }

        public JNode asNode() {
            return this.then(t -> true, BooleanNode::valueOf, JNode::new, t -> "", null);
        }
    }

    public static class Str extends ResultTransformer<String> {
        private Str(ExpressionResult result, String value) {
            super(result, value);
        }
    }

    public static class JArr extends ResultTransformer<ArrayNode> {
        private JArr(ExpressionResult result, ArrayNode value) {
            super(result, value);
        }
    }

}
