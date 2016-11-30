package framework.core;

import framework.core.annotations.Component;
import framework.parsers.Bean;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class XmlBeanFactory implements BeanFactory {

    public static HashMap<String, Bean> beanTable = new HashMap<>();
    HashMap<String, Object> interceptorTable = new HashMap<>();


    HashMap<String, Object> componentsTable = new HashMap<>();
    HashMap<String, Object> controllerTable = new HashMap<>();
    HashMap<String, Object> serviceTable = new HashMap<>();
    HashMap<String, Object> repositoryTable = new HashMap<>();

    XmlBeanFactory(String xmlFilePath, XmlBeanDefinitionReader xbdr) {
        xbdr.loadBeanDefinitions(xmlFilePath);
        generateBeans(xbdr.getBeanList());
        setupInterceptors(xbdr.getInterceptorList());

        generateComponents(xbdr.getPackageName());
//        generateControllers(xbdr.getPackageName());
//        generateServices(xbdr.getPackageName());
//        generateRepositories(xbdr.getPackageName());
    }

    private void generateComponents(String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Component.class);
        for (Class<?> clazz : annotated) {
            try {
                final Object newInstance = clazz.newInstance();
                componentsTable.put(clazz.getSimpleName().toLowerCase(), newInstance);
            } catch (InstantiationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    private void generateBeans(List<Bean> beanList) {
        for (Bean bean : beanList) {
            try {
                bean.getBeanInstance();
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
        return (T) beanTable.get(string).getBeanInstance();
    }

    public Object[] getInterceptors() {
        return (Object[]) interceptorTable.values().toArray();
    }

    public Object[] getComponents() {
        return (Object[]) componentsTable.keySet().toArray();
    }

    public Object[] getComponentsValues() {
        return (Object[]) componentsTable.values().toArray();
    }

}
