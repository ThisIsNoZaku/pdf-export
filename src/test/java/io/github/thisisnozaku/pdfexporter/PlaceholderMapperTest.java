package io.github.thisisnozaku.pdfexporter;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Damien on 9/21/2016.
 */
public class PlaceholderMapperTest {
    @Test
    public void testSimplePlaceholderMapping(){
        List<String> fieldNames = new ArrayList<>();
        fieldNames.add("character.skills.Common Lore ({1})");
        List<String> propertyNames = new ArrayList<>();
        propertyNames.add("character.skills.Common Lore (Imperium)");

        Map<String, String> expectedMappings = new HashMap<>();
        expectedMappings.put("1", "Imperium");

        assertEquals(expectedMappings, new PlaceholderMapper().mapPlaceholdersToValues(fieldNames, propertyNames));
    }

    @Test
    public void testMultiplePlaceholderMapping(){
        List<String> fieldNames = new ArrayList<>();
        fieldNames.add("character.skills.Common Lore ({1})");
        fieldNames.add("character.skills.Common Lore ({2})");
        fieldNames.add("character.skills.Common Lore ({3})");
        List<String> propertyNames = new ArrayList<>();
        propertyNames.add("character.skills.Common Lore (Imperium)");
        propertyNames.add("character.skills.Common Lore (Imperial Guard)");
        propertyNames.add("character.skills.Common Lore (War)");

        Map<String, String> expectedMappings = new HashMap<>();
        expectedMappings.put("1", "Imperium");
        expectedMappings.put("2", "Imperial Guard");
        expectedMappings.put("3", "War");

        assertEquals(expectedMappings, new PlaceholderMapper().mapPlaceholdersToValues(fieldNames, propertyNames));
    }
}
