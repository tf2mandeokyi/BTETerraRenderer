package com.mndk.bteterrarenderer.jsonscript.expression;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.CONSTRUCTOR)
public @interface ArrayArgumentAcceptable {
    boolean variableSize() default false;
}
