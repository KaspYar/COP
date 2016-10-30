package task1.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import framework.parsers.SaxParser;

public class SaxParserTest {
    String xmlFilePath = getClass().getResource("/GS_SpringXMLConfig.xml").getPath();
    
    @Test
    public void testCreation() {
        SaxParser tester = new SaxParser(xmlFilePath);
        assertNotNull(tester);
    }
    
    @Test
    public void testGetBeanList() {
        SaxParser tester = new SaxParser(xmlFilePath);
        assertNotNull(tester.getBeanList());
    }
    
    @Test
    public void testToString() {
        SaxParser tester = new SaxParser(xmlFilePath);
        assertNotNull(tester.toString());
    }
}
