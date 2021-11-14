package com.mndk.bteterrarenderer.storage;

import com.mndk.bteterrarenderer.gui.sidebar.elem.SidebarDropdownCategory;
import com.mndk.bteterrarenderer.tms.TileMapService;

import java.util.ArrayList;
import java.util.List;

public class TileMapLoaderResult {
	
	private final List<Category> categories;
	private int totalMapCount;
	private int uiElementCount;
	
	public TileMapLoaderResult() {
		this.categories = new ArrayList<>();
		this.totalMapCount = 0; this.uiElementCount = 0;
	}
	
	public TileMapLoaderResult(List<Category> categories) {
		this.categories = categories;
		this.totalMapCount = 0; this.uiElementCount = 0;
		for(Category category : categories) {
			this.totalMapCount += category.maps.size();
			this.uiElementCount += category.maps.size() + 1;
		}
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
	
	public TileMapService getTileMap(String mapId) {
		for(Category category : categories) {
			for(TileMapService map : category.getMaps()) {
				if(mapId.equals(map.getId())) {
					return map;
				}
			}
		}
		return null;
	}
	
	public void append(TileMapLoaderResult other) {
		this.categories.addAll(other.categories);
		this.totalMapCount += other.totalMapCount;
		this.uiElementCount += other.uiElementCount;
	}
	
	public static class Category implements SidebarDropdownCategory<TileMapService> {
		private final String name;
		private final List<TileMapService> maps;
		private boolean opened;
		
		public Category(String name, List<TileMapService> maps) {
			this.name = name;
			this.maps = maps;
		}

		@Override public String getName() { return name; }
		@Override public boolean isOpened() { return opened; }
		@Override public void setOpened(boolean opened) { this.opened = opened; }
		@Override public List<TileMapService> getItems() { return maps; }
		public List<TileMapService> getMaps() { return maps; }
	}
	
}
