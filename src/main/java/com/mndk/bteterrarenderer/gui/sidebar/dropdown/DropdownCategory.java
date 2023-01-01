package com.mndk.bteterrarenderer.gui.sidebar.dropdown;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class DropdownCategory<T extends DropdownCategoryElement> {

    @Getter private final String name;
    @Getter private final List<T> items;
    @Getter @Setter private boolean opened;

    public void addItem(T item) { items.add(item); }
    public void addItems(Collection<T> collection) { items.addAll(collection); }

}
