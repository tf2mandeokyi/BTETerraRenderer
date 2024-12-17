package com.mndk.bteterrarenderer.mcconnector.config;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.mcconnector.config.annotation.ConfigRangeDouble;
import com.mndk.bteterrarenderer.mcconnector.config.annotation.ConfigRangeInt;
import com.mndk.bteterrarenderer.mcconnector.config.annotation.ConfigSlidingOption;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ForgeCfgConfigSaveLoader extends AbstractConfigSaveLoader {

    private final Configuration configuration;
    private final Stack<String> categoryNameStack = new Stack<>();

    public ForgeCfgConfigSaveLoader(Class<?> configClass, String modId) {
        super(configClass);
        File configDir = Loader.instance().getConfigDir();
        File configFile = new File(configDir, modId + ".cfg");
        this.configuration = new Configuration(configFile);
        this.categoryNameStack.push(Configuration.CATEGORY_GENERAL);
    }

    private String getCategoryWholePath() {
        return categoryNameStack.stream()
                .reduce((prev, curr) -> prev + Configuration.CATEGORY_SPLITTER + curr)
                .orElse("");
    }

    @Override
    protected void onPush(String pathName, @Nullable String comment) {
        categoryNameStack.push(pathName);
        this.configuration.getCategory(this.getCategoryWholePath()).setComment(comment);
    }

    @Override
    protected void onPop() {
        categoryNameStack.pop();
    }

    @Override
    protected ConfigPropertyConnection makePropertyConnection(Field field, String name, @Nullable String comment,
                                                              Supplier<?> getter, Consumer<Object> setter, Object defaultValue) {
        String wholePath = this.getCategoryWholePath();
        Class<?> clazz = field.getType();
        List<Consumer<Property>> propertyConsumers = new ArrayList<>();

        // Determine property type
        Property.Type type = Property.Type.STRING;
        if (clazz == Integer.class || clazz == int.class) type = Property.Type.INTEGER;
        else if (clazz == Double.class || clazz == double.class) type = Property.Type.DOUBLE;
        else if (clazz == Boolean.class || clazz == boolean.class) type = Property.Type.BOOLEAN;
        final Property.Type finalType = type;

        // Set property object
        String defaultString = String.valueOf(defaultValue);
        Supplier<Property> propertyGetter = () -> this.configuration.get(wholePath, name, defaultString, comment, finalType);
        Function<Property, Object> propertyFunction = Property::getString;

        // Check if enum
        if (Enum.class.isAssignableFrom(clazz)) {
            Enum<?>[] values = BTRUtil.uncheckedCast(clazz.getEnumConstants());
            String[] defaultValues = Arrays.stream(values).map(Enum::name).toArray(String[]::new);
            propertyFunction = p -> Enum.valueOf(BTRUtil.uncheckedCast(clazz), p.getString());

            propertyConsumers.add(p -> p.setDefaultValues(defaultValues));
        }

        // Property functions: used for load
        if (clazz == Integer.class || clazz == int.class) propertyFunction = Property::getInt;
        else if (clazz == Double.class || clazz == double.class) propertyFunction = Property::getDouble;
        else if (clazz == Boolean.class || clazz == boolean.class) propertyFunction = Property::getBoolean;
        final Function<Property, Object> finalPropertyFunction = propertyFunction;

        ConfigRangeInt rangeInt = field.getAnnotation(ConfigRangeInt.class);
        if (rangeInt != null) {
            propertyConsumers.add(p -> p.setMinValue(rangeInt.min()));
            propertyConsumers.add(p -> p.setMaxValue(rangeInt.max()));
        }

        ConfigRangeDouble rangeDouble = field.getAnnotation(ConfigRangeDouble.class);
        if (rangeDouble != null) {
            propertyConsumers.add(p -> p.setMinValue(rangeDouble.min()));
            propertyConsumers.add(p -> p.setMaxValue(rangeDouble.max()));
        }

        ConfigSlidingOption slidingOption = field.getAnnotation(ConfigSlidingOption.class);
        if (slidingOption != null) {
            propertyConsumers.add(p -> p.setHasSlidingControl(true));
        }

        return new ConfigPropertyConnection() {
            public void save() {
                Property p = propertyGetter.get();
                for (Consumer<Property> propertyConsumer : propertyConsumers) propertyConsumer.accept(p);
                p.set(String.valueOf(getter.get()));
            }
            public void load() {
                Property p = propertyGetter.get();
                for (Consumer<Property> propertyConsumer : propertyConsumers) propertyConsumer.accept(p);
                setter.accept(finalPropertyFunction.apply(p));
            }
        };
    }

    @Override
    protected void postInitialization() {}

    @Override
    protected void saveToFile() {
        this.configuration.save();
    }

    @Override
    protected void loadFromFile() {
        this.configuration.load();
    }
}
