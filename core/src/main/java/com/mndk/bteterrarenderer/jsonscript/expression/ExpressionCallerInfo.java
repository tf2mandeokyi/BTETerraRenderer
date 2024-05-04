package com.mndk.bteterrarenderer.jsonscript.expression;

import com.google.common.collect.BiMap;
import com.mndk.bteterrarenderer.core.util.ArrayUtil;
import lombok.Getter;

@Getter
public class ExpressionCallerInfo {
    private final String callerName;
    private final String[] extraInfo;

    public ExpressionCallerInfo(String callerName, String... extraInfo) {
        this.callerName = callerName;
        this.extraInfo = extraInfo;
    }

    public ExpressionCallerInfo(JsonExpression expression, String... extraInfo) {
        BiMap<Class<? extends JsonExpression>, String> map = Expressions.RESERVED.inverse();
        this.callerName = map.getOrDefault(expression.getClass(), "(unknown)");
        this.extraInfo = extraInfo;
    }

    public ExpressionCallerInfo(Class<? extends JsonExpression> clazz, String... extraInfo) {
        BiMap<Class<? extends JsonExpression>, String> map = Expressions.RESERVED.inverse();
        this.callerName = map.getOrDefault(clazz, "(unknown)");
        this.extraInfo = extraInfo;
    }

    public ExpressionCallerInfo add(String info) {
        return new ExpressionCallerInfo(callerName, ArrayUtil.expandOne(this.extraInfo, info, String[]::new));
    }
}
