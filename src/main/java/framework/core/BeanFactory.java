package framework.core;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public interface BeanFactory {
    
    public class PrimitiveWrapperPair {
        public Class<?> primitiveTypeClassObject;
        public Class<?> wrapperClassObject;
        
        PrimitiveWrapperPair(Class<?> primitiveTypeClassObject, Class<?> wrapperClassObject) {
            this.primitiveTypeClassObject = primitiveTypeClassObject;
            this.wrapperClassObject = wrapperClassObject;
        }
    }
    
    HashMap<String, PrimitiveWrapperPair> classLibrary = new HashMap<String, PrimitiveWrapperPair>() {
        private static final long serialVersionUID = 1L;

        {
            put("boolean", new PrimitiveWrapperPair(boolean.class, Boolean.class));
            put("byte", new PrimitiveWrapperPair(byte.class, Byte.class));
            put("short", new PrimitiveWrapperPair(short.class, Short.class));
            put("int", new PrimitiveWrapperPair(int.class, Integer.class));
            put("long", new PrimitiveWrapperPair(long.class, Long.class));
            put("float", new PrimitiveWrapperPair(float.class, Float.class));
            put("double", new PrimitiveWrapperPair(double.class, Double.class));
            put("char", new PrimitiveWrapperPair(char.class, Character.class));
            put("void", new PrimitiveWrapperPair(void.class, Void.class));
            //From the Java language specification PoV, 'void' is not a primitive type.
            //Still, void.class.isPrimitive() returns true,
            //so it is included here to cover this option 
        }
        
    };
    
    Object getBean(String string);
    <T extends Object> T getBean(String string, Class<T> type);
    Object[] getInterceptors();
    
    default Class<?> getPrimitiveClassForName(String primitiveTypeName) {
        Class<?> res = null;

        if (classLibrary.containsKey(primitiveTypeName)) {
            res = classLibrary.get(primitiveTypeName).primitiveTypeClassObject;
        }
        
        return res;
    }
    
    default Object getWrapperClassValueForPrimitiveType(Class<?> primitiveType, String stringValue) {
        Object res = null;
        
        try {
            res = classLibrary.get(primitiveType.getName()).wrapperClassObject
                    .getMethod("valueOf", String.class).invoke(null, stringValue);
        } catch (IllegalAccessException | IllegalArgumentException 
                | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            e.printStackTrace();
        }        

        return res;
    }
}
