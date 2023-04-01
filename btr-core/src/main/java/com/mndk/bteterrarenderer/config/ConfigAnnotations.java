package com.mndk.bteterrarenderer.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ConfigAnnotations {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    @interface Name {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Comment {
        String[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    @interface Ignore {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface RangeInt {
        int min() default Integer.MIN_VALUE;
        int max() default Integer.MAX_VALUE;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface RangeDouble {
        double min() default Double.MIN_VALUE;
        double max() default Double.MAX_VALUE;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface SlidingOption {}

}
