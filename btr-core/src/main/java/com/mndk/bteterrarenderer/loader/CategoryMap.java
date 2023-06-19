package com.mndk.bteterrarenderer.loader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(using = CategoryMap.Deserializer.class)
public class CategoryMap<T> extends LinkedHashMap<String, CategoryMap.Category<T>> {

	public Wrapper<T> getWrappedItem(String categoryName, String mapId) {
		Category<T> category = this.get(categoryName);
		if(category == null) return null;
		return category.get(mapId);
	}

	public void setSource(String source) {
		for(Category<T> category : this.values()) {
			category.setSource(source);
		}
	}
	
	public void append(CategoryMap<T> other) {
		for(Map.Entry<String, Category<T>> otherCategoryEntry : other.entrySet()) {
			String otherCategoryName = otherCategoryEntry.getKey();
			Category<T> otherCategoryObject = otherCategoryEntry.getValue();

			Category<T> existingCategory = this.get(otherCategoryName);
			if(existingCategory != null) {
				existingCategory.putAll(otherCategoryObject);
			}
			else {
				this.put(otherCategoryName, otherCategoryObject);
			}
		}
	}

	@Getter @Setter @RequiredArgsConstructor
	public static class Category<T> extends LinkedHashMap<String, Wrapper<T>> {
		private final String name;

		public void setSource(String source) {
			for(Wrapper<T> wrapped : this.values()) {
				wrapped.source = source;
			}
		}
	}

	@Getter
	public static class Wrapper<T> {
		final Category<T> parentCategory;
		private String source;
		final T value;
		final String id;
		private Wrapper(Category<T> parentCategory, String id, T value) {
			this.parentCategory = parentCategory;
			this.id = id;
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(o == null || getClass() != o.getClass()) return false;

			Wrapper<?> wrapper = (Wrapper<?>) o;
			if(!Objects.equals(source, wrapper.source)) return false;
			if(!Objects.equals(parentCategory.name, wrapper.parentCategory.name)) return false;
			return Objects.equals(id, wrapper.id);
		}
	}

	public static class Deserializer extends JsonDeserializer<CategoryMap<?>> implements ContextualDeserializer {
		private JavaType valueType;

		@Override
		public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
			JavaType categoryMapType = property != null ? property.getType() : ctxt.getContextualType();
			JavaType valueType = categoryMapType.containedType(0);
			Deserializer deserializer = new Deserializer();
			deserializer.valueType = valueType;
			return deserializer;
		}

		@Override
		public CategoryMap<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			JsonNode node = ctxt.readTree(p);
			if(!node.has("categories") || !node.get("categories").isObject())
				throw JsonMappingException.from(p, "\"categories\" field not found, or is not an object");

			CategoryMap<Object> result = new CategoryMap<>();
			for (Iterator<Map.Entry<String, JsonNode>> categoryIt = node.get("categories").fields(); categoryIt.hasNext(); ) {
				Map.Entry<String, JsonNode> categoryEntry = categoryIt.next();
				String categoryName = categoryEntry.getKey();
				JsonNode categoryNode = categoryEntry.getValue();

				if(!categoryNode.isObject())
					throw JsonMappingException.from(p, "category should be an object");

				Category<Object> category = new Category<>(categoryName);
				for (Iterator<Map.Entry<String, JsonNode>> it = categoryNode.fields(); it.hasNext(); ) {
					Map.Entry<String, JsonNode> valueEntry = it.next();
					String valueId = valueEntry.getKey();
					Object valueObject = ctxt.readTreeAsValue(valueEntry.getValue(), this.valueType);

					Wrapper<Object> wrapped = new Wrapper<>(category, valueId, valueObject);
					category.put(valueId, wrapped);
				}

				result.put(categoryName, category);
			}

			return result;
		}
	}

}
