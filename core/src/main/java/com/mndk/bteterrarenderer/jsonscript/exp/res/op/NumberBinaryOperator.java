package com.mndk.bteterrarenderer.jsonscript.exp.res.op;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.mndk.bteterrarenderer.core.util.json.JsonParserUtil;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionRunException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.BiFunction;

public interface NumberBinaryOperator<T> extends NodeBinaryOperator {

    String getSymbol();
    T apply(BigDecimal left, BigDecimal right);
    T apply(BigInteger left, BigInteger right);

    @Override
    default JsonNode run(JsonScriptRuntime runtime, JsonNode left, JsonNode right) throws ExpressionRunException {
        if (!left.isNumber() || !right.isNumber()) {
            throw runtime.exception("Cannot apply binary operator '" + this.getSymbol() + "' to values of type - " +
                    "left: " + left.getNodeType() + ", right: " + right.getNodeType());
        }

        JsonNode leftNumber = JsonParserUtil.toBiggerPrimitiveNode(left);
        JsonNode rightNumber = JsonParserUtil.toBiggerPrimitiveNode(right);
        T result;
        if(leftNumber instanceof DecimalNode || rightNumber instanceof DecimalNode) {
            result = this.apply(leftNumber.decimalValue(), rightNumber.decimalValue());
        } else {
            result = this.apply(leftNumber.bigIntegerValue(), rightNumber.bigIntegerValue());
        }
        return JsonParserUtil.primitiveToBigNode(result);
    }

    static <T> NumberBinaryOperator<T> of(String symbol,
                                          BiFunction<BigDecimal, BigDecimal, T> bd,
                                          BiFunction<BigInteger, BigInteger, T> bi)
    {
        return new NumberBinaryOperator<T>() {
            public String getSymbol() {
                return symbol;
            }
            public T apply(BigDecimal left, BigDecimal right) {
                return bd.apply(left, right);
            }
            public T apply(BigInteger left, BigInteger right) {
                return bi.apply(left, right);
            }
        };
    }
}
