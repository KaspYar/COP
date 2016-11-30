package framework.parsers;

import framework.core.BeanFactory;
import framework.core.XmlBeanFactory;
import framework.core.utils.BeanUtils;
import framework.parsers.entities.BeanConstructorParameters;
import framework.parsers.entities.BeanProperties;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static framework.core.BeanFactory.classLibrary;

public class Bean {
    private interface KeyScopes {
        String SINGLETON = "singleton";
        String PROTOTYPE = "prototype";
    }

    private String name;
    private String className;
    private String scope;

    private Object instance;
    private Lock lock = new ReentrantLock();


    ArrayList<BeanConstructorParameters> constructorArg = new ArrayList<>();
    ArrayList<BeanProperties> properties = new ArrayList<>();

    public Object getBeanInstance() {
        if (isSingleton()) {
            lock.lock();
            try {
                if (instance == null) {
                    initializeBean();
                }
            } finally {
                lock.unlock();
            }
        } else

        {
            if (instance == null) {
                initializeBean();
            } else try {
                instance = BeanUtils.copy(instance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private void initializeBean() {
        final Class<?> clazz;
        try {
            clazz = Class.forName(getClassName());
            Constructor<?> ctor;

            List<BeanConstructorParameters> ca = getConstructorArg();

            if (!ca.isEmpty()) {

                Class<?>[] consClasses = new Class[ca.size()];
                Object[] consArgs = new Object[ca.size()];

                for (int i = 0; i < ca.size(); i++) {
                    BeanConstructorParameters params = ca.get(i);

                    if (params.getRef() != null) {
                        if (XmlBeanFactory.beanTable.get(params.getRef()).getBeanInstance() == null)
                            throw new ClassNotFoundException("Bean not found: " + params.getRef());
                        consClasses[i] = XmlBeanFactory.beanTable.get(params.getRef()).getBeanInstance().getClass();
                        consArgs[i] = XmlBeanFactory.beanTable.get(params.getRef()).getBeanInstance();

                    } else if (params.getType() == null || params.getType().equals("String")) {
                        consClasses[i] = String.class;
                        consArgs[i] = consClasses[i].cast(params.getValue());
                    } else if (classLibrary.containsKey(params.getType())) {
                        consClasses[i] = BeanFactory.getPrimitiveClassForName(params.getType());
                        consArgs[i] =
                                BeanFactory.getWrapperClassValueForPrimitiveType(consClasses[i], params.getValue());
                    } else {
                        consClasses[i] = Class.forName(params.getType());
                        consArgs[i] = consClasses[i].cast(params.getValue());
                    }
                }
                ctor = clazz.getConstructor(consClasses);
                instance = ctor.newInstance(consArgs);
            } else {
                ctor = clazz.getConstructor();
                instance = ctor.newInstance();
            }

            List<BeanProperties> props = properties;

            if (!props.isEmpty()) {
                for (int i = 0; i < props.size(); i++) {
                    BeanProperties beanProperty = props.get(i);
                    char first = Character.toUpperCase(beanProperty.getName().charAt(0));
                    String methodName = "set" + first + beanProperty.getName().substring(1);
                    if (beanProperty.getValue() != null) {
                        Method method = instance.getClass().getMethod(methodName,
                                new Class[]{beanProperty.getValue().getClass()});
                        method.invoke(instance, beanProperty.getValue());
                    } else {
                        Object o = XmlBeanFactory.beanTable.get(beanProperty.getReference()).getBeanInstance();
                        if (o == null)
                            throw new ClassNotFoundException("Bean not found: " + beanProperty.getName());
                        Method method = instance.getClass().getMethod(methodName,
                                new Class[]{XmlBeanFactory.beanTable.get(beanProperty.getReference()).getBeanInstance().getClass()});
                        method.invoke(instance, XmlBeanFactory.beanTable.get(beanProperty.getReference()).getBeanInstance());
                    }
                }
            }

            XmlBeanFactory.beanTable.put(name, this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<BeanProperties> getProperties() {
        return properties;
    }

    public void setProperties(ArrayList<BeanProperties> properties) {
        this.properties = properties;
    }

    public ArrayList<BeanConstructorParameters> getConstructorArg() {
        return constructorArg;
    }

    public void setConstructorArg(ArrayList<BeanConstructorParameters> constructorArg) {
        this.constructorArg = constructorArg;
    }

    public String toString() {
        return name + " : " + className.toString() + constructorArg.toString() + ", " + properties.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isSingleton() {
        return scope == null || KeyScopes.SINGLETON.equals(scope);
    }

    public boolean isPrototype() {
        return KeyScopes.PROTOTYPE.equals(scope);
    }
}
