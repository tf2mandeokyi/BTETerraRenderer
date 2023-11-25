package com.mndk.bteterrarenderer.core.util;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class Loggers {
    private final Map<Class<?>, Logger> LOGGERS = new HashMap<>();

    public Logger get(Class<?> clazz) {
        return LOGGERS.computeIfAbsent(clazz, c -> LogManager.getLogger(BTETerraRendererConstants.NAME + "/" + c.getSimpleName()));
    }

    public Logger get(Object o) {
        return get(o.getClass());
    }

    /** Only use this on static contexts. Otherwise use {@link #get(Object)} */
    @SneakyThrows
    public Logger get() {
        Class<?> clazz = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
        return get(clazz);
    }
}
