package com.mndk.bteterrarenderer.core.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigRangeInt {
    int min() default Integer.MIN_VALUE;
    int max() default Integer.MAX_VALUE;
}
