package service;

import model.annotation.Id;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Practical 13: Generic Repository<T> with reflection for @Id, CRUD operations, @SafeVarargs, and bounded type parameter
public class Repository<T> {

    private final Map<String, T> storage = new HashMap<>();

    // Practical 13 requirement: save(T entity) using reflection to find field marked @Id
    public T save(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Cannot save null entity");
        }

        String id = extractId(entity);
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Extracted @Id value cannot be null or empty");
        }

        storage.put(id, entity);
        return entity;
    }

    // Practical 13 requirement: findById(String id)
    public T findById(String id) {
        if (id == null) return null;
        return storage.get(id);
    }

    // Practical 13 requirement: findAll() returning ArrayList<T>
    public ArrayList<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    // Practical 13 requirement: delete(String id) returning boolean
    public boolean delete(String id) {
        if (id == null) return false;
        return storage.remove(id) != null;
    }

    // Supplementary: count() returning total items
    public int count() {
        return storage.size();
    }

    // Practical 13 requirement: @SafeVarargs saveAll(T... items)
    @SafeVarargs
    public final void saveAll(T... items) {
        if (items != null) {
            for (T item : items) {
                if (item != null) {
                    save(item);
                }
            }
        }
    }

    // Practical 13 requirement: generic method with a bounded type parameter
    public static <N extends Number> double average(N[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Values array cannot be null or empty");
        }
        double sum = 0.0;
        for (N val : values) {
            if (val != null) {
                sum += val.doubleValue();
            }
        }
        return sum / values.length;
    }

    // Reflection helper to locate field annotated with @Id across class hierarchy
    private String extractId(T entity) {
        Class<?> current = entity.getClass();
        while (current != null && current != Object.class) {
            Field[] fields = current.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    try {
                        Object val = field.get(entity);
                        return val != null ? val.toString() : null;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to access @Id field: " + field.getName(), e);
                    }
                }
            }
            current = current.getSuperclass();
        }
        throw new IllegalArgumentException("No field annotated with @Id found in " + entity.getClass().getName());
    }
}
