package com.mndk.bteterrarenderer.jsonscript.parameter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParameterType {
    NORMAL(0), OPTIONAL(1), VARIABLE(2);
    private final int positionOrder;
}
