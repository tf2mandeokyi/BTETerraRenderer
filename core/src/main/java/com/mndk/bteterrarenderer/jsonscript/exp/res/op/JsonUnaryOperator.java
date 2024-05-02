package com.mndk.bteterrarenderer.jsonscript.exp.res.op;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionRunException;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

@RequiredArgsConstructor
public enum JsonUnaryOperator implements JsonOperator {
    POSITIVE("+", bd -> bd          , bi -> bi          ),
    NEGATIVE("-", BigDecimal::negate, BigInteger::negate),
    BITWISE_NOT("~", BigInteger::not),
    LOGICAL_NOT("not", (NodeUnaryOperator) JsonUnaryOperator::notOperation);

    @Getter
    private final String symbol;
    private final ExpressionUnaryOperator instance;

    JsonUnaryOperator(String symbol, Function<BigDecimal, Object> bd, Function<BigInteger, Object> bi) {
        this(symbol, NumberUnaryOperator.of(symbol, bd, bi));
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
    public ExpressionResult run(JsonScriptRuntime runtime, JsonExpression... expressions) throws ExpressionRunException {
        return this.instance.run(runtime, expressions[0]);
    }

    @Nullable
    public static JsonUnaryOperator fromSymbol(String symbol) {
        for(JsonUnaryOperator operator : values()) {
            if(operator.symbol.equals(symbol)) return operator;
        }
        return null;
    }

    private static JsonNode notOperation(JsonScriptRuntime runtime, JsonNode value) throws ExpressionRunException {
        if(!value.isBoolean()) {
            throw runtime.exception("left operand must be a boolean type, instead " + value.getNodeType() + " was given");
        }
        return BooleanNode.valueOf(!value.booleanValue());
    }
}
