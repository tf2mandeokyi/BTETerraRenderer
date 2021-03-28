package com.mndk.bteterrarenderer.map;

import java.util.List;

public class TileMapJsonResult {
	
	private final List<Category> categories;
	private final int totalMapCount;
	private final int uiElementCount;
	
	public TileMapJsonResult(List<Category> categories) {
		this.categories = categories;
		int tot = 0, ui = 0;
		for(Category category : categories) {
			tot += category.maps.size();
			ui += category.maps.size() + 1;
		}
		this.totalMapCount = tot;
		this.uiElementCount = ui;
	}
	
	public List<Category> getCategories() {
		return this.categories;
	}
	
	public int getTotalMapCount() {
		return this.totalMapCount;
	}
	
	public int getUiElementCount() {
		return this.uiElementCount;
	}
	
	public ExternalTileMap getTileMap(String mapId) {
		for(Category category : categories) {
			for(ExternalTileMap map : category.getMaps()) {
				if(mapId.equals(map.getId())) {
					return map;
				}
			}
		}
		return null;
	}
	
	public static class Category {
		private final String name;
		private final List<ExternalTileMap> maps;
		
		public Category(String name, List<ExternalTileMap> maps) {
			this.name = name;
			this.maps = maps;
		}
		
		public String getName() { 
			return name;
		}
		
		public List<ExternalTileMap> getMaps() {
			return maps;
		}
	}
	
}
