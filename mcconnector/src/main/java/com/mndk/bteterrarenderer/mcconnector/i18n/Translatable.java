package com.mndk.bteterrarenderer.mcconnector.i18n;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@JsonSerialize(using = TranslatableSerializer.class)
@JsonDeserialize(using = TranslatableDeserializer.class)
public class Translatable<T> {
    public static final String DEFAULT_KEY = "en_us";

    @Getter(AccessLevel.PACKAGE)
    private final Map<String, T> translations;

    public T get() {
        return this.get(McConnector.common().i18nManager.getCurrentLanguage());
    }
    private T get(String language) {
        return Optional.ofNullable(translations.get(language)).orElse(translations.get(DEFAULT_KEY));
    }
    public <U> Translatable<U> map(Function<T, U> function) {
        Map<String, U> newMap = new HashMap<>();
        translations.forEach((key, value) -> newMap.put(key, function.apply(value)));
        return new Translatable<>(newMap);
    }

}
