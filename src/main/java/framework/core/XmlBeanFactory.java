package framework.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import framework.parsers.Bean;

public class XmlBeanFactory implements BeanFactory {
    
    HashMap<String, Object> beanTable = new HashMap<String, Object>();
    HashMap<String, Object> interceptorTable = new HashMap<String, Object>();
    
    XmlBeanFactory(String xmlFilePath, XmlBeanDefinitionReader xbdr) {        
        xbdr.loadBeanDefinitions(xmlFilePath);        
        generateBeans(xbdr.getBeanList());
        setupInterceptors(xbdr.getInterceptorList());
    }
    
    private void generateBeans(List<Bean> beanList) {                
        for (Bean b : beanList) {
            try {
                final Class<?> clazz = Class.forName(b.getClassName());
                Constructor<?> ctor;
                Object object;
                
                List<String> ca = b.getConstructorArg();
                
                if (!ca.isEmpty()) {
                    
                    Class<?>[] consClasses = new Class[ca.size() / 2];
                    
                    for (int i = 0, j = 0; i < ca.size(); i+=2) {
                        if (ca.get(i) == null || ca.get(i).contentEquals("String")) {
                            consClasses[j] = String.class;
                        } else if (classLibrary.containsKey(ca.get(i))) { 
                            consClasses[j] = getPrimitiveClassForName(ca.get(i));
                        } else {
                            consClasses[j] = Class.forName(ca.get(i));
                        }
                        j++;
                    }
                    ctor = clazz.getConstructor(consClasses);
                    Object[] consArgs = new Object[consClasses.length];
                    for (int i = 1, j = 0; i < ca.size(); i+=2) {
                        if (consClasses[j].isPrimitive()){
                            consArgs[j] = 
                                    getWrapperClassValueForPrimitiveType(consClasses[j], ca.get(i));
                        }
                        else {
                            consArgs[j] = consClasses[j].cast(ca.get(i));
                        }
                        j++;
                    }
                    object = ctor.newInstance(consArgs);
                } else {
                    ctor = clazz.getConstructor();
                    object = ctor.newInstance();
                }
                
                List<String> props = b.getProperties();
                
                if (!props.isEmpty()) {
                    for (int i = 0; i < props.size(); i++) {
                        char first = Character.toUpperCase(props.get(i).charAt(0));
                        String methodName = "set" + first + props.get(i).substring(1);
                        Method method = object.getClass().getMethod(methodName, 
                                new Class[] { props.get(i+1).getClass() });               
                        method.invoke(object, props.get(i+1));
                        i++;
                    }
                }

                beanTable.put(b.getName(), object);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void setupInterceptors(List<Bean> interceptorList) {
        for (Bean b : interceptorList) {
            try {
                final Class<?> clazz = Class.forName(b.getClassName());                
                Object interceptor = clazz.getConstructor().newInstance();
                interceptorTable.put(b.getName(), interceptor);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public Object getBean(String string) {
        return beanTable.get(string);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(String string, Class<T> type) {
        return (T) beanTable.get(string);
    }
    
    public Object[] getInterceptors() {
        return (Object[]) interceptorTable.values().toArray();
    }

}
