package com.mndk.bteterrarenderer.jsonscript.exp.res.op;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.mndk.bteterrarenderer.core.util.json.JsonParserUtil;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionCallerInfo;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionResult;
import com.mndk.bteterrarenderer.jsonscript.exp.JsonExpression;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptJsonValue;
import com.mndk.bteterrarenderer.jsonscript.value.JsonScriptValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.BiFunction;

import static com.mndk.bteterrarenderer.jsonscript.exp.res.op.OperatorType.*;

@RequiredArgsConstructor
public enum JsonBinaryOperator implements JsonOperator {
    PLUS    ("+", ADDITIVE      , true, BigDecimal::add      , BigInteger::add      ),
    MINUS   ("-", ADDITIVE      , true, BigDecimal::subtract , BigInteger::subtract ),
    MULTIPLY("*", MULTIPLICATIVE, true, BigDecimal::multiply , BigInteger::multiply ),
    DIVIDE  ("/", MULTIPLICATIVE, true, BigDecimal::divide   , BigInteger::divide   ),
    MODULO  ("%", MULTIPLICATIVE, true, BigDecimal::remainder, BigInteger::remainder),

    BITWISE_AND("&", OperatorType.BITWISE_AND, true, BigInteger::and),
    BITWISE_OR ("|", OperatorType.BITWISE_OR , true, BigInteger::or ),
    BITWISE_XOR("^", OperatorType.BITWISE_XOR, true, BigInteger::xor),

    SHIFT_LEFT       ("<<", SHIFT, true, (l, r) -> l.shiftLeft(r.intValue())),
    SHIFT_RIGHT_ARITH(">>", SHIFT, true, (l, r) -> l.shiftRight(r.intValue())),

    LOGICAL_AND("and", OperatorType.LOGICAL_AND, false, JsonBinaryOperator::andOperation),
    LOGICAL_OR ("or",  OperatorType.LOGICAL_OR , false, JsonBinaryOperator::orOperation),
    LOGICAL_XOR("xor", OperatorType.LOGICAL_XOR, false, JsonBinaryOperator::xorOperation),

    EQUALS    ("==", EQUALITY, false, JsonBinaryOperator::equalsOperation),
    NOT_EQUALS("!=", EQUALITY, false, JsonBinaryOperator::notEqualsOperation),

    GTE(">=", RELATIONAL, false, (l, r) -> l.compareTo(r) >= 0, (l, r) -> l.compareTo(r) >= 0),
    LTE("<=", RELATIONAL, false, (l, r) -> l.compareTo(r) <= 0, (l, r) -> l.compareTo(r) <= 0),
    GT (">",  RELATIONAL, false, (l, r) -> l.compareTo(r) >  0, (l, r) -> l.compareTo(r) >  0),
    LT ("<",  RELATIONAL, false, (l, r) -> l.compareTo(r) <  0, (l, r) -> l.compareTo(r) <  0);

    private static final String LEFT_OPERAND = "left operand";
    private static final ExpressionCallerInfo AND_INFO_LEFT = LOGICAL_AND.info.add(LEFT_OPERAND);
    private static final ExpressionCallerInfo OR_INFO_LEFT  = LOGICAL_OR .info.add(LEFT_OPERAND);
    private static final ExpressionCallerInfo XOR_INFO_LEFT = LOGICAL_XOR.info.add(LEFT_OPERAND);

    private static final String RIGHT_OPERAND = "right operand";
    private static final ExpressionCallerInfo AND_INFO_RIGHT = LOGICAL_AND.info.add(RIGHT_OPERAND);
    private static final ExpressionCallerInfo OR_INFO_RIGHT  = LOGICAL_OR .info.add(RIGHT_OPERAND);
    private static final ExpressionCallerInfo XOR_INFO_RIGHT = LOGICAL_XOR.info.add(RIGHT_OPERAND);

    @Getter(onMethod_ = @JsonValue)
    private final String symbol;
    @Getter
    private final OperatorType type;
    @Getter
    private final boolean assignable;
    private final ExpressionCallerInfo info;
    @Nonnull
    private ExpressionBinaryOperator instance;

    JsonBinaryOperator(String symbol, OperatorType type, boolean assignable) {
        this.symbol = symbol;
        this.type = type;
        this.assignable = assignable;
        this.info = new ExpressionCallerInfo("binary operator", symbol);
    }

    JsonBinaryOperator(String symbol, OperatorType type, boolean assignable, @Nonnull ExpressionBinaryOperator instance) {
        this(symbol, type, assignable);
        this.instance = instance;
    }

    JsonBinaryOperator(String symbol, OperatorType type, boolean assignable, NodeBinaryOperator instance) {
        this(symbol, type, assignable);

        ExpressionCallerInfo leftInfo = this.info.add(LEFT_OPERAND);
        ExpressionCallerInfo rightInfo = this.info.add(RIGHT_OPERAND);
        this.instance = new EboNodeImpl(leftInfo, rightInfo, instance);
    }

    JsonBinaryOperator(String symbol, OperatorType type, boolean assignable,
                       BiFunction<BigDecimal, BigDecimal, Object> bd, BiFunction<BigInteger, BigInteger, Object> bi)
    {
        this(symbol, type, assignable);

        NodeBinaryOperator nbo = (runtime, left, right) -> {
            if (!left.isNumber() || !right.isNumber()) {
                return ExpressionResult.error("Cannot apply binary operator " +
                        "'" + this.symbol + "' to values of type - " +
                        "left: " + left.getNodeType() + ", right: " + right.getNodeType(), this.info);
            }

            JsonNode leftNumber = JsonParserUtil.toBiggerPrimitiveNode(left);
            JsonNode rightNumber = JsonParserUtil.toBiggerPrimitiveNode(right);
            Object result;
            if(leftNumber instanceof DecimalNode || rightNumber instanceof DecimalNode) {
                result = bd.apply(leftNumber.decimalValue(), rightNumber.decimalValue());
            } else {
                result = bi.apply(leftNumber.bigIntegerValue(), rightNumber.bigIntegerValue());
            }
            return ExpressionResult.ok(JsonParserUtil.primitiveToBigNode(result));
        };

        ExpressionCallerInfo leftInfo = this.info.add(LEFT_OPERAND);
        ExpressionCallerInfo rightInfo = this.info.add(RIGHT_OPERAND);
        this.instance = new EboNodeImpl(leftInfo, rightInfo, nbo);
    }

    JsonBinaryOperator(String symbol, OperatorType type, boolean assignable, BiFunction<BigInteger, BigInteger, Object> bi) {
        this(symbol, type, assignable, (left, right) -> bi.apply(left.toBigInteger(), right.toBigInteger()), bi);
    }

    @Override
    public int getArgumentCount() {
        return 2;
    }

    @Override
    public ExpressionResult run(JsonScriptRuntime runtime, JsonExpression... expressions) {
        return this.instance.run(runtime, expressions[0], expressions[1]);
    }

    @Override
    public String toString() {
        return this.symbol;
    }

    @Nullable
    public static JsonBinaryOperator fromSymbol(String symbol) {
        for(JsonBinaryOperator operator : values()) {
            if(operator.symbol.equals(symbol)) return operator;
        }
        return null;
    }

    private static ExpressionResult equalsOperation(JsonScriptRuntime runtime, JsonNode left, JsonNode right) {
        return ExpressionResult.ok(BooleanNode.valueOf(left.equals(right)));
    }

    private static ExpressionResult notEqualsOperation(JsonScriptRuntime runtime, JsonNode left, JsonNode right) {
        return ExpressionResult.ok(BooleanNode.valueOf(!left.equals(right)));
    }

    private static ExpressionResult xorOperation(JsonScriptRuntime runtime, JsonNode left, JsonNode right) {
        if(!left.isBoolean()) {
            return ExpressionResult.error("left operand must be a boolean type, " +
                    "instead " + left.getNodeType() + " was given", XOR_INFO_LEFT);
        }
        if(!right.isBoolean()) {
            return ExpressionResult.error("right operand is not a boolean type, " +
                    "instead " + right.getNodeType() + " was given", XOR_INFO_RIGHT);
        }
        return ExpressionResult.ok(BooleanNode.valueOf(left.booleanValue() ^ right.booleanValue()));
    }

    private static ExpressionResult andOperation(JsonScriptRuntime runtime, JsonExpression left, JsonExpression right) {
        ExpressionResult result;
        JsonScriptValue value;
        JsonNode node;

        // LEFT
        if((result = left.run(runtime, AND_INFO_LEFT)).isBreakType()) return result;
        if(!((value = result.getValue()) instanceof JsonScriptJsonValue)) {
            return ExpressionResult.error("left operand must be a json type", AND_INFO_LEFT);
        }
        if(!(node = ((JsonScriptJsonValue) value).getNode()).isBoolean()) {
            return ExpressionResult.error("left operand is not a boolean type", AND_INFO_LEFT);
        }
        if(!node.booleanValue()) return ExpressionResult.ok(BooleanNode.FALSE);

        // RIGHT
        if((result = right.run(runtime, AND_INFO_RIGHT)).isBreakType()) return result;
        if(!((value = result.getValue()) instanceof JsonScriptJsonValue)) {
            return ExpressionResult.error("right operand must be a json type", AND_INFO_RIGHT);
        }
        if(!(node = ((JsonScriptJsonValue) value).getNode()).isBoolean()) {
            return ExpressionResult.error("right operand is not a boolean type", AND_INFO_RIGHT);
        }
        return ExpressionResult.ok(node);
    }

    private static ExpressionResult orOperation(JsonScriptRuntime runtime, JsonExpression left, JsonExpression right) {
        ExpressionResult result;
        JsonScriptValue value;
        JsonNode node;

        // LEFT
        if((result = left.run(runtime, OR_INFO_LEFT)).isBreakType()) return result;
        if(!((value = result.getValue()) instanceof JsonScriptJsonValue)) {
            return ExpressionResult.error("left operand must be a json type", OR_INFO_LEFT);
        }
        if(!(node = ((JsonScriptJsonValue) value).getNode()).isBoolean()) {
            return ExpressionResult.error("left operand is not a boolean type", OR_INFO_LEFT);
        }
        if(node.booleanValue()) return ExpressionResult.ok(BooleanNode.TRUE);

        // RIGHT
        if((result = right.run(runtime, OR_INFO_RIGHT)).isBreakType()) return result;
        if(!((value = result.getValue()) instanceof JsonScriptJsonValue)) {
            return ExpressionResult.error("right operand must be a json type", OR_INFO_RIGHT);
        }
        if(!(node = ((JsonScriptJsonValue) value).getNode()).isBoolean()) {
            return ExpressionResult.error("right operand is not a boolean type", OR_INFO_RIGHT);
        }
        return ExpressionResult.ok(node);
    }

    @FunctionalInterface
    private interface ExpressionBinaryOperator {
        ExpressionResult run(JsonScriptRuntime runtime, JsonExpression left, JsonExpression right);
    }

    @FunctionalInterface
    private interface NodeBinaryOperator {
        ExpressionResult run(JsonScriptRuntime runtime, JsonNode left, JsonNode right);
    }

    @RequiredArgsConstructor
    private static class EboNodeImpl implements ExpressionBinaryOperator {

        private final ExpressionCallerInfo leftInfo, rightInfo;
        private final NodeBinaryOperator instance;

        @Override
        public ExpressionResult run(JsonScriptRuntime runtime, JsonExpression left, JsonExpression right) {
            ExpressionResult result;
            JsonScriptValue value;

            // LEFT
            if((result = left.run(runtime, this.leftInfo)).isBreakType()) return result;
            if(!((value = result.getValue()) instanceof JsonScriptJsonValue)) {
                return ExpressionResult.error("left operand must be a json type", this.leftInfo);
            }
            JsonNode leftNode = JsonParserUtil.toBiggerPrimitiveNode(((JsonScriptJsonValue) value).getNode());

            // RIGHT
            if((result = right.run(runtime, this.rightInfo)).isBreakType()) return result;
            if(!((value = result.getValue()) instanceof JsonScriptJsonValue)) {
                return ExpressionResult.error("right operand must be a json type", this.rightInfo);
            }
            JsonNode rightValue = JsonParserUtil.toBiggerPrimitiveNode(((JsonScriptJsonValue) value).getNode());

            return this.instance.run(runtime, leftNode, rightValue);
        }
    }
}
