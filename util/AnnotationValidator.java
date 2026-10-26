package util;

import model.annotation.MaxLength;
import model.annotation.Positive;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

// Practical 7: Reflection-based validator for @Positive and @MaxLength annotations
public class AnnotationValidator {

    public static String[] validate(Object obj) {
        List<String> errors = new ArrayList<>();
        if (obj == null) {
            return new String[0];
        }

        // Traverse class hierarchy so fields from superclasses (like Account) are validated
        Class<?> current = obj.getClass();
        while (current != null && current != Object.class) {
            Field[] fields = current.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);

                    // Check @Positive annotation
                    if (field.isAnnotationPresent(Positive.class)) {
                        Positive positive = field.getAnnotation(Positive.class);
                        if (value instanceof Number num) {
                            if (num.longValue() <= 0) {
                                errors.add(field.getName() + " " + positive.message());
                            }
                        }
                    }

                    // Check @MaxLength annotation
                    if (field.isAnnotationPresent(MaxLength.class)) {
                        MaxLength maxLength = field.getAnnotation(MaxLength.class);
                        if (value instanceof String str) {
                            if (str.length() > maxLength.value()) {
                                errors.add(field.getName() + " exceeds max length of " + maxLength.value());
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    errors.add("Unable to access field " + field.getName());
                }
            }
            current = current.getSuperclass();
        }

        return errors.toArray(new String[0]);
    }
}