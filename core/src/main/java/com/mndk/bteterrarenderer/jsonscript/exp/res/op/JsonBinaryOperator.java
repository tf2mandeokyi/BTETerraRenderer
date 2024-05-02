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
import java.util.function.BiFunction;

import static com.mndk.bteterrarenderer.jsonscript.exp.res.op.OperatorType.*;

@RequiredArgsConstructor
public enum JsonBinaryOperator implements JsonOperator {
    PLUS    ("+", ADDITIVE      , BigDecimal::add,       BigInteger::add      ),
    MINUS   ("-", ADDITIVE      , BigDecimal::subtract,  BigInteger::subtract ),
    MULTIPLY("*", MULTIPLICATIVE, BigDecimal::multiply,  BigInteger::multiply ),
    DIVIDE  ("/", MULTIPLICATIVE, BigDecimal::divide,    BigInteger::divide   ),
    MODULO  ("%", MULTIPLICATIVE, BigDecimal::remainder, BigInteger::remainder),

    BITWISE_AND("&", OperatorType.BITWISE_AND, BigInteger::and),
    BITWISE_OR ("|", OperatorType.BITWISE_OR , BigInteger::or ),
    BITWISE_XOR("^", OperatorType.BITWISE_XOR, BigInteger::xor),

    SHIFT_LEFT       ("<<", SHIFT, (l, r) -> l.shiftLeft(r.intValue())),
    SHIFT_RIGHT_ARITH(">>", SHIFT, (l, r) -> l.shiftRight(r.intValue())),

    LOGICAL_AND("and", OperatorType.LOGICAL_AND, JsonBinaryOperator::andOperation),
    LOGICAL_OR ("or",  OperatorType.LOGICAL_OR , JsonBinaryOperator::orOperation),
    LOGICAL_XOR("xor", OperatorType.LOGICAL_XOR, (NodeBinaryOperator) JsonBinaryOperator::xorOperation),

    EQUALS    ("==", EQUALITY, (NodeBinaryOperator) JsonBinaryOperator::equalsOperation),
    NOT_EQUALS("!=", EQUALITY, (NodeBinaryOperator) JsonBinaryOperator::notEqualsOperation),

    GTE(">=", RELATIONAL, (l, r) -> l.compareTo(r) >= 0, (l, r) -> l.compareTo(r) >= 0),
    LTE("<=", RELATIONAL, (l, r) -> l.compareTo(r) <= 0, (l, r) -> l.compareTo(r) <= 0),
    GT (">",  RELATIONAL, (l, r) -> l.compareTo(r) >  0, (l, r) -> l.compareTo(r) >  0),
    LT ("<",  RELATIONAL, (l, r) -> l.compareTo(r) <  0, (l, r) -> l.compareTo(r) <  0);

    @Getter
    private final String symbol;
    @Getter
    private final OperatorType type;
    private final ExpressionBinaryOperator instance;

    JsonBinaryOperator(String symbol, OperatorType type,
                       BiFunction<BigDecimal, BigDecimal, Object> bd, BiFunction<BigInteger, BigInteger, Object> bi)
    {
        this(symbol, type, NumberBinaryOperator.of(symbol, bd, bi));
    }

    JsonBinaryOperator(String symbol, OperatorType type, BiFunction<BigInteger, BigInteger, Object> bi) {
        this(symbol, type, (left, right) -> bi.apply(left.toBigInteger(), right.toBigInteger()), bi);
    }

    @Override
    public int getArgumentCount() {
        return 2;
    }

    @Override
    public ExpressionResult run(JsonScriptRuntime runtime, JsonExpression... expressions)
            throws ExpressionRunException
    {
        return this.instance.run(runtime, expressions[0], expressions[1]);
    }

    @Nullable
    public static JsonBinaryOperator fromSymbol(String symbol) {
        for(JsonBinaryOperator operator : values()) {
            if(operator.symbol.equals(symbol)) return operator;
        }
        return null;
    }

    private static JsonNode equalsOperation(JsonScriptRuntime runtime, JsonNode left, JsonNode right) {
        return BooleanNode.valueOf(left.equals(right));
    }

    private static JsonNode notEqualsOperation(JsonScriptRuntime runtime, JsonNode left, JsonNode right) {
        return BooleanNode.valueOf(!left.equals(right));
    }

    private static JsonNode xorOperation(JsonScriptRuntime runtime, JsonNode left, JsonNode right)
            throws ExpressionRunException
    {
        if(!left.isBoolean()) {
            throw runtime.exception("left operand must be a boolean type, instead " + left.getNodeType() + " was given");
        }
        if(!right.isBoolean()) {
            throw runtime.exception("right operand is not a boolean type, instead " + right.getNodeType() + " was given");
        }
        return BooleanNode.valueOf(left.booleanValue() ^ right.booleanValue());
    }

    private static ExpressionResult andOperation(JsonScriptRuntime runtime, JsonExpression left, JsonExpression right)
            throws ExpressionRunException
    {
        ExpressionResult result;
        JsonNode value;

        // LEFT
        if((result = left.run(runtime)).isBreakType()) return result;
        if(!(value = result.getValue().getAsJsonValue()).isBoolean()) {
            throw runtime.exception("left operand is not a boolean type");
        }
        if(!value.booleanValue()) return ExpressionResult.ok(BooleanNode.FALSE);

        // RIGHT
        if((result = right.run(runtime)).isBreakType()) return result;
        if(!(value = result.getValue().getAsJsonValue()).isBoolean()) {
            throw runtime.exception("right operand is not a boolean type");
        }
        return ExpressionResult.ok(value);
    }

    private static ExpressionResult orOperation(JsonScriptRuntime runtime, JsonExpression left, JsonExpression right)
            throws ExpressionRunException
    {
        ExpressionResult result;
        JsonNode value;

        // LEFT
        if((result = left.run(runtime)).isBreakType()) return result;
        if(!(value = result.getValue().getAsJsonValue()).isBoolean()) {
            throw runtime.exception("left operand must be a boolean type, instead " + value.getNodeType() + " was given");
        }
        if(value.booleanValue()) return ExpressionResult.ok(BooleanNode.TRUE);

        // RIGHT
        if((result = right.run(runtime)).isBreakType()) return result;
        if(!(value = result.getValue().getAsJsonValue()).isBoolean()) {
            throw runtime.exception("right operand is not a boolean type, instead " + value.getNodeType() + " was given");
        }
        return ExpressionResult.ok(value);
    }
}
