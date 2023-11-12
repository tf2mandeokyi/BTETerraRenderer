package com.mndk.bteterrarenderer.mod.config;

import com.mndk.bteterrarenderer.core.config.AbstractConfigBuilder;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.config.ConfigPropertyConnection;
import com.mndk.bteterrarenderer.core.config.annotation.ConfigRangeDouble;
import com.mndk.bteterrarenderer.core.config.annotation.ConfigRangeInt;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MC18ForgeTomlConfigBuilder extends AbstractConfigBuilder {

    private final ForgeConfigSpec.Builder builder;
    private ForgeConfigSpec.ConfigValue<Boolean> doRenderConfig;

    public MC18ForgeTomlConfigBuilder() {
        builder = new ForgeConfigSpec.Builder();
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
    protected ConfigPropertyConnection makePropertyConnection(Field field, String name, @Nullable String comment,
                                                              Supplier<?> getter, Consumer<Object> setter, Object defaultValue) {
        Class<?> clazz = field.getType();
        ConfigRangeInt rangeInt = field.getAnnotation(ConfigRangeInt.class);
        ConfigRangeDouble rangeDouble = field.getAnnotation(ConfigRangeDouble.class);

        // Set comment
        if(comment != null) builder.comment(comment);

        ForgeConfigSpec.ConfigValue<?> configValue;
        if(Enum.class.isAssignableFrom(clazz)) {
            configValue = builder.defineEnum(name, BTRUtil.uncheckedCast(defaultValue));
        }
        else if(rangeInt != null) {
            configValue = builder.defineInRange(name, (int) defaultValue, rangeInt.min(), rangeInt.max());
        }
        else if(rangeDouble != null) {
            configValue = builder.defineInRange(name, (double) defaultValue, rangeDouble.min(), rangeDouble.max());
        }
        else {
            configValue = builder.define(name, defaultValue);
        }

        // I don't like this
        if("Do Render".equals(name)) {
            doRenderConfig = BTRUtil.uncheckedCast(configValue);
        }

        return new ConfigPropertyConnection(
                () -> configValue.set(BTRUtil.uncheckedCast(getter.get())),
                () -> setter.accept(configValue.get())
        );
    }

    public void saveRenderState() {
        // I don't like this
        doRenderConfig.set(BTETerraRendererConfig.HOLOGRAM.doRender);
    }

    @Override
    protected void postInitialization() {
        ForgeConfigSpec configSpec = this.builder.build();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, configSpec);
    }

    @Override
    protected void saveAll() {}

    @Override
    protected void loadAll() {}
}
