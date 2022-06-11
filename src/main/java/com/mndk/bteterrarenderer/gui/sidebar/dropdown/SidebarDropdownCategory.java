package com.mndk.bteterrarenderer.gui.sidebar.dropdown;

import java.util.List;

public interface SidebarDropdownCategory<T> {
    String getName();
    boolean isOpened();
    void setOpened(boolean opened);
    List<T> getItems();
}
