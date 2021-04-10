package com.mndk.bteterrarenderer.config;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.BTRConstants;

import java.io.*;

public class ConfigHandler {

	private static final String YAML_FILE_LOCATION = "config/" + BTETerraRenderer.MODID + "/config.yml";
	private static ModConfig config;

	public static void refresh(String fileLocation) throws IOException {
		if(!new File(fileLocation).exists()) {
			saveDefaultFile(fileLocation);
		}

		try {
			config = new ModConfig(BTRConstants.YAML.load(new FileReader(fileLocation)));
		} catch(FileNotFoundException e) {
			System.out.println("Cannot load configuration file!");
		}
	}

	public static void refresh() throws IOException {
		refresh(YAML_FILE_LOCATION);
	}

	public static ModConfig getModConfig() {
		return config;
	}

	public static void saveDefaultFile(String fileLocation) throws IOException {
		FileWriter writer = new FileWriter(fileLocation);
		new ModConfig().saveTo(BTRConstants.YAML, writer);
	}

	public static void saveConfig() throws IOException {
		config.saveTo(BTRConstants.YAML, new FileWriter(YAML_FILE_LOCATION));
	}
}
