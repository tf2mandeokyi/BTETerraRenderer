package com.mndk.bteterrarenderer.mcconnector;

import com.mndk.bteterrarenderer.mcconnector.config.AbstractConfigSaveLoader;
import com.mndk.bteterrarenderer.mcconnector.config.DefaultYamlConfigSaveLoader;
import com.mndk.bteterrarenderer.mcconnector.i18n.I18nManager;
import com.mndk.bteterrarenderer.mcconnector.util.ResourceLocationWrapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class CommonMinecraftManager {

    public final I18nManager i18nManager;

    public AbstractConfigSaveLoader newConfigSaveLoader(Class<?> configClass, String modId) {
        return new DefaultYamlConfigSaveLoader(
                configClass, () -> new File(this.getConfigDirectory(modId), "config.yml"));
    }

    public abstract ResourceLocationWrapper newResourceLocation(String modId, String location);

    public abstract File getGameDirectory();

    protected File getConfigDirectory() {
        return new File(this.getGameDirectory(), "config");
    }

    public final File getConfigDirectory(String modId) {
        return new File(this.getConfigDirectory(), modId);
    }
}
