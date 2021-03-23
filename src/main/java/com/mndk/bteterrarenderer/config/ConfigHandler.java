package com.mndk.bteterrarenderer.config;

import org.yaml.snakeyaml.Yaml;

import com.mndk.bteterrarenderer.BTETerraRenderer;

import java.io.*;

public class ConfigHandler {

    public static final Yaml YAML = new Yaml();
    private static final String YAML_FILE_LOCATION = "config/" + BTETerraRenderer.MODID + "/config.yml";
    private static ModConfig config;

    public static void init(String fileLocation) throws IOException {
        if(!new File(fileLocation).exists()) {
            saveDefaultFile(fileLocation);
        }

        try {
            config = new ModConfig(YAML.load(new FileReader(fileLocation)));
        } catch(FileNotFoundException e) {
            System.out.println("Cannot load configuration file!");
        }
    }

    public static void init() throws IOException {
        init(YAML_FILE_LOCATION);
    }

    public static ModConfig getModConfig() {
        return config;
    }

    public static void saveDefaultFile(String fileLocation) throws IOException {
        FileWriter writer = new FileWriter(fileLocation);
        new ModConfig().saveTo(YAML, writer);
    }

    public static void saveConfig() throws IOException {
        config.saveTo(YAML, new FileWriter(YAML_FILE_LOCATION));
    }
}
