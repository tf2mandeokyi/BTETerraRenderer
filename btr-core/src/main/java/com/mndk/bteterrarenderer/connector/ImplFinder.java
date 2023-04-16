package com.mndk.bteterrarenderer.connector;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.util.BtrUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class ImplFinder {
    private static final Map<Class<?>, Object> IMPL_MAP = new HashMap<>();

    public static <T> T search(Class<T> connectorClazz) {
        for(Map.Entry<Class<?>, Object> entry : IMPL_MAP.entrySet()) {
            if(connectorClazz.isAssignableFrom(entry.getKey())) {
                return BtrUtil.uncheckedCast(entry.getValue());
            }
        }
        throw new RuntimeException("No connectorImpl found for " + connectorClazz + " (Found impls: " + IMPL_MAP.keySet() + ")");
    }

    public static <T> T search() {
        try {
            StackTraceElement callStack = Thread.currentThread().getStackTrace()[2];
            Class<T> callerClazz = BtrUtil.uncheckedCast(Class.forName(callStack.getClassName()));
            return search(callerClazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        BTETerraRendererConstants.REFLECTIONS.getTypesAnnotatedWith(ConnectorImpl.class).forEach(clazz -> {
            try {
                IMPL_MAP.put(clazz, clazz.getConstructor().newInstance());
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
