package com.mndk.bteterrarenderer.connector;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.util.BtrUtil;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ImplFinder {
    private static final Set<Class<?>> IMPL_SET = new HashSet<>();
    private static final Map<Class<?>, Object> IMPL_OBJECT_MAP = new HashMap<>();

    public static <T> T search(Class<T> connectorClazz) {
        for(Class<?> implClazz : IMPL_SET) {
            if(!connectorClazz.isAssignableFrom(implClazz)) continue;
            return BtrUtil.uncheckedCast(IMPL_OBJECT_MAP.computeIfAbsent(implClazz, ImplFinder::invokeDefaultConstructor));
        }
        throw new RuntimeException("No connectorImpl found for " + connectorClazz + " (Found impls: " + IMPL_SET + ")");
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

    /**
     * Use this method to add impl class manually.
     * <p>
     * <s>DO NOT USE THIS METHOD</s> if the normal {@link ImplFinder#search() search()} method is working fine.
     */
    public static void add(Class<?>... implClazz) {
        IMPL_SET.addAll(Arrays.asList(implClazz));
    }

    private static <T> T invokeDefaultConstructor(Class<T> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        // Setting up REFLECTIONS
        final String rootPackage = "com.mndk." + BTETerraRendererConstants.MODID;
        ClassLoader[] classLoaders = new ClassLoader[]{
            ClasspathHelper.contextClassLoader(), ClasspathHelper.staticClassLoader()
        };
        Configuration reflectionConfiguration = new ConfigurationBuilder()
                .setScanners(Scanners.SubTypes, Scanners.Resources)
                .setUrls(ClasspathHelper.forPackage(rootPackage, classLoaders))
                .filterInputsBy(new FilterBuilder().includePackage(rootPackage));

        // Registering @ConnectedImpl annotated classes
        new Reflections(reflectionConfiguration)
                .getTypesAnnotatedWith(ConnectorImpl.class).forEach(ImplFinder::add);
        new Reflections(rootPackage)
                .getTypesAnnotatedWith(ConnectorImpl.class).forEach(ImplFinder::add);
    }
}
