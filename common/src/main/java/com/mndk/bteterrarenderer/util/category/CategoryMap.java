package com.mndk.bteterrarenderer.util.category;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mndk.bteterrarenderer.util.function.ThrowableBiConsumer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@JsonSerialize(using = CategoryMapSerializer.class)
@JsonDeserialize(using = CategoryMapDeserializer.class)
public class CategoryMap<T> {

	@Getter(AccessLevel.PACKAGE)
	private final Map<String, Category<T>> map = new LinkedHashMap<>();

	public void forEach(BiConsumer<String, Category<T>> consumer) {
		map.forEach(consumer);
	}

	public <E extends Throwable> void forEachThrowable(ThrowableBiConsumer<String, Category<T>, E> consumer) throws E {
		for (Map.Entry<String, Category<T>> entry : map.entrySet()) {
			consumer.accept(entry.getKey(), entry.getValue());
		}
	}

	@Nullable
	public T getItem(String categoryName, String elementId) {
		Category<T> category = map.get(categoryName);
		if (category == null) return null;
		return category.get(elementId);
	}

	public void setItem(String categoryName, String elementId, @Nonnull T item) {
		map.computeIfAbsent(categoryName, n -> new Category<>()).put(elementId, item);
	}

	@Getter
	@RequiredArgsConstructor
	public static class Entry<T> {
		private final String categoryName;
		private final T value;
	}

}
