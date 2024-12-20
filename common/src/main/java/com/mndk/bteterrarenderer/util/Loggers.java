package com.mndk.bteterrarenderer.util;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class Loggers {
    private final Map<Class<?>, Logger> LOGGERS = new HashMap<>();

    public synchronized Logger get(Class<?> clazz) {
        return LOGGERS.computeIfAbsent(clazz, Loggers::makeLogger);
    }

    private Logger makeLogger(Class<?> c) {
        return LogManager.getLogger(BTETerraRenderer.NAME.toLowerCase() + "/" + c.getSimpleName());
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
