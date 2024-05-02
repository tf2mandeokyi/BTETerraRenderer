package com.mndk.bteterrarenderer.jsonscript.exp.res.op;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.mndk.bteterrarenderer.core.util.json.JsonParserUtil;
import com.mndk.bteterrarenderer.jsonscript.JsonScriptRuntime;
import com.mndk.bteterrarenderer.jsonscript.exp.ExpressionRunException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

public interface NumberUnaryOperator<T> extends NodeUnaryOperator {

    String getSymbol();
    T apply(BigDecimal value);
    T apply(BigInteger value);

    @Override
    default JsonNode run(JsonScriptRuntime runtime, JsonNode value) throws ExpressionRunException {
        if (!value.isNumber()) {
            throw runtime.exception("Cannot apply unary operator '" + this.getSymbol() + "' to value of type "
                    + value.getNodeType());
        }

        JsonNode number = JsonParserUtil.toBiggerPrimitiveNode(value);
        T result;
        if(number instanceof DecimalNode) {
            result = this.apply(number.decimalValue());
        }
        else {
            result = this.apply(number.bigIntegerValue());
        }
        return JsonParserUtil.primitiveToBigNode(result);
    }

    static <T> NumberUnaryOperator<T> of(String symbol, Function<BigDecimal, T> bd, Function<BigInteger, T> bi) {
        return new NumberUnaryOperator<T>() {
            public String getSymbol() {
                return symbol;
            }
            public T apply(BigDecimal value) {
                return bd.apply(value);
            }
            public T apply(BigInteger value) {
                return bi.apply(value);
            }
        };
    }
}
