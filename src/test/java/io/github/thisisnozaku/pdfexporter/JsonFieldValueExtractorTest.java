package io.github.thisisnozaku.pdfexporter;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Damien on 4/23/2016.
 */
public class JsonFieldValueExtractorTest {

    @org.junit.Test
    public void generateFieldMappings() throws Exception {
        JsonFieldValueExtractor fieldValueExtractor = new JsonFieldValueExtractor();
        String sourceJson = "{\"name\":\"character\"," +
                "\"player\":\"player\"}";

        Map<String, String> expected = new HashMap<>();
        expected.put("name", "character");
        expected.put("player", "player");

        assertEquals(expected, fieldValueExtractor.generateFieldMappings(sourceJson));
    }

    @org.junit.Test
    public void generateFieldMappingsWithNestedObject() throws Exception {
        JsonFieldValueExtractor fieldValueExtractor = new JsonFieldValueExtractor();
        String sourceJson = "{'name' : 'Damien Marble', 'address' : {'city' : {'name' : 'San Diego', 'population' : '1,381,069'}}}";

        Map<String, String> expected = new HashMap<>();
        expected.put("name", "Damien Marble");
        expected.put("address.city.name", "San Diego");
        expected.put("address.city.population", "1,381,069");
        assertEquals(expected, fieldValueExtractor.generateFieldMappings(sourceJson));
    }

    @org.junit.Test
    public void generateFieldMappingsWithNestedArray() throws Exception {
        JsonFieldValueExtractor fieldValueExtractor = new JsonFieldValueExtractor();
        String sourceJson = "{'name' : 'Damien Marble', 'phone numbers' : ['555-123-4567', '555-987-6543'], 'address' : '123 Some St'}";

        Map<String, String> expected = new HashMap<>();
        expected.put("name", "Damien Marble");
        expected.put("phone numbers[0]", "555-123-4567");
        expected.put("phone numbers[1]", "555-987-6543");
        expected.put("phone numbers", "555-123-4567, 555-987-6543");
        expected.put("address", "123 Some St");
        assertEquals(expected, fieldValueExtractor.generateFieldMappings(sourceJson));
    }

    @org.junit.Test(expected = IllegalArgumentException.class)
    public void throwExceptionOnInvalidJson() throws Exception {
        JsonFieldValueExtractor fieldValueExtractor = new JsonFieldValueExtractor();
        String sourceJson = "'name' : 'Damien Marble', 'phone numbers' : ['555-123-4567', '555-987-6543'], 'address' : '123 Some St'";
        Map<String, String> result = fieldValueExtractor.generateFieldMappings(sourceJson);
        throw new Exception();
    }

    @Test
    public void simpleDefaultMappingOverride(){
        JsonFieldValueExtractor fieldValueExtractor = new JsonFieldValueExtractor();
        String sourceJson = "{'name' : 'Damien Marble', 'phone numbers' : ['555-123-4567', '555-987-6543'], 'address' : '123 Some St'}";

        Map<String, String> mappingOverrides = new HashMap<>();
        mappingOverrides.put("phone numbers\\[(\\d)\\]", "contacts[$1]");

        Map<String, String> expected = new HashMap<>();
        expected.put("name", "Damien Marble");
        expected.put("contacts[0]", "555-123-4567");
        expected.put("contacts[1]", "555-987-6543");
        expected.put("address", "123 Some St");
        assertEquals(expected, fieldValueExtractor.generateFieldMappings(sourceJson, mappingOverrides));
    }

    @Test
    public void nestedDefaultMappingOverride(){
        JsonFieldValueExtractor fieldValueExtractor = new JsonFieldValueExtractor();
        String sourceJson = "{'name' : 'Damien Marble', 'phone numbers' : ['555-123-4567', '555-987-6543'], 'home' : {'address': '123 Some St', 'city': 'Hometown', 'country' : 'USA'}}";

        Map<String, String> mappingOverrides = new HashMap<>();
        mappingOverrides.put("home.address", "address");

        Map<String, String> expected = new HashMap<>();
        expected.put("name", "Damien Marble");
        expected.put("phone numbers[0]", "555-123-4567");
        expected.put("phone numbers[1]", "555-987-6543");
        expected.put("address", "123 Some St");
        expected.put("home.city", "Hometown");
        expected.put("home.country", "USA");
        expected.put("phone numbers", "555-123-4567, 555-987-6543");
        assertEquals(expected, fieldValueExtractor.generateFieldMappings(sourceJson, mappingOverrides));
    }

    @Test
    public void combiningMultiplePropertiesIntoSingleArray(){
        JsonFieldValueExtractor fieldValueExtractor = new JsonFieldValueExtractor();
        String sourceJson = "{'name' : 'Damien Marble', 'phone numbers' : {'mobile':'555-123-4567', 'work': '555-987-6543'}, 'home' : {'address': '123 Some St', 'city': 'Hometown', 'country' : 'USA'}}";

        Map<String, String> mappingOverrides = new HashMap<>();
        mappingOverrides.put("phone numbers.mobile", "phone[0]");
        mappingOverrides.put("phone numbers.work", "phone[1]");

        Map<String, String> expected = new HashMap<>();
        expected.put("name", "Damien Marble");
        expected.put("phone[0]", "555-123-4567");
        expected.put("phone[1]", "555-987-6543");
        expected.put("home.address", "123 Some St");
        expected.put("home.city", "Hometown");
        expected.put("home.country", "USA");
        assertEquals(expected, fieldValueExtractor.generateFieldMappings(sourceJson, mappingOverrides));
    }
}