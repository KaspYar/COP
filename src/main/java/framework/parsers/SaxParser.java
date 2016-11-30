package framework.parsers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import framework.parsers.entities.BeanConstructorParameters;
import framework.parsers.entities.BeanProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SaxParser extends DefaultHandler implements Parser {
    
    List<Bean> beanList;
    List<Bean> interceptorList;
    String xmlFileName;
    String tmpValue;
    Bean beanTmp;
    private String packageToScan;

    public List<Bean> getBeanList() {
        return beanList;
    }
    
    public List<Bean> getInterceptorList() {
        return interceptorList;
    }
        
    public SaxParser(String xmlFileName) {
        this.xmlFileName = xmlFileName;
        beanList = new ArrayList<Bean>();
        interceptorList = new ArrayList<Bean>();
        parseDocument();
    }
    
    private void parseDocument() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(xmlFileName, this);
        } catch (ParserConfigurationException e) {
            System.out.println("ParserConfig error");
        } catch (SAXException e) {
            System.out.println("SAXException : xml not well formed");
        } catch (IOException e) {
            System.out.println("IO error");
        }
    }
    
    public String toString() {
        String res = "";
        for (Bean tmpB : beanList) {
            res += tmpB.toString() + "; ";
        }
        
        return res;
    }
    
    @Override
    public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException {
        
        if (elementName.equalsIgnoreCase("bean") || elementName.equalsIgnoreCase("interceptor")) {
            beanTmp = new Bean();
            beanTmp.setName(attributes.getValue("id"));
            beanTmp.setClassName(attributes.getValue("class"));
            beanTmp.setScope(attributes.getValue("scope"));
        }
        
        if (elementName.equalsIgnoreCase("constructor-arg")) {
            BeanConstructorParameters params = new BeanConstructorParameters();

            params.setType(attributes.getValue("type"));
            params.setValue(attributes.getValue("value"));

            params.setRef(attributes.getValue("ref"));

            beanTmp.getConstructorArg().add(params);
        }
        
        if (elementName.equalsIgnoreCase("property")) {
            BeanProperties properties = new BeanProperties();
            properties.setName(attributes.getValue("name"));
            properties.setValue(attributes.getValue("value"));

            properties.setReference(attributes.getValue("ref"));

            beanTmp.getProperties().add(properties);
        }

        if (elementName.equalsIgnoreCase("component-scan")) {
            packageToScan = attributes.getValue("base-package");
        }
    }

    public String getPackageToScan() {
        return packageToScan;
    }

    @Override
    public void endElement(String s, String s1, String element) throws SAXException {
        if (element.equals("bean")) {
            beanList.add(beanTmp);
        }
        
        if (element.equals("interceptor")) {
            interceptorList.add(beanTmp);
        }
    }
    
    @Override
    public void characters(char[] ac, int i, int j) throws SAXException {
        tmpValue = new String(ac, i, j);
    }    
}
