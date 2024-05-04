package com.mndk.bteterrarenderer.jsonscript.expression.operator;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.mndk.bteterrarenderer.core.util.json.JsonParserUtil;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.expression.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.expression.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.ResultTransformer;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptJsonValue;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

@RequiredArgsConstructor
public enum JsonUnaryOperator implements JsonOperator {
    POSITIVE("+", bd -> bd          , bi -> bi          ),
    NEGATIVE("-", BigDecimal::negate, BigInteger::negate),
    BITWISE_NOT("~", BigInteger::not),
    LOGICAL_NOT("not", JsonUnaryOperator::notOperation);

    private static final String OPERAND = "operand";
    private static final ExpressionCallerInfo LOGICAL_NOT_OPERAND = LOGICAL_NOT.info.add(OPERAND);

    @Getter(onMethod_ = @JsonValue)
    private final String symbol;
    private final ExpressionCallerInfo info;
    @Nonnull
    private ExpressionUnaryOperator instance;

    JsonUnaryOperator(String symbol) {
        this.symbol = symbol;
        this.info = new ExpressionCallerInfo("unary operator", symbol);
    }

    JsonUnaryOperator(String symbol, NodeUnaryOperator instance) {
        this(symbol);

        ExpressionCallerInfo operandInfo = this.info.add(OPERAND);
        this.instance = new EuoNodeImpl(operandInfo, instance);
    }

    JsonUnaryOperator(String symbol, Function<BigDecimal, Object> bd, Function<BigInteger, Object> bi) {
        this(symbol);

        NodeUnaryOperator nuo = (runtime, value) -> {
            if (!value.isNumber()) {
                return ExpressionResult.error("Cannot apply unary operator '" + this.getSymbol() + "' " +
                        "to value of type " + value.getNodeType(), this.info);
            }

            JsonNode number = JsonParserUtil.toBiggerPrimitiveNode(value);
            Object result;
            if(number instanceof DecimalNode) result = bd.apply(number.decimalValue());
            else result = bi.apply(number.bigIntegerValue());
            return ExpressionResult.ok(JsonParserUtil.primitiveToBigNode(result));
        };

        ExpressionCallerInfo operandInfo = this.info.add(OPERAND);
        this.instance = new EuoNodeImpl(operandInfo, nuo);
    }

    JsonUnaryOperator(String symbol, Function<BigInteger, Object> bi) {
        this(symbol, value -> bi.apply(value.toBigInteger()), bi);
    }

    @Override
    public OperatorType getType() {
        return OperatorType.UNARY;
    }

    @Override
    public int getArgumentCount() {
        return 1;
    }

    @Override
    public ExpressionResult run(JsonScriptRuntime runtime, JsonExpression... expressions) {
        return this.instance.run(runtime, expressions[0]);
    }

    @Override
    public String toString() {
        return this.symbol;
    }

    @Nullable
    public static JsonUnaryOperator fromSymbol(String symbol) {
        for(JsonUnaryOperator operator : values()) {
            if(operator.symbol.equals(symbol)) return operator;
        }
        return null;
    }

    private static ExpressionResult notOperation(JsonScriptRuntime runtime, JsonNode value) {
        if(!value.isBoolean()) {
            return ExpressionResult.error("left operand must be a boolean type, " +
                    "instead " + value.getNodeType() + " was given", LOGICAL_NOT_OPERAND);
        }
        return ExpressionResult.ok(BooleanNode.valueOf(!value.booleanValue()));
    }

    @FunctionalInterface
    private interface ExpressionUnaryOperator {
        ExpressionResult run(JsonScriptRuntime runtime, JsonExpression expression);
    }

    @FunctionalInterface
    private interface NodeUnaryOperator {
        ExpressionResult run(JsonScriptRuntime runtime, JsonNode value);
    }

    @RequiredArgsConstructor
    private static class EuoNodeImpl implements ExpressionUnaryOperator {

        private final ExpressionCallerInfo operandInfo;
        private final NodeUnaryOperator instance;

        @Override
        public ExpressionResult run(JsonScriptRuntime runtime, JsonExpression expression) {
            ResultTransformer.JNode transformer = expression.run(runtime, this.operandInfo)
                    .transformer()
                    .asJsonValue("value must be a json type", this.operandInfo)
                    .asNode();
            if(transformer.isBreakType()) return transformer.getResult();

            JsonNode node = transformer.getWrapped();
            return instance.run(runtime, node);
        }
    }
}
