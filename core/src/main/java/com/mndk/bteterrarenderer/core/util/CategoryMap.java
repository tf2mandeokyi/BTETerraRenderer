package com.mndk.bteterrarenderer.core.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

@JsonSerialize(using = CategoryMap.Serializer.class)
@JsonDeserialize(using = CategoryMap.Deserializer.class)
public class CategoryMap<T> {

	private final Map<String, Category<T>> map = new LinkedHashMap<>();

	public Category<T> getCategory(String categoryName) {
		return map.get(categoryName);
	}

	public Set<Map.Entry<String, Category<T>>> getCategories() {
		return map.entrySet();
	}

	/**
	 * @return A wrapper.<br>If the category is not found, all properties of the returned wrapper should be {@code null}.
	 * <br> If the element is not found, only the id and the value of the wrapper should be {@code null}.
	 */
	@Nonnull
	public Wrapper<T> getItemWrapper(String categoryName, String elementId) {
		Category<T> category = map.get(categoryName);
		if(category == null) return new Wrapper<>(null, null, null);
		Wrapper<T> wrapped = category.get(elementId);
		return wrapped != null ? wrapped : new Wrapper<>(category, null, null);
	}

	@Nullable
	public T getItem(String categoryName, String elementId) {
		return this.getItemWrapper(categoryName, elementId).getItem();
	}

	public void setItem(String categoryName, String elementId, @Nonnull T item) {
		Category<T> category = map.computeIfAbsent(categoryName, Category::new);
		category.put(elementId, new Wrapper<>(category, elementId, item));
	}

	public Set<Wrapper<T>> getItemWrappers() {
		Set<Wrapper<T>> result = new HashSet<>();
        map.forEach((key, value) -> result.addAll(value.values()));
		return result;
	}

	public void setSource(String source) {
		for(Category<T> category : map.values()) {
			category.setSource(source);
		}
	}
	
	public void append(CategoryMap<T> other) {
        other.map.forEach((otherCategoryName, otherCategoryObject) -> {
            Category<T> existingCategory = map.get(otherCategoryName);
            if (existingCategory != null) {
                existingCategory.putAll(otherCategoryObject);
            } else {
                map.put(otherCategoryName, otherCategoryObject);
            }
        });
	}

	@Getter
	@JsonIgnoreProperties
	@RequiredArgsConstructor
	public static class Category<T> extends LinkedHashMap<String, Wrapper<T>> {
		private final String name;
		public void setSource(String source) {
			this.values().forEach(wrapped -> wrapped.source = source);
		}
	}

	@Getter
	@JsonIgnoreProperties
	public static class Wrapper<T> {
		private final Category<T> parentCategory;
		private String source;
		private final T item;
		private final String id;
		private Wrapper(Category<T> parentCategory, String id, T item) {
			this.parentCategory = parentCategory;
			this.id = id;
			this.item = item;
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

	static class Serializer extends JsonSerializer<CategoryMap<Object>> {
		@Override
		public void serialize(CategoryMap<Object> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
			gen.writeStartObject(); // main

			for(Map.Entry<String, Category<Object>> categoryEntry : value.getCategories()) {
				gen.writeFieldName(categoryEntry.getKey());
				gen.writeStartObject();

				for(Map.Entry<String, Wrapper<Object>> wrapperEntry : categoryEntry.getValue().entrySet()) {
					gen.writeFieldName(wrapperEntry.getKey());
					gen.writeObject(wrapperEntry.getValue().item);
				}
				gen.writeEndObject();
			}

			gen.writeEndObject(); // main
		}
	}

	static class Deserializer extends JsonDeserializer<CategoryMap<?>> implements ContextualDeserializer {
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
			if (p.currentToken() == JsonToken.START_OBJECT)
				p.nextToken();

			JsonNode node = ctxt.readTree(p);
			CategoryMap<Object> result = new CategoryMap<>();

			for(Iterator<Map.Entry<String, JsonNode>> categoryIt = node.fields(); categoryIt.hasNext(); ) {
				Map.Entry<String, JsonNode> categoryEntry = categoryIt.next();
				String categoryName = categoryEntry.getKey();
				JsonNode categoryNode = categoryEntry.getValue();

				if(!categoryNode.isObject())
					throw JsonMappingException.from(p, "category should be an object");

				Category<Object> category = new Category<>(categoryName);
				for(Iterator<Map.Entry<String, JsonNode>> it = categoryNode.fields(); it.hasNext(); ) {
					Map.Entry<String, JsonNode> valueEntry = it.next();
					String valueId = valueEntry.getKey();
					Object valueObject = ctxt.readTreeAsValue(valueEntry.getValue(), this.valueType);

					Wrapper<Object> wrapped = new Wrapper<>(category, valueId, valueObject);
					category.put(valueId, wrapped);
				}

				result.map.put(categoryName, category);
			}

			return result;
		}
	}

}
