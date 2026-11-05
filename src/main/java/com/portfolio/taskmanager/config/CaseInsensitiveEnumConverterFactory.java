package com.portfolio.taskmanager.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

@Component
public class CaseInsensitiveEnumConverterFactory implements ConverterFactory<String, Enum> {

    @Override
    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter<>(targetType);
    }

    private static class StringToEnumConverter<T extends Enum> implements Converter<String, T> {

        private final Class<T> enumType;

        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String source) {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }
            String formatted = source.trim().replace('-', '_').toUpperCase();
            for (T constant : enumType.getEnumConstants()) {
                if (constant.name().equalsIgnoreCase(formatted)) {
                    return constant;
                }
            }
            throw new IllegalArgumentException("Không tìm thấy giá trị enum hợp lệ cho: '" + source + "'");
        }
    }
}

