package com.mndk.bteterrarenderer.jsonscript.expression.operator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OperatorType {
    UNARY(12),
    MULTIPLICATIVE(11),
    ADDITIVE(10),
    SHIFT(9),
    RELATIONAL(8),
    EQUALITY(7),
    BITWISE_AND(6),
    BITWISE_XOR(5),
    BITWISE_OR(4),
    LOGICAL_AND(3),
    LOGICAL_XOR(2),
    LOGICAL_OR(1),
    ASSIGNMENT(0);

    private final int precedence;
}
