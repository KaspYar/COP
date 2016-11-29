package framework.parsers;

import framework.parsers.entities.BeanConstructorParameters;
import framework.parsers.entities.BeanProperties;

import java.util.ArrayList;

public class Bean {
    String name;
    String className;
    ArrayList<BeanConstructorParameters> constructorArg = new ArrayList<>();
    ArrayList<BeanProperties> properties = new ArrayList<>();

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
}
