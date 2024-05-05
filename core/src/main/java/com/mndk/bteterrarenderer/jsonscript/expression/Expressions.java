package com.mndk.bteterrarenderer.jsonscript.expression;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mndk.bteterrarenderer.jsonscript.expression.control.*;
import com.mndk.bteterrarenderer.jsonscript.expression.define.*;
import com.mndk.bteterrarenderer.jsonscript.expression.function.PrintExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.function.StringTemplateFunction;
import com.mndk.bteterrarenderer.jsonscript.expression.literal.*;
import com.mndk.bteterrarenderer.jsonscript.expression.operator.AssignOperatorExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.operator.BinaryOperatorExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.operator.OperatorsExpression;
import com.mndk.bteterrarenderer.jsonscript.expression.operator.UnaryOperatorExpression;

public class Expressions {

    public static final BiMap<String, Class<? extends JsonExpression>> RESERVED = HashBiMap.create();

    static {
        // TODO: Satisfy assets/bteterrarenderer/jsonscript/specification.yml!!!

        // variable & function operations
        RESERVED.put("def", DefineFunctionExpression.class);
        RESERVED.put("let", DeclareVariableExpression.class);
        RESERVED.put("lets", DeclareMultiVariableExpression.class);
        RESERVED.put("set", AssignToVariableExpression.class);
        RESERVED.put("get", GetVariableExpression.class);

        // variable operations
        RESERVED.put("literal", LiteralExpression.class);
        RESERVED.put("list", ListExpression.class);
        RESERVED.put("object", ObjectExpression.class);
        RESERVED.put("typeof", GetValueTypeExpression.class);
        RESERVED.put("lambda", LambdaExpression.class);

        // operations
        RESERVED.put("un-op", UnaryOperatorExpression.class);
        RESERVED.put("bi-op", BinaryOperatorExpression.class);
        RESERVED.put("ops", OperatorsExpression.class);
        RESERVED.put("set-op", AssignOperatorExpression.class);

        // control flow statements
        RESERVED.put("closure", ClosureExpression.class);
        RESERVED.put("ifs", IfBranchesExpression.class);
        RESERVED.put("while", WhileExpression.class);
        RESERVED.put("do-while", DoWhileExpression.class);
        RESERVED.put("for", ForLoopExpression.class);
        RESERVED.put("foreach", ForeachLoopExpression.class);
        RESERVED.put("continue", ContinueLoopExpression.class);
        RESERVED.put("break", BreakLoopExpression.class);
        RESERVED.put("return", ReturnFunctionExpression.class);
        RESERVED.put("call", FunctionCallExpression.class);

        // TODO: test only; remove this
        RESERVED.put("print", PrintExpression.class);
        RESERVED.put("str-template", StringTemplateFunction.class);
    }
}
