package com.mndk.bteterrarenderer.util.merge;

import com.mndk.bteterrarenderer.util.category.CategoryMap;

/**
 * MergeStrategy for CategoryMap that appends entries.
 */
public class CategoryMapMergeStrategy<V> implements MergeStrategy<CategoryMap<V>> {
    @Override
    public void merge(CategoryMap<V> original, CategoryMap<V> addition) {
        // merge each item from addition into original without using deprecated append()
        addition.forEach((categoryName, category) ->
                category.forEach((id, value) -> original.setItem(categoryName, id, value))
        );
    }
}
