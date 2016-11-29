package framework.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import framework.parsers.Bean;
import framework.parsers.entities.BeanConstructorParameters;
import framework.parsers.entities.BeanProperties;

public class XmlBeanFactory implements BeanFactory {

    HashMap<String, Object>  beanTable = new HashMap<String, Object>();
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

                List<BeanConstructorParameters> ca = b.getConstructorArg();

                if (!ca.isEmpty()) {

                    Class<?>[] consClasses = new Class[ca.size()];
                    Object[] consArgs = new Object[ca.size()];

                    for (int i = 0; i < ca.size(); i++) {
                        BeanConstructorParameters params = ca.get(i);

                        if (params.getRef() != null) {
                            if (getBean(params.getRef()) == null)
                                throw new ClassNotFoundException("Bean not found: "+ params.getRef());
                            consClasses[i] = beanTable.get(params.getRef()).getClass();
                            consArgs[i] = beanTable.get(params.getRef());

                        } else if (params.getType() == null || params.getType().equals("String")) {
                            consClasses[i] = String.class;
                            consArgs[i] = consClasses[i].cast(params.getValue());
                        } else if (classLibrary.containsKey(params.getType())) {
                            consClasses[i] = getPrimitiveClassForName(params.getType());
                            consArgs[i] =
                                    getWrapperClassValueForPrimitiveType(consClasses[i], params.getValue());
                        } else {
                            consClasses[i] = Class.forName(params.getType());
                            consArgs[i] = consClasses[i].cast(params.getValue());
                        }
                    }
                    ctor = clazz.getConstructor(consClasses);
                    object = ctor.newInstance(consArgs);
                } else {
                    ctor = clazz.getConstructor();
                    object = ctor.newInstance();
                }

                List<BeanProperties> props = b.getProperties();

                if (!props.isEmpty()) {
                    for (int i = 0; i < props.size(); i++) {
                        char first = Character.toUpperCase(props.get(i).getName().charAt(0));
                        String methodName = "set" + first + props.get(i).getName().substring(1);
                        Method method = object.getClass().getMethod(methodName,
                                new Class[]{props.get(i).getValue().getClass()});
                        method.invoke(object, props.get(i).getValue());
                        i++;
                    }
                }

                beanTable.put(b.getName(), object);
            } catch (Exception ex) {
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
            } catch (Exception ex) {
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
