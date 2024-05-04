package com.mndk.bteterrarenderer.jsonscript.expression;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mndk.bteterrarenderer.jsonscript.expression.control.ClosureExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.control.IfBranchesExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.define.*;
import com.mndk.bteterrarenderer.jsonscript.expression.function.PrintExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.function.StringTemplateFunction;
import com.mndk.bteterrarenderer.jsonscript.expression.literal.LambdaExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.literal.LiteralExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.operator.BinaryOperatorExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.operator.OperatorsExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.operator.UnaryOperatorExpression;

public class Expressions {

    public static final BiMap<String, Class<? extends JsonExpression>> RESERVED = HashBiMap.create();

    static {
        // TODO: Satisfy assets/bteterrarenderer/jsonscript/specification.yml!!!

        // variable & function operations
        RESERVED.put("def", DefineFunctionExpression.class);
        RESERVED.put("call", FunctionCallExpression.class);
        RESERVED.put("let", DeclareVariableExpression.class);
        RESERVED.put("lets", DeclareMultiVariableExpression.class);
        RESERVED.put("set", AssignToVariableExpression.class);
        RESERVED.put("get", GetVariableExpression.class);

        // variable operations
        RESERVED.put("literal", LiteralExpression.class);
        RESERVED.put("lambda", LambdaExpression.class);

        // operations
        RESERVED.put("un-op", UnaryOperatorExpression.class);
        RESERVED.put("bi-op", BinaryOperatorExpression.class);
        RESERVED.put("ops", OperatorsExpression.class);
//        RESERVED_EXPRESSIONS.put("setop", );

        // control flow statements
        RESERVED.put("closure", ClosureExpression.class);
        RESERVED.put("ifs", IfBranchesExpression.class);
//        RESERVED_EXPRESSIONS.put("while", );
//        RESERVED_EXPRESSIONS.put("for", );
//        RESERVED_EXPRESSIONS.put("foreach", );

        // TODO: test only; remove this
        RESERVED.put("print", PrintExpression.class);
        RESERVED.put("str-template", StringTemplateFunction.class);
    }
}
