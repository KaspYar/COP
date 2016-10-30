package framework.parsers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SaxParser extends DefaultHandler implements Parser {
    
    List<Bean> beanList;
    List<Bean> interceptorList;
    String xmlFileName;
    String tmpValue;
    Bean beanTmp;
    
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
        }
        
        if (elementName.equalsIgnoreCase("constructor-arg")) {
            beanTmp.getConstructorArg().add(attributes.getValue("type"));
            beanTmp.getConstructorArg().add(attributes.getValue("value"));
        }
        
        if (elementName.equalsIgnoreCase("property")) {
            beanTmp.getProperties().add(attributes.getValue("name"));
            beanTmp.getProperties().add(attributes.getValue("value"));
        }
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
