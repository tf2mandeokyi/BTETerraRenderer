package com.mndk.bteterrarenderer.core.gui.sidebar.mapaligner;

import com.mndk.bteterrarenderer.core.gui.sidebar.checkbox.SidebarCheckBox;
import com.mndk.bteterrarenderer.core.gui.sidebar.decorator.SidebarBlank;
import com.mndk.bteterrarenderer.core.gui.sidebar.input.SidebarNumberInput;
import com.mndk.bteterrarenderer.core.gui.sidebar.wrapper.SidebarElementHorizontalList;
import com.mndk.bteterrarenderer.core.gui.sidebar.wrapper.SidebarElementVerticalList;
import com.mndk.bteterrarenderer.core.gui.sidebar.wrapper.SidebarElementWrapper;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.mcconnector.client.MinecraftClientManager;
import com.mndk.bteterrarenderer.mcconnector.i18n.I18nManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;

public class SidebarMapAligner extends SidebarElementVerticalList {

    static final int MARKER_COLOR = 0xFFFF0000;

    private final PropertyAccessor<Double> xOffset, zOffset;
    private final PropertyAccessor<Boolean> lockNorth;
    private final AlignerBox alignBox;

    private SidebarNumberInput xInput, zInput;

    public SidebarMapAligner(PropertyAccessor<Double> xOffset, PropertyAccessor<Double> zOffset,
                             PropertyAccessor<Boolean> lockNorth) {
        super(0, 0, null, false);
        this.xOffset = xOffset;
        this.zOffset = zOffset;
        this.lockNorth = lockNorth;
        this.alignBox = new AlignerBox(150, xOffset, zOffset, () -> {
            xInput.update();
            zInput.update();
        });
    }

    @Override
    public void init() {
        this.clear();

        this.xInput = new SidebarNumberInput(xOffset, "X: ");
        this.zInput = new SidebarNumberInput(zOffset, "Z: ");

        SidebarElementHorizontalList hList = new SidebarElementHorizontalList(0, false);
        hList.add(this.xInput, null);
        hList.add(new SidebarBlank(0), (totalWidth, widthLeft) -> 6);
        hList.add(this.zInput, null);

        PropertyAccessor<Boolean> lockNorthWrapper = PropertyAccessor.of(
                this.lockNorth::get,
                value -> { lockNorth.set(value); this.updatePlayerYawRadians(); }
        );

        SidebarCheckBox checkBox = new SidebarCheckBox(lockNorthWrapper, I18nManager.format("gui.bteterrarenderer.settings.lock_north"));
        this.addAll(
                hList,
                new SidebarElementWrapper(this.alignBox, 20, 15, 15, 15),
                new SidebarElementWrapper(checkBox, 0, 15, 0, 15)
        );

        this.xInput.setPrefixColor(MARKER_COLOR);
        this.zInput.setPrefixColor(MARKER_COLOR);
    }

    private void updatePlayerYawRadians() {
        this.alignBox.setPlayerYawRadians(lockNorth.get() ?
                Math.PI :
                Math.toRadians(MinecraftClientManager.getPlayerRotationYaw())
        );
    }

    @Override
    public void drawComponent(DrawContextWrapper<?> drawContextWrapper) {
        this.updatePlayerYawRadians();
        super.drawComponent(drawContextWrapper);
    }
}
