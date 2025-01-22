package com.mndk.bteterrarenderer.mcconnector.config;

import com.mndk.bteterrarenderer.mcconnector.config.annotation.ConfigComment;
import com.mndk.bteterrarenderer.mcconnector.config.annotation.ConfigIgnore;
import com.mndk.bteterrarenderer.mcconnector.config.annotation.ConfigName;
import com.mndk.bteterrarenderer.mcconnector.config.annotation.ConfigurableClass;
import com.mndk.bteterrarenderer.util.BTRUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractConfigSaveLoader {

    private boolean initialized = false;
    private final Class<?> configClass;
    private List<ConfigPropertyConnection> connections = null;

    protected AbstractConfigSaveLoader(Class<?> configClass) {
        this.configClass = configClass;
    }

    /**
     * Do not use this method in the constructor.
     */
    public void initialize() {
        if (this.initialized) return;

        try {
            this.connections = this.getConnections(configClass);
            this.postInitialization();
            this.initialized = true;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public final void save() {
        if (!this.initialized) this.initialize();

        for (ConfigPropertyConnection connection : this.connections) {
            connection.save();
        }
        this.saveToFile();
    }

    public final void load() {
        if (!this.initialized) this.initialize();

        this.loadFromFile();
        for (ConfigPropertyConnection connection : this.connections) {
            connection.load();
        }
    }

    public final List<ConfigPropertyConnection> getConnections(Class<?> clazz) throws IllegalAccessException {
        Field[] fields = clazz.getDeclaredFields();
        List<ConfigPropertyConnection> connections = new ArrayList<>();
        for (Field f : fields) {
            if (!Modifier.isStatic(f.getModifiers())) continue;
            connections.addAll(getConnections(f, null));
        }
        return connections;
    }

    private <T> List<ConfigPropertyConnection> getConnections(Field field, Object parentInstance) throws IllegalAccessException {
        if (field.getAnnotation(ConfigIgnore.class) != null) return Collections.emptyList();

        ConfigName configName = field.getAnnotation(ConfigName.class);
        String name = configName == null ? field.getName() : configName.value();

        ConfigComment configComment = field.getAnnotation(ConfigComment.class);
        String comment = configComment == null ? null : Arrays.stream(configComment.value())
                .reduce((prev, curr) -> prev + "\n" + curr).orElse("");

        Class<?> fieldClass = field.getType();
        T fieldValue = BTRUtil.uncheckedCast(field.get(parentInstance));
        if (fieldValue == null) {
            throw new NullPointerException("Field value \"" + field.getName() + "\" is null for class " + field.getDeclaringClass().getName());
        }
        if (!fieldClass.isPrimitive() && !fieldClass.isEnum() && fieldClass != String.class) {
            return getClassConnections(name, comment, fieldClass, fieldValue);
        }

        Supplier<T> getter = () -> {
            try { return BTRUtil.uncheckedCast(field.get(parentInstance)); }
            catch (Throwable t) { throw new RuntimeException(t); }
        };
        Consumer<T> setter = (value) -> {
            try { field.set(parentInstance, value); }
            catch (Throwable t) { throw new RuntimeException(t); }
        };

        ConfigPropertyConnection connection = this.makePropertyConnection(field, name, comment, getter, setter, fieldValue);
        return Collections.singletonList(connection);
    }

    private List<ConfigPropertyConnection> getClassConnections(String name, @Nullable String comment,
                                                               Class<?> fieldClass, Object instance) throws IllegalAccessException {
        if (fieldClass.getAnnotation(ConfigurableClass.class) == null) return Collections.emptyList();

        Field[] childFields = fieldClass.getDeclaredFields();

        List<ConfigPropertyConnection> connections = new ArrayList<>();
        this.onPush(name, comment);
        for (Field childField : childFields) {
            connections.addAll(getConnections(childField, instance));
        }
        this.onPop();

        return connections;
    }

    protected abstract void onPush(String pathName, @Nullable String comment);
    protected abstract void onPop();
    protected abstract <T> ConfigPropertyConnection makePropertyConnection(Field field, String name, @Nullable String comment,
                                                                           Supplier<T> getter, Consumer<T> setter,
                                                                           @Nonnull T defaultValue);
    protected abstract void postInitialization();
    protected abstract void saveToFile();
    protected abstract void loadFromFile();

}
