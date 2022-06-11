package com.mndk.bteterrarenderer.tile;

import com.mndk.bteterrarenderer.gui.sidebar.dropdown.SidebarDropdownCategory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TMSLoaderResult {
	
	private final List<Category> categories;
	private int totalMapCount;
	
	public TMSLoaderResult() {
		this.categories = new ArrayList<>();
		this.totalMapCount = 0;
	}
	
	public TMSLoaderResult(List<Category> categories) {
		this.categories = categories;
		this.totalMapCount = 0;
		for(Category category : categories) {
			this.totalMapCount += category.maps.size();
		}
	}

	public Category getCategory(String categoryName) {
		for(Category category : categories) {
			if(categoryName.equals(category.getName())) {
				return category;
			}
		}
		return null;
	}

	public List<Category> getCategories() {
		return this.categories;
	}
	
	public int getTotalMapCount() {
		return this.totalMapCount;
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
	
	public void append(TMSLoaderResult other) {
		for(Category category : other.categories) {
			Category existingCategory = getCategory(category.name);
			if(existingCategory != null) {
				existingCategory.addItems(category.maps);
			}
			else {
				this.categories.add(category);
			}
		}
		this.totalMapCount += other.totalMapCount;
	}

	@RequiredArgsConstructor
	public static class Category implements SidebarDropdownCategory<TileMapService> {
		@Getter private final String name;
		@Getter private final List<TileMapService> maps;
		@Getter @Setter private boolean opened;

		@Override public List<TileMapService> getItems() { return maps; }
		public void addItem(TileMapService tms) { maps.add(tms); }
		public void addItems(Collection<TileMapService> tms) { maps.addAll(tms); }
	}
	
}
