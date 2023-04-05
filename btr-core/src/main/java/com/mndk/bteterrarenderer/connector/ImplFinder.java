package com.mndk.bteterrarenderer.connector;

import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ImplFinder {
    private static final Reflections REFLECTIONS = new Reflections("com.mndk.bteterrarenderer");
    private static final Map<Class<?>, Object> IMPL_MAP = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T search(Class<T> connectorClazz) {
        for(Map.Entry<Class<?>, Object> entry : IMPL_MAP.entrySet()) {
            if(entry.getKey().isAssignableFrom(connectorClazz)) {
                return (T) entry.getValue();
            }
        }
        throw new RuntimeException("No connectorImpl found for " + connectorClazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T search() {
        try {
            StackTraceElement callStack = Thread.currentThread().getStackTrace()[2];
            Class<T> callerClazz = (Class<T>) Class.forName(callStack.getClassName());
            return search(callerClazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        Set<Class<?>> clazzSet = REFLECTIONS.getTypesAnnotatedWith(ConnectorImpl.class);
        clazzSet.forEach(clazz -> {
            try {
                IMPL_MAP.put(clazz, clazz.getConstructor().newInstance());
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
