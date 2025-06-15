package com.mndk.bteterrarenderer.mcconnector.client.mcfx;

import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.gui.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.button.McFXBooleanButton;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.button.McFXButton;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.checkbox.McFXCheckBox;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.dropdown.McFXDropdown;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.image.McFXImage;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.input.McFXNumberInput;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.list.McFXHorizontalList;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.list.McFXVerticalList;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.slider.McFXSlider;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.wrapper.McFXScreenWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.wrapper.McFXWrapper;
import com.mndk.bteterrarenderer.util.BTRUtil;
import com.mndk.bteterrarenderer.util.accessor.PropertyAccessor;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.Range;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public class McFX {
    public McFXVerticalList vList(int elementDistance, int sidePadding) {
        return new McFXVerticalList(elementDistance, sidePadding, null, false);
    }

    public McFXVerticalList vList(int elementDistance, int sidePadding,
                                  @Nullable Supplier<Integer> maxHeight, boolean makeSound) {
        return new McFXVerticalList(elementDistance, sidePadding, maxHeight, makeSound);
    }

    public McFXHorizontalList hList() {
        return new McFXHorizontalList(0, false);
    }

    public McFXHorizontalList hList(int sidePadding, boolean makeSound) {
        return new McFXHorizontalList(sidePadding, makeSound);
    }

    public McFXWrapper wrapper() {
        return new McFXWrapper(null);
    }

    public McFXWrapper wrapper(McFXElement element) {
        return new McFXWrapper(element);
    }

    public McFXScreenWrapper screenWrapper(Supplier<Integer> height) {
        return new McFXScreenWrapper(height);
    }

    public McFXElement div() {
        return new McFXElement() {
            protected void init() {}
            protected void onWidthChange() {}
            protected void drawElement(GuiDrawContextWrapper drawContextWrapper) {}
        };
    }

    public McFXElement div(int height) {
        return new McFXElement() {
            public int getPhysicalHeight() {
                return height;
            }
            protected void init() {}
            protected void onWidthChange() {}
            protected void drawElement(GuiDrawContextWrapper drawContextWrapper) {}
        };
    }

    public McFXElement div(Supplier<Integer> height) {
        return new McFXElement() {
            public int getPhysicalHeight() {
                return height.get();
            }
            protected void init() {}
            protected void onWidthChange() {}
            protected void drawElement(GuiDrawContextWrapper drawContextWrapper) {}
        };
    }

    public McFXButton button(String text, McFXButton.MouseClickedEvent event) {
        return new McFXButton(text, event);
    }

    public McFXImage image() {
        return new McFXImage();
    }

    public McFXButton i18nButton(String i18nKey, McFXButton.MouseClickedEvent event) {
        return new McFXButton(McConnector.client().i18nManager.format(i18nKey), event);
    }

    public McFXBooleanButton i18nBoolButton(String i18nKey, PropertyAccessor<Boolean> value) {
        return new McFXBooleanButton(value, McConnector.client().i18nManager.format(i18nKey) + ": ");
    }

    public <T extends Number> McFXSlider<T> i18nSlider(String i18nKey, PropertyAccessor<T> value) {
        return new McFXSlider<>(value, McConnector.client().i18nManager.format(i18nKey) + ": ", "");
    }

    public McFXCheckBox i18nCheckBox(String i18nKey, PropertyAccessor<Boolean> value) {
        return new McFXCheckBox(value, McConnector.client().i18nManager.format(i18nKey));
    }

    public McFXNumberInput numberInput(String prefix, PropertyAccessor<Double> value) {
        return new McFXNumberInput(value, prefix);
    }

    public McFXNumberInput i18nNumberInput(String i18nKeyPrefix, PropertyAccessor<Double> value) {
        return new McFXNumberInput(value, McConnector.client().i18nManager.format(i18nKeyPrefix) + ": ");
    }

    public McFXDropdown dropdown(PropertyAccessor<String[]> categoryPathAccessor,
                                 Function<String[], String> nameGetter,
                                 Function<String[], NativeTextureWrapper> iconTextureObjectGetter) {
        return new McFXDropdown(categoryPathAccessor, nameGetter, iconTextureObjectGetter);
    }

    public McFXElement fromPropertyAccessor(PropertyAccessor.Localized<?> property) {
        Class<?> propertyClass = property.getPropertyClass();

        boolean numberAssignable = propertyClass == Double.class || propertyClass == double.class ||
                propertyClass == Float.class || propertyClass == float.class ||
                propertyClass == Long.class || propertyClass == long.class ||
                propertyClass == Integer.class || propertyClass == int.class ||
                propertyClass == Short.class || propertyClass == short.class ||
                propertyClass == Byte.class || propertyClass == byte.class;
        if (numberAssignable) {
            PropertyAccessor<Number> numberProperty = BTRUtil.uncheckedCast(property);
            Range<Number> range = numberProperty.getRange();
            if (range != null) {
                return McFX.i18nSlider(property.getI18nKey(), numberProperty);
            }

            PropertyAccessor<Double> propertyWrapper = PropertyAccessor.of(
                    () -> numberProperty.get().doubleValue(),
                    v  -> numberProperty.set(BTRUtil.doubleToNumber(numberProperty.getPropertyClass(), v))
            );
            return McFX.i18nNumberInput(property.getI18nKey(), propertyWrapper);
        }
        else if (propertyClass == Boolean.class || propertyClass == boolean.class) {
            return McFX.i18nBoolButton(property.getI18nKey(), BTRUtil.uncheckedCast(property));
        }
        throw new RuntimeException("Unsupported property type: " + propertyClass);
    }
}
