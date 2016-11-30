package framework.core.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BeanUtils {
    public  static  <T> T copy(T entity) throws IllegalAccessException, InstantiationException {
        Class<?> clazz = entity.getClass();
        T newEntity = (T) entity.getClass().newInstance();

        while (clazz != null) {
            copyFields(entity, newEntity, clazz);
            clazz = clazz.getSuperclass();
        }

        return newEntity;
    }

    private static  <T> T copyFields(T entity, T newEntity, Class<?> clazz) throws IllegalAccessException {
        List<Field> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            fields.add(field);
        }
        for (Field field : fields) {
            field.setAccessible(true);
            field.set(newEntity, field.get(entity));
        }
        return newEntity;
    }
}
