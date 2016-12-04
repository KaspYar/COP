package framework.core;

import framework.core.annotations.*;
import framework.parsers.Bean;
import org.reflections.Reflections;

import javax.naming.ConfigurationException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        Reflections reflections = new Reflections(xbdr.getPackageName());
        generator(reflections.getTypesAnnotatedWith(Service.class), serviceTable);
        generator(reflections.getTypesAnnotatedWith(Repository.class), repositoryTable);
        generator(reflections.getTypesAnnotatedWith(Controller.class), controllerTable);

    }

    private void generateComponents(String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Component.class);
        for (Class<?> clazz : annotated) {
            try {
                final Object newInstance = clazz.newInstance();
                componentsTable.put(clazz.getSimpleName().toLowerCase(), newInstance);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void generator(Set<Class<?>> annotated, Map<String, Object> map) {
        for (Class<?> clazz : annotated) {
            try {
                final Object newInstance = clazz.newInstance();
                try {
                    autowire(newInstance.getClass());
                } catch (ConfigurationException e) {
                    e.printStackTrace();
                }
                map.put(clazz.getSimpleName().toLowerCase(), newInstance);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void autowire(Class<?> classObject) throws ConfigurationException {
        Field[] fields = classObject.getDeclaredFields();
        for (Field currentField : fields) {
            if (currentField.isAnnotationPresent(Autowiring.class)) {
                ClassLoader myCL = Thread.currentThread().getContextClassLoader();
                Field classLoaderClassesField = null;
                Class<?> myCLClass = myCL.getClass();
                while (myCLClass != java.lang.ClassLoader.class) {
                    myCLClass = myCLClass.getSuperclass();
                }
                try {
                    classLoaderClassesField = myCLClass.getDeclaredField("classes");
                } catch (NoSuchFieldException | SecurityException e) {
                    e.printStackTrace();
                }
                classLoaderClassesField.setAccessible(true);

                List<Class<?>> classes = null;
                try {
                    classes = (List<Class<?>>) classLoaderClassesField.get(myCL);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                Class<?> currentFieldClass = currentField.getType();
                Class<?> match = null;

                if (!currentField.getAnnotation(Autowiring.class).value().isEmpty()) {
                    Class<?> classInAnnotation = null;
                    try {
                        classInAnnotation = Class.forName(currentField.getAnnotation(Autowiring.class)
                                .value());
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (GenericXmlApplicationContext.canInstantiate(currentFieldClass).test(classInAnnotation)) {
                        match = classInAnnotation;
                    } else {
                        throw new ConfigurationException("Class specified in annotation is not compatible with "
                                + currentFieldClass.getName() + ".");
                    }

                } else {
                    if (!classes.stream().anyMatch(GenericXmlApplicationContext.canInstantiate(currentFieldClass))) {
                        throw new ConfigurationException("No suitable implementation for "
                                + currentFieldClass.getName() + " found. Please check your configuration file.");
                    }

                    match = classes.stream().filter(GenericXmlApplicationContext.canInstantiate(currentFieldClass)).findFirst().get();

                    if (classes.stream().anyMatch(GenericXmlApplicationContext.canInstantiate(currentFieldClass)
                            .and(GenericXmlApplicationContext.isTheSameClassAs(match).negate()))) {
                        throw new ConfigurationException("Ambiguous configuration for "
                                + currentFieldClass.getName() + ". Please check your configuration file.");
                    }
                }

                try {
                    currentField.setAccessible(true);
                    currentField.set(null, match.newInstance());
                } catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
        ;
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
        return interceptorTable.values().toArray();
    }

    public Object[] getComponents() {
        return componentsTable.keySet().toArray();
    }

    public Object[] getComponentsValues() {
        return componentsTable.values().toArray();
    }

    @Override
    public Object[] getServiceNames() {
        return serviceTable.keySet().toArray();
    }

    @Override
    public Object[] getServiceInstances() {
        return serviceTable.values().toArray();
    }

    @Override
    public Object[] getRepositoriesNames() {
        return repositoryTable.keySet().toArray();
    }

    @Override
    public Object[] getRepositoriesInstances() {
        return repositoryTable.values().toArray();
    }

    @Override
    public Object[] getControllerNames() {
        return controllerTable.keySet().toArray();
    }

    @Override
    public Object[] getControllerInstancess() {
        return controllerTable.values().toArray();
    }
}
