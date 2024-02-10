package com.mndk.bteterrarenderer.core.util;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.mcconnector.client.MinecraftClientManager;
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
        return LogManager.getLogger(BTETerraRendererConstants.NAME.toLowerCase() + "/" + c.getSimpleName());
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

    public void sendErrorMessageToChat(String message) {
        MinecraftClientManager.INSTANCE.sendFormattedStringToChat("§c[" + BTETerraRendererConstants.NAME + "] " + message);
    }

    public void sendErrorMessageToChat(Class<?> clazz, String message, Throwable t) {
        MinecraftClientManager.INSTANCE.sendFormattedStringToChat("§c[" + BTETerraRendererConstants.NAME + "] " + message);
        MinecraftClientManager.INSTANCE.sendFormattedStringToChat("§c[" + BTETerraRendererConstants.NAME + "] Reason: " + t.getMessage());
        get(clazz).error(message, t);
    }

    public void sendErrorMessageToChat(Object caller, String message, Throwable t) {
        sendErrorMessageToChat(caller.getClass(), message, t);
    }
}
