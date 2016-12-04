package application.main;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import application.classes.*;
import framework.core.annotations.Autowiring;
import framework.core.BeanFactory;
import framework.core.GenericXmlApplicationContext;
import framework.core.Interceptor;
import framework.core.XmlBeanDefinitionReader.ParserTypes;

public class MainApp {
    
    private static GenericXmlApplicationContext context = new GenericXmlApplicationContext(MainApp.class);
    
    //@Autowiring("java.lang.String") /* <- throws 'Class specified in annotation is not compatible' exception*/
    @Autowiring("application.classes.LowerCasingInterceptor")
    //@Autowiring
    private static Interceptor activeInterceptor;
    
    private static class ObjectInfo {
        
        private String name = "Anonymous";
        private String className;
        private String superClassName;
        private Class<?>[] implementedInterfaces;
        private Field[] fields;
        private Constructor<?>[] constructors;
        private Method[] methods;
        
        ObjectInfo(Object o, String name) {
            if (name != null && !name.isEmpty()) {
                this.name = name;
            }
            
            this.className = o.getClass().getName();
            this.superClassName = o.getClass().getSuperclass().getName();
            this.implementedInterfaces = o.getClass().getInterfaces();
            this.fields = o.getClass().getDeclaredFields();
            this.constructors = o.getClass().getDeclaredConstructors();
            this.methods = o.getClass().getMethods();
        }
        
        public String toString() {
            StringBuilder objectInfo = new StringBuilder();
            objectInfo.append("Inspecting " + name + ":\n");
            objectInfo.append("Class name: " + className + "\n");            
            objectInfo.append("Parent class: " + superClassName + "\n");            
            objectInfo.append("Implemented interfaces:");
            if (implementedInterfaces.length == 0) {
                objectInfo.append(" none\n");
            } else {
                objectInfo.append("\n");
                for (Class<?> ii : implementedInterfaces) {
                    objectInfo.append("\t* " + ii.getName() + "\n");
                }
            }
            
            objectInfo.append("Attributes:\n");
            for (Field f : fields) {
                int fieldModifiers = f.getModifiers();            
                objectInfo.append("\t* ");
                
                if (Modifier.isPublic(fieldModifiers)) {
                    objectInfo.append("public ");
                }
                if (Modifier.isProtected(fieldModifiers)) {
                    objectInfo.append("protected ");
                }
                if (Modifier.isPrivate(fieldModifiers)) {
                    objectInfo.append("private ");
                }
                if (Modifier.isFinal(fieldModifiers)) {
                    objectInfo.append("final ");
                }
                if (Modifier.isStatic(fieldModifiers)) {
                    objectInfo.append("static ");
                }
                if (Modifier.isSynchronized(fieldModifiers)) {
                    objectInfo.append("synchronized ");
                }
                
                objectInfo.append(f.getType().getSimpleName() + " " + f.getName() + ";\n");
            }
            
            objectInfo.append("Constructors:\n");
            for (Constructor<?> c : constructors) {
                int constructorModifiers = c.getModifiers();
                objectInfo.append("\t* ");
                
                if (Modifier.isPublic(constructorModifiers)) {
                    objectInfo.append("public ");
                }
                if (Modifier.isProtected(constructorModifiers)) {
                    objectInfo.append("protected ");
                }
                if (Modifier.isPrivate(constructorModifiers)) {
                    objectInfo.append("private ");
                }
                
                objectInfo.append(c.getName() + "(");
                if (c.getParameterCount() > 0) {
                    for (Parameter p : c.getParameters()) {
                        if (Modifier.isFinal(p.getModifiers())) {
                            objectInfo.append("final ");
                        }
                        objectInfo.append(p.getType().getSimpleName());
                        
                        if (p.isVarArgs()) {
                            objectInfo.delete(objectInfo.length() - 2, objectInfo.length()).append("...");
                        }
                        objectInfo.append(" " + p.getName() + ", ");
                    }
                    objectInfo.delete(objectInfo.length() - 2, objectInfo.length());
                }
                objectInfo.append(");\n");
            }
            
            objectInfo.append("Methods:\n");        
            for (Method m : methods) {
                int methodModifiers = m.getModifiers();            
                objectInfo.append("\t* ");
                
                if (Modifier.isPublic(methodModifiers)) {
                    objectInfo.append("public ");
                }
                if (Modifier.isProtected(methodModifiers)) {
                    objectInfo.append("protected ");
                }
                if (Modifier.isPrivate(methodModifiers)) {
                    objectInfo.append("private ");
                }
                if (Modifier.isFinal(methodModifiers)) {
                    objectInfo.append("final ");
                }
                if (Modifier.isAbstract(methodModifiers)) {
                    objectInfo.append("abstract ");
                }
                if (Modifier.isStatic(methodModifiers)) {
                    objectInfo.append("static ");
                }
                if (Modifier.isSynchronized(methodModifiers)) {
                    objectInfo.append("synchronized ");
                }
                
                //In order to get formal method parameters names via reflection, they must be present in .class file.
                //By default Java compiler discards this information, 
                //so you'll get arg0, arg1... instead of real informative names.
                //In order to have the method parameters names included in the .class files,
                //you must compile .java files with -parameters compile option.
                //To do this in Eclipse, go to Window > Preferences > Java > Compiler and
                //check the box 'Store information about method parameters (usable via reflection)'
                objectInfo.append(m.getReturnType().getSimpleName() + " " + m.getName() + "(");
                if (m.getParameterCount() > 0) {
                    for (Parameter p : m.getParameters()) {
                        if (Modifier.isFinal(p.getModifiers())) {
                            objectInfo.append("final ");
                        }
                        objectInfo.append(p.getType().getSimpleName());
                        
                        if (p.isVarArgs()) {
                            objectInfo.delete(objectInfo.length() - 2, objectInfo.length()).append("...");
                        }
                        objectInfo.append(" " + p.getName() + ", ");
                    }
                    objectInfo.delete(objectInfo.length() - 2, objectInfo.length());
                }
                objectInfo.append(");\n");
            }
            
            return objectInfo.toString();
        }
    }    
    
    public static void main(String[] args) {        
        context.load(MainApp.class.getResource("/GS_SpringXMLConfig.xml").getPath());
        context.setValidating(true);
        context.setParserType(ParserTypes.SAX);

        BeanFactory factory = context.getBeanFactory();

        GreetingService greetingService = 
                (GreetingService) factory.getBean("greetingService", GreetingService.class);
        System.out.println(greetingService.getMessage());


        Transport bus = 
                (Bus) factory.getBean("bus", Transport.class);
        bus.getTransport();

        System.out.println("bus2");
        Transport bus2 =
                (Bus) factory.getBean("bus2", Transport.class);
        bus2.getTransport();
        ((Bus) bus2).setMessage("New message was set");


        Transport bus3 =
                (Bus) factory.getBean("bus2", Transport.class);
        bus3.getTransport();
        System.out.println("---------------");

        Transport car = 
                (Car) factory.getBean("car", Transport.class);
        System.out.println(car.toString());

        Garage garage = factory.getBean("garage", Garage.class);
        System.out.println(garage.toString());

        for ( Object clazz : factory.getComponents()){
            System.out.println("Comm class: " + clazz);
        }

        for ( Object clazz : factory.getComponentsValues()){
            System.out.println("Comm value: " + clazz);
        }

        for ( Object clazz : factory.getServiceNames()){
            System.out.println("Service class: " + clazz);
        }

        for ( Object clazz : factory.getServiceInstances()){
            System.out.println("Serice value: " + clazz);
        }

        for ( Object clazz : factory.getRepositoriesNames()){
            System.out.println("Repositories class: " + clazz);
        }

        for ( Object clazz : factory.getRepositoriesInstances()){
            System.out.println("Repositories value: " + clazz);
        }

        for ( Object clazz : factory.getControllerNames()){
            System.out.println("Controller class: " + clazz);
        }

        for ( Object clazz : factory.getControllerInstancess()){
            System.out.println("Controller value: " + clazz);
        }
        //This block is needed for being able to inspect currently loaded classes 
        //with tools like Java VisualVM
        System.out.println("Press any key to exit...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}