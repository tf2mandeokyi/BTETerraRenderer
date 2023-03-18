package com.mndk.bteterrarenderer.gui.sidebar.dropdown;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;

@NoArgsConstructor
public class DropdownCategory<T> extends LinkedHashMap<String, T> {
    @Getter @Setter
    private transient boolean opened = false;
}
