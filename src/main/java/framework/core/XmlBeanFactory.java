package framework.core;

import framework.parsers.Bean;

import java.util.HashMap;
import java.util.List;

public class XmlBeanFactory implements BeanFactory {

    public static HashMap<String, Bean> beanTable = new HashMap<>();
    HashMap<String, Object> interceptorTable = new HashMap<>();

    XmlBeanFactory(String xmlFilePath, XmlBeanDefinitionReader xbdr) {
        xbdr.loadBeanDefinitions(xmlFilePath);
        generateBeans(xbdr.getBeanList());
        setupInterceptors(xbdr.getInterceptorList());
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

}
