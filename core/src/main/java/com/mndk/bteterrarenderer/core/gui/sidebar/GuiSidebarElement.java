package com.mndk.bteterrarenderer.core.gui.sidebar;

import com.mndk.bteterrarenderer.core.gui.sidebar.button.SidebarBooleanButton;
import com.mndk.bteterrarenderer.mcconnector.gui.component.GuiComponentCopy;
import com.mndk.bteterrarenderer.core.gui.sidebar.input.SidebarNumberInput;
import com.mndk.bteterrarenderer.core.gui.sidebar.slider.SidebarSlider;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.mcconnector.i18n.I18nManager;
import lombok.AccessLevel;
import lombok.Getter;

public abstract class GuiSidebarElement extends GuiComponentCopy {

    public boolean hide = false;
    /**
     * Default, non-initialized value is -1
     */
    @Getter(AccessLevel.PROTECTED)
    private int width = -1;

    public abstract int getPhysicalHeight();
    /** This shouldn't return something less than {@link GuiSidebarElement#getPhysicalHeight()} */
    public int getVisualHeight() {
        return this.getPhysicalHeight();
    }

    public final void init(int width) {
        this.width = width;
        this.init();
    }
    public final void onWidthChange(int width) {
        this.width = width;
        this.onWidthChange();
    }

    protected abstract void init();
    public abstract void onWidthChange();
    /** Used to calculate Z-axis translation on component drawing step */
    public int getCount() { return 1; }

    protected static GuiSidebarElement fromProperty(PropertyAccessor.Localized<?> property) {
        Class<?> propertyClass = property.delegate.getPropertyClass();

        String localized = I18nManager.format(property.i18nKey);

        if(Number.class.isAssignableFrom(propertyClass)) {
            PropertyAccessor<Number> numberProperty = BTRUtil.uncheckedCast(property.delegate);
            if(numberProperty instanceof PropertyAccessor.Ranged) {
                PropertyAccessor.Ranged<Number> rangedNumberProperty = BTRUtil.uncheckedCast(numberProperty);
                return new SidebarSlider<>(rangedNumberProperty, localized + ": ", "");
            }

            PropertyAccessor<Double> propertyWrapper = PropertyAccessor.of(
                    () -> numberProperty.get().doubleValue(),
                    v  -> numberProperty.set(BTRUtil.doubleToNumber(numberProperty.getPropertyClass(), v))
            );
            return new SidebarNumberInput(propertyWrapper, localized);
        }
        else if(propertyClass == Boolean.class || propertyClass == boolean.class) {
            PropertyAccessor<Boolean> booleanProperty = BTRUtil.uncheckedCast(property.delegate);
            return new SidebarBooleanButton(booleanProperty, localized + ": ");
        }
        throw new RuntimeException("Unsupported property type: " + propertyClass);
    }
}

