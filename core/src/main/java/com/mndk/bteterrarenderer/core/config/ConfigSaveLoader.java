package com.mndk.bteterrarenderer.core.config;

import com.mndk.bteterrarenderer.core.loader.ConfigLoaders;
import lombok.Getter;

import java.io.File;
import java.util.List;

public class ConfigSaveLoader {

    public static <C> ConfigSaveLoader makeSaveLoader(Class<C> configClass) {
        return new ConfigSaveLoader(configClass,
                new DefaultYamlConfigBuilder(() -> new File(ConfigLoaders.MOD_CONFIG_DIRECTORY, "config.yml")));
    }

    private boolean initialized = false;
    private final Class<?> configClass;
    private List<ConfigPropertyConnection> connections = null;
    @Getter
    private final AbstractConfigBuilder configBuilder;

    public ConfigSaveLoader(Class<?> configClass, AbstractConfigBuilder builder) {
        this.configClass = configClass;
        this.configBuilder = builder;
    }

    /**
     * Do not use this method in the constructor.
     */
    public void initialize() {
        if(this.initialized) return;

        try {
            this.connections = this.configBuilder.getConnections(configClass);
            this.configBuilder.postInitialization();
            this.initialized = true;
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        if(!this.initialized) this.initialize();

        for(ConfigPropertyConnection connection : this.connections) {
            connection.save.run();
        }
        this.configBuilder.saveAll();
    }

    public void load() {
        if(!this.initialized) this.initialize();

        this.configBuilder.loadAll();
        for(ConfigPropertyConnection connection : this.connections) {
            connection.load.run();
        }
    }

}
