package com.mndk.bteterrarenderer.mcconnector.client.mcfx;

import com.mndk.bteterrarenderer.mcconnector.client.mcfx.button.McFXBooleanButton;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.button.McFXButton;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.checkbox.McFXCheckBox;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.dropdown.McFXDropdown;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.input.McFXNumberInput;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.list.McFXHorizontalList;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.list.McFXVerticalList;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.slider.McFXSlider;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.wrapper.McFXScreenWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.mcfx.wrapper.McFXWrapper;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapper;
import lombok.experimental.UtilityClass;

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
            public void onWidthChange() {}
            protected void drawElement(DrawContextWrapper<?> drawContextWrapper) {}
        };
    }

    public McFXElement div(int height) {
        return new McFXElement() {
            public int getPhysicalHeight() {
                return height;
            }
            protected void init() {}
            public void onWidthChange() {}
            protected void drawElement(DrawContextWrapper<?> drawContextWrapper) {}
        };
    }

    public McFXElement div(Supplier<Integer> height) {
        return new McFXElement() {
            public int getPhysicalHeight() {
                return height.get();
            }
            protected void init() {}
            public void onWidthChange() {}
            protected void drawElement(DrawContextWrapper<?> drawContextWrapper) {}
        };
    }

    public McFXButton button(String text, McFXButton.MouseClickedEvent event) {
        return new McFXButton(text, event);
    }

    public McFXButton i18nButton(String i18nKey, McFXButton.MouseClickedEvent event) {
        return new McFXButton(McConnector.client().i18nManager.format(i18nKey), event);
    }

    public McFXBooleanButton i18nBoolButton(String i18nKey, PropertyAccessor<Boolean> value) {
        return new McFXBooleanButton(value, McConnector.client().i18nManager.format(i18nKey) + ": ");
    }

    public <T extends Number> McFXSlider<T> i18nSlider(String i18nKey, PropertyAccessor.Ranged<T> value) {
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

    public <T> McFXDropdown<T> dropdown(PropertyAccessor<T> selectedValue,
                                        Function<T, String> nameGetter,
                                        Function<T, NativeTextureWrapper> iconTextureObjectGetter) {
        return new McFXDropdown<>(selectedValue, nameGetter, iconTextureObjectGetter);
    }

    public McFXElement fromProperty(PropertyAccessor.Localized<?> property) {
        Class<?> propertyClass = property.getPropertyClass();

        if(Number.class.isAssignableFrom(propertyClass)) {
            PropertyAccessor<Number> numberProperty = BTRUtil.uncheckedCast(property);
            if(numberProperty instanceof PropertyAccessor.Ranged) {
                PropertyAccessor.Ranged<Number> rangedNumberProperty = BTRUtil.uncheckedCast(numberProperty);
                return McFX.i18nSlider(property.getI18nKey(), rangedNumberProperty);
            }

            PropertyAccessor<Double> propertyWrapper = PropertyAccessor.of(
                    () -> numberProperty.get().doubleValue(),
                    v  -> numberProperty.set(BTRUtil.doubleToNumber(numberProperty.getPropertyClass(), v))
            );
            return McFX.i18nNumberInput(property.getI18nKey(), propertyWrapper);
        }
        else if(propertyClass == Boolean.class || propertyClass == boolean.class) {
            return McFX.i18nBoolButton(property.getI18nKey(), BTRUtil.uncheckedCast(property));
        }
        throw new RuntimeException("Unsupported property type: " + propertyClass);
    }
}
