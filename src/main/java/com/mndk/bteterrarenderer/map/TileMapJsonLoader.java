package com.mndk.bteterrarenderer.map;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.util.JsonUtil;

public class TileMapJsonLoader {
	
	private static final String RESOURCES_MAP_JSON_PATH = "assets/" + BTETerraRenderer.MODID + "/maps.json";
	
	public static final JsonParser JSON_PARSER = new JsonParser();
	public static final Gson GSON = new Gson();
	
	public static TileMapJsonResult result;
	
	public static void load(String modConfigDirectory) throws Exception {

		File configFolder = new File(modConfigDirectory + "/" + BTETerraRenderer.MODID);
		File customMapJson = new File(modConfigDirectory + "/" + BTETerraRenderer.MODID + "/maps.json");
		if(!configFolder.isDirectory()) {
			configFolder.mkdirs();
		}
		if(!customMapJson.exists()) {
			saveMapJsonTo(customMapJson);
		}
		result = load(new FileReader(customMapJson));
	}
	

	private static TileMapJsonResult load(Reader fileReader) throws Exception {
		JsonElement mapJson = JSON_PARSER.parse(fileReader);
		JsonArray array = mapJson.getAsJsonObject().get("categories").getAsJsonArray();
		List<TileMapJsonResult.Category> result = new ArrayList<>();
		for(JsonElement element : array) {
			if(!element.isJsonObject()) continue;
			result.add(getMapSetFromJsonObject(element.getAsJsonObject()));
		}
		return new TileMapJsonResult(result);
	}
	
	
	private static void saveMapJsonTo(File file) throws IOException {
		file.createNewFile();
		FileWriter writer = new FileWriter(file);
		
		InputStreamReader reader = new InputStreamReader(
				TileMapJsonLoader.class.getClassLoader().getResourceAsStream(RESOURCES_MAP_JSON_PATH), 
				Charset.defaultCharset()
		);

        int c;
        while ((c = reader.read()) != -1) writer.write(c);
        writer.close();
	}
	
	
	private static TileMapJsonResult.Category getMapSetFromJsonObject(JsonObject object) throws Exception {
		String name = JsonUtil.validateStringElement(object, "name");
		
		List<ExternalTileMap> mapSet = new ArrayList<>();
		JsonArray mapJsonArray = object.get("maps").getAsJsonArray();
		for(JsonElement mapElement : mapJsonArray) {
			if(!mapElement.isJsonObject()) continue;
			mapSet.add(ExternalTileMap.parse(mapElement.getAsJsonObject()));
		}
		
		return new TileMapJsonResult.Category(name, mapSet);
	}
}
