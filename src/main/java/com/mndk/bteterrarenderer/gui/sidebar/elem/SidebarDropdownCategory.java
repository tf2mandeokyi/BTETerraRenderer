package com.mndk.bteterrarenderer.gui.sidebar.elem;

import java.util.List;

public interface SidebarDropdownCategory<T> {
    String getName();
    boolean isOpened();
    void setOpened(boolean opened);
    List<T> getItems();
}
