package io.github.thisisnozaku.pdfexporter;

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
        String sourceJson = "{'name' : 'Damien Marble'}";

        Map<String, String> expected = new HashMap<>();
        expected.put("name", "Damien Marble");
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
        String sourceJson = "{'name' : 'Damien Marble', 'phone numbers' : ['555-123-4567', '555-987-6543']}";

        Map<String, String> expected = new HashMap<>();
        expected.put("name", "Damien Marble");
        expected.put("phone numbers[0]", "555-123-4567");
        expected.put("phone numbers[1]", "555-987-6543");
        assertEquals(expected, fieldValueExtractor.generateFieldMappings(sourceJson));
    }
}