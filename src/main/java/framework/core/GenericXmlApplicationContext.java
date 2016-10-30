package framework.core;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;

import framework.core.XmlBeanDefinitionReader.ParserTypes;

public class GenericXmlApplicationContext {
    
    private static class ConfigurationException extends RuntimeException {
        /**
         * 
         */
        private static final long serialVersionUID = -2684645760336447485L;
        
        public ConfigurationException(String e) {
            super(e);
        }
    }
    
    private static final Predicate<Class<?>> canInstantiate(Class<?> classToInstantiate) {
        return testClass -> 
            !testClass.isInterface() && classToInstantiate.isAssignableFrom(testClass);
    }
    
    private static final Predicate<Class<?>> isTheSameClassAs(Class<?> anotherClass) {
        return testClass -> 
            anotherClass.getName().equals(testClass.getName());
    }
    
    private static final String CONFIG_FILE_NAME = GenericXmlApplicationContext.class.getResource("/GS_SpringXMLConfig.xml").getPath();
    
    private final XmlBeanDefinitionReader reader;
    private final BeanFactory beanFactory;
    
    public XmlBeanDefinitionReader getReader() {
        return reader;
    }

    private String xmlFileLocation;    
    
    public GenericXmlApplicationContext() {
        this(CONFIG_FILE_NAME);        
    }
    
    @SuppressWarnings("unchecked")
    public GenericXmlApplicationContext(Class<?> classObject) {
        this(CONFIG_FILE_NAME);
        
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
                    if (canInstantiate(currentFieldClass).test(classInAnnotation)) {
                        match = classInAnnotation;
                    } else {
                        throw new ConfigurationException("Class specified in annotation is not compatible with " 
                                + currentFieldClass.getName()    + ".");
                    }
                    
                } else {
                    if (!classes.stream().anyMatch(canInstantiate(currentFieldClass))) {                
                        throw new ConfigurationException("No suitable implementation for " 
                                + currentFieldClass.getName()    + " found. Please check your configuration file.");
                    }

                    match = classes.stream().filter(canInstantiate(currentFieldClass)).findFirst().get();

                    if (classes.stream().anyMatch(canInstantiate(currentFieldClass)
                            .and(isTheSameClassAs(match).negate()))) {
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
        };
    }                

    public GenericXmlApplicationContext(String xmlFileLocation) {
        this.xmlFileLocation = xmlFileLocation;
        reader = new XmlBeanDefinitionReader();
        beanFactory = new XmlBeanFactory(this.xmlFileLocation, reader);
    }
    
    public void setValidating(boolean validating){
        reader.setValidating(validating);
    }
    
    public void setParserType(ParserTypes parserType){
        reader.setParserType(parserType);
    }
    
    public void load(String xmlFileLocation){
        this.xmlFileLocation = xmlFileLocation;
    }
    
    public BeanFactory getBeanFactory(){
        return beanFactory;
    }
}
