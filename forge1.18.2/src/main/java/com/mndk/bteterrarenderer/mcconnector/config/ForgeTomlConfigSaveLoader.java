package com.mndk.bteterrarenderer.mcconnector.config;

import com.mndk.bteterrarenderer.util.BTRUtil;
import com.mndk.bteterrarenderer.mcconnector.config.annotation.ConfigRangeDouble;
import com.mndk.bteterrarenderer.mcconnector.config.annotation.ConfigRangeInt;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ForgeTomlConfigSaveLoader extends AbstractConfigSaveLoader {

    private final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

    public ForgeTomlConfigSaveLoader(Class<?> configClass) {
        super(configClass);
    }

    @Override
    protected void onPush(String pathName, @Nullable String comment) {
        builder.comment(comment).push(pathName);
    }

    @Override
    protected void onPop() {
        builder.pop();
    }

    @Override
    protected <T> ConfigPropertyConnection makePropertyConnection(Field field, String name, @Nullable String comment,
                                                                  Supplier<T> getter, Consumer<T> setter,
                                                                  @Nonnull T defaultValue) {
        Class<?> clazz = field.getType();
        ConfigRangeInt rangeInt = field.getAnnotation(ConfigRangeInt.class);
        ConfigRangeDouble rangeDouble = field.getAnnotation(ConfigRangeDouble.class);

        // Set comment
        if (comment != null) builder.comment(comment);

        ForgeConfigSpec.ConfigValue<?> configValue;
        if (Enum.class.isAssignableFrom(clazz)) {
            configValue = builder.defineEnum(name, BTRUtil.uncheckedCast(defaultValue));
        }
        else if (rangeInt != null) {
            configValue = builder.defineInRange(name, (int) defaultValue, rangeInt.min(), rangeInt.max());
        }
        else if (rangeDouble != null) {
            configValue = builder.defineInRange(name, (double) defaultValue, rangeDouble.min(), rangeDouble.max());
        }
        else {
            configValue = builder.define(name, defaultValue);
        }

        return new ConfigPropertyConnection() {
            public void save() {
                configValue.set(BTRUtil.uncheckedCast(getter.get()));
            }
            public void load() {
                setter.accept(BTRUtil.uncheckedCast(configValue.get()));
            }
        };
    }

    @Override
    protected void postInitialization() {
        ForgeConfigSpec configSpec = this.builder.build();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, configSpec);
    }

    @Override
    protected void saveToFile() {}

    @Override
    protected void loadFromFile() {}
}
