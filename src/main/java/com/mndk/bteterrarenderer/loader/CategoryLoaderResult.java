package com.mndk.bteterrarenderer.loader;

import com.mndk.bteterrarenderer.gui.sidebar.dropdown.DropdownCategory;
import com.mndk.bteterrarenderer.gui.sidebar.dropdown.DropdownCategoryElement;
import lombok.Getter;

import java.util.List;

public class CategoryLoaderResult<T extends DropdownCategoryElement> {
	
	@Getter private final List<DropdownCategory<T>> data;
	@Getter private int totalMapCount;
	
	public CategoryLoaderResult(List<DropdownCategory<T>> data) {
		this.data = data;
		this.totalMapCount = 0;
		for(DropdownCategory<T> category : data) {
			this.totalMapCount += category.getItems().size();
		}
	}

	public DropdownCategory<T> getCategory(String categoryName) {
		for(DropdownCategory<T> category : data) {
			if(categoryName.equals(category.getName())) {
				return category;
			}
		}
		return null;
	}

	public T getTileMap(String mapId) {
		for(DropdownCategory<T> category : data) {
			for(T map : category.getItems()) {
				if(mapId.equals(map.getId())) {
					return map;
				}
			}
		}
		return null;
	}
	
	public void append(CategoryLoaderResult<T> other) {
		for(DropdownCategory<T> category : other.data) {
			DropdownCategory<T> existingCategory = getCategory(category.getName());
			if(existingCategory != null) {
				existingCategory.addItems(category.getItems());
			}
			else {
				this.data.add(category);
			}
		}
		this.totalMapCount += other.totalMapCount;
	}
	
}
