package com.mndk.mapdisp4bte.config;

import com.mndk.mapdisp4bte.MapDisplayer4BTE;
import org.yaml.snakeyaml.Yaml;

import java.io.*;

public class ConfigHandler {

    public static final Yaml YAML = new Yaml();
    private static final String fileLocation = "config/" + MapDisplayer4BTE.MODID + ".yml";
    private static ModConfig config;

    public static void init() throws IOException {
        if(!new File(fileLocation).exists()) {
            saveDefaultFile();
        }

        try {
            config = new ModConfig(YAML.load(new FileReader(fileLocation)));
        } catch(FileNotFoundException e) {
            System.out.println("Cannot load configuration file!");
        }
        System.out.println(config.getYLevel());
    }

    public static ModConfig getModConfig() {
        return config;
    }

    public static void saveDefaultFile() throws IOException {
        FileWriter writer = new FileWriter(fileLocation);
        new ModConfig().saveTo(YAML, writer);
    }

    public static void saveConfig() throws IOException {
        config.saveTo(YAML, new FileWriter(fileLocation));
    }
}
