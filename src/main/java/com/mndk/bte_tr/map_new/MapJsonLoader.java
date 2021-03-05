package com.mndk.bte_tr.map_new;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mndk.bte_tr.BTETerraRenderer;
import com.mndk.bte_tr.config.ConfigHandler;
import com.mndk.bte_tr.config.ModConfig;
import com.mndk.bte_tr.util.JsonUtil;

import scala.actors.threadpool.Arrays;

public class MapJsonLoader {
	
	private static final String MAP_JSON_PATH = "assets/" + BTETerraRenderer.MODID + "/maps.json";
	
	public static List<ExternalMapSet> maps = new ArrayList<>();
	
	public static void load() throws Exception {
		
		maps = new ArrayList<>();
		JsonElement mapJson = new JsonParser().parse(new InputStreamReader(MapJsonLoader.class.getClassLoader().getResourceAsStream(MAP_JSON_PATH)));
		JsonArray array = mapJson.getAsJsonObject().get("categories").getAsJsonArray();
		for(JsonElement element : array) {
			if(!element.isJsonObject()) continue;
			maps.add(getMapSetFromJsonObject(element.getAsJsonObject()));
		}
	}
	/*
	public static void initializeDefaultMapJsonFile(FMLPreInitializationEvent event) {
		File directory = event.getModConfigurationDirectory();
		if(!directory.exists()) directory.mkdirs();
		
	}
	*/
	
	private static ExternalMapSet getMapSetFromJsonObject(JsonObject object) throws Exception {
		String name = JsonUtil.validateStringElement(object, "name");
		
		Set<NewExternalMapManager> mapSet = new HashSet<>();
		JsonArray mapJsonArray = object.get("maps").getAsJsonArray();
		for(JsonElement mapElement : mapJsonArray) {
			if(!mapElement.isJsonObject()) continue;
			mapSet.add(NewExternalMapManager.parse(mapElement.getAsJsonObject()));
		}
		
		return new ExternalMapSet(name, mapSet);
	}
	
	public static void main(String[] args) throws Exception {
		load();
	}
}
