package com.mndk.bteterrarenderer.core.gui.mapaligner;

import com.mndk.bteterrarenderer.core.gui.mcfx.McFX;
import com.mndk.bteterrarenderer.core.gui.mcfx.checkbox.McFXCheckBox;
import com.mndk.bteterrarenderer.core.gui.mcfx.input.McFXNumberInput;
import com.mndk.bteterrarenderer.core.gui.mcfx.list.McFXHorizontalList;
import com.mndk.bteterrarenderer.core.gui.mcfx.list.McFXVerticalList;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;

public class MapAligner extends McFXVerticalList {

    static final int MARKER_COLOR = 0xFFFF0000;

    private final PropertyAccessor<Double> xOffset, zOffset;
    private final PropertyAccessor<Boolean> lockNorth;
    private final MapAlignerBox alignBox;

    private McFXNumberInput xInput, zInput;

    public MapAligner(PropertyAccessor<Double> xOffset, PropertyAccessor<Double> zOffset,
                      PropertyAccessor<Boolean> lockNorth) {
        super(0, 0, null, false);
        this.xOffset = xOffset;
        this.zOffset = zOffset;
        this.lockNorth = lockNorth;
        this.alignBox = new MapAlignerBox(150, xOffset, zOffset, () -> {
            xInput.update();
            zInput.update();
        });
    }

    @Override
    public void init() {
        this.clear();

        this.xInput = McFX.numberInput("X: ", xOffset);
        this.zInput = McFX.numberInput("Z: ", zOffset);

        McFXHorizontalList hList = McFX.hList(0, false)
                .add(this.xInput, null)
                .add(McFX.div(0), (totalWidth, widthLeft) -> 6)
                .add(this.zInput, null);

        PropertyAccessor<Boolean> lockNorthWrapper = PropertyAccessor.of(
                this.lockNorth::get,
                value -> { lockNorth.set(value); this.updatePlayerYawRadians(); }
        );

        McFXCheckBox checkBox = McFX.i18nCheckBox("gui.bteterrarenderer.settings.lock_north", lockNorthWrapper);
        this.addAll(
                hList,
                McFX.wrapper(this.alignBox).setPadding(20, 15, 15, 15),
                McFX.wrapper(checkBox).setPadding(0, 15, 0, 15)
        );

        this.xInput.setPrefixColor(MARKER_COLOR);
        this.zInput.setPrefixColor(MARKER_COLOR);
    }

    private void updatePlayerYawRadians() {
        this.alignBox.setPlayerYawRadians(lockNorth.get() ?
                Math.PI :
                Math.toRadians(McConnector.client().getPlayerRotationYaw())
        );
    }

    @Override
    public void drawElement(DrawContextWrapper<?> drawContextWrapper) {
        this.updatePlayerYawRadians();
        super.drawElement(drawContextWrapper);
    }
}
