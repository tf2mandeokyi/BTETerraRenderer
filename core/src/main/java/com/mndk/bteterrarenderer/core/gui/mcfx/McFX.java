package com.mndk.bteterrarenderer.core.gui.mcfx;

import com.mndk.bteterrarenderer.core.gui.mcfx.button.McFXBooleanButton;
import com.mndk.bteterrarenderer.core.gui.mcfx.button.McFXButton;
import com.mndk.bteterrarenderer.core.gui.mcfx.checkbox.McFXCheckBox;
import com.mndk.bteterrarenderer.core.gui.mcfx.dropdown.McFXDropdown;
import com.mndk.bteterrarenderer.core.gui.mcfx.input.McFXNumberInput;
import com.mndk.bteterrarenderer.core.gui.mcfx.slider.McFXSlider;
import com.mndk.bteterrarenderer.core.gui.mcfx.list.McFXHorizontalList;
import com.mndk.bteterrarenderer.core.gui.mcfx.list.McFXVerticalList;
import com.mndk.bteterrarenderer.core.gui.mcfx.wrapper.McFXWrapper;
import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.core.util.accessor.PropertyAccessor;
import com.mndk.bteterrarenderer.mcconnector.i18n.I18nManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.NativeTextureWrapper;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public class McFX {
    public McFXVerticalList vList(int elementDistance, int sidePadding) {
        return new McFXVerticalList(elementDistance, sidePadding, null, false);
    }

    public McFXVerticalList vList(int elementDistance, int sidePadding, boolean makeSound) {
        return new McFXVerticalList(elementDistance, sidePadding, null, makeSound);
    }

    public McFXVerticalList vList(int elementDistance, int sidePadding,
                                         @Nullable Supplier<Integer> maxHeight, boolean makeSound) {
        return new McFXVerticalList(elementDistance, sidePadding, maxHeight, makeSound);
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
        return new McFXButton(I18nManager.format(i18nKey), event);
    }

    public McFXBooleanButton i18nBoolButton(String i18nKey, PropertyAccessor<Boolean> value) {
        return new McFXBooleanButton(value, I18nManager.format(i18nKey) + ": ");
    }

    public <T extends Number> McFXSlider<T> i18nSlider(String i18nKey, PropertyAccessor.Ranged<T> value) {
        return new McFXSlider<>(value, I18nManager.format(i18nKey) + ": ", "");
    }

    public McFXCheckBox i18nCheckBox(String i18nKey, PropertyAccessor<Boolean> value) {
        return new McFXCheckBox(value, I18nManager.format(i18nKey));
    }

    public McFXNumberInput numberInput(String prefix, PropertyAccessor<Double> value) {
        return new McFXNumberInput(value, prefix);
    }

    public McFXNumberInput i18nNumberInput(String i18nKeyPrefix, PropertyAccessor<Double> value) {
        return new McFXNumberInput(value, I18nManager.format(i18nKeyPrefix) + ": ");
    }

    public <T> McFXDropdown<T> dropdown(PropertyAccessor<T> selectedValue,
                                        Function<T, String> nameGetter,
                                        Function<T, NativeTextureWrapper> iconTextureObjectGetter) {
        return new McFXDropdown<>(selectedValue, nameGetter, iconTextureObjectGetter);
    }

    public McFXElement fromProperty(PropertyAccessor.Localized<?> property) {
        Class<?> propertyClass = property.delegate.getPropertyClass();

        if(Number.class.isAssignableFrom(propertyClass)) {
            PropertyAccessor<Number> numberProperty = BTRUtil.uncheckedCast(property.delegate);
            if(numberProperty instanceof PropertyAccessor.Ranged) {
                PropertyAccessor.Ranged<Number> rangedNumberProperty = BTRUtil.uncheckedCast(numberProperty);
                return McFX.i18nSlider(property.i18nKey, rangedNumberProperty);
            }

            PropertyAccessor<Double> propertyWrapper = PropertyAccessor.of(
                    () -> numberProperty.get().doubleValue(),
                    v  -> numberProperty.set(BTRUtil.doubleToNumber(numberProperty.getPropertyClass(), v))
            );
            return McFX.i18nNumberInput(property.i18nKey, propertyWrapper);
        }
        else if(propertyClass == Boolean.class || propertyClass == boolean.class) {
            PropertyAccessor<Boolean> booleanProperty = BTRUtil.uncheckedCast(property.delegate);
            return McFX.i18nBoolButton(property.i18nKey, booleanProperty);
        }
        throw new RuntimeException("Unsupported property type: " + propertyClass);
    }
}
