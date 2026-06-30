package com.dev.ultron.utilitarios;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.lang.reflect.Field;

public class UppercaseEntityListener {

    @PrePersist
    @PreUpdate
    public void convertStringsToUppercase(Object entity) {
        if (entity == null) {
            return;
        }

        Class<?> clazz = entity.getClass();
        while (clazz != null && clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType().equals(String.class)) {
                    field.setAccessible(true);
                    try {
                        String value = (String) field.get(entity);
                        if (value != null) {
                            field.set(entity, value.toUpperCase());
                        }
                    } catch (IllegalAccessException e) {
                        // ignore or log
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
}
