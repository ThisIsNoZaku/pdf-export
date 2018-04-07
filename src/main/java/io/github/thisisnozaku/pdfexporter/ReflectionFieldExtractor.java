package io.github.thisisnozaku.pdfexporter;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This implementation of {@link FieldValueExtractor} uses reflection to
 * traverse an object graph and extract values from it. Each object in the graph
 * is examined recursively to determine if it contains any fields annotated by
 * {@link PdfExportable}. The stringified value of each annotated field is then
 * stored, mapped to the field name given by the annotation.
 * 
 * This implementations provides special handling of annotated
 * {@link Collection}s and {@link Map}s. Each element in a Collection, and each
 * value in a Map, is treated as a separate field with the same annotation as
 * their containing collection. The values are examined and traversed like any
 * other object field, if they themselves contain any properly annotated fields,
 * or otherwise are stringified.
 * 
 * When determining their names, the fieldValue of the annotation on the
 * collection is used, followed by a '#' and, for Collections, a number for the
 * order in which the items are iterated over, or, for Maps, the key of the
 * entry. For example, the string form of the first element in a Collection
 * annotated with the value "Numbers" will be mapped to 'Numbers#1' while in a
 * Map&lt;String, Number&gt; annotated the same, the value mapped to the key "one"
 * would go into field "Numbers#one".
 * 
 * The extractor maintains a set of all the objects it has already visited in
 * it's traversal and skips any previously visited objects.
 * 
 * @author Damien
 *
 */
public class ReflectionFieldExtractor implements FieldValueExtractor {
	private Set<Object> visitedObjects = new HashSet<>();
	private Deque<String> namePrefixes = new ArrayDeque<>();

	@Override
	public Map<String, String> generateFieldMappings(Object source) {
		return generateFieldMappings(source, FieldMappingDefinition.getDefinition());
	}

	public @Override Map<String, String> generateFieldMappings(Object object, FieldMappingDefinition mappingDefinition) {
		if (visitedObjects.contains(object)) {
			return Collections.<String, String> emptyMap();
		}
		visitedObjects.add(object);

		Map<String, String> valueMappings = new HashMap<>();

		Set<Field> fields = new HashSet<>();
		Class<? extends Object> objectClass = object.getClass();
		do {
			fields.addAll(Arrays.asList(object.getClass().getDeclaredFields()));
		} while ((objectClass = objectClass.getSuperclass()) != null);
		for (Field field : fields) {
			try {
				String fieldName = field.getAnnotation(PdfExportable.class).fieldName();
				if (field.getAnnotation(PdfExportable.class) != null) {
					if (Collection.class.isAssignableFrom(field.getType())) {
						valueMappings.putAll(processCollection(fieldName, Collection.class.cast(field.get(object))));
					} else if (Map.class.isAssignableFrom(field.getType())) {
						valueMappings.putAll(processMap(fieldName, Map.class.cast(field.get(object))));
					} else {
						valueMappings.put(namePrefixes.peek() + fieldName, field.get(object).toString());
					}
				} else {
					valueMappings.putAll(generateFieldMappings(field.get(object)));
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		namePrefixes.pop();
		return valueMappings;
	}

	private Map<String, String> processMap(String mapName,
			Map<? extends Object, ? extends Object> map) {
		visitedObjects.add(map);
		namePrefixes.push(mapName);
		Map<String, String> values = new HashMap<>();
		for (Entry<? extends Object, ? extends Object> entry : map.entrySet()) {
			if (visitedObjects.contains(entry.getValue())) {
				continue;
			} else {
				namePrefixes.push(entry.getKey().toString());
				values.putAll(generateFieldMappings(entry.getValue()));
			}
		}
		namePrefixes.pop();
		return values;
	}

	private Map<String, String> processCollection(String collectionName,
			Collection<? extends Object> collection) {
		visitedObjects.add(collection);
		Map<String, String> values = new HashMap<>();
		int counter = 0;
		for (Object object : collection) {
			if (visitedObjects.contains(object)) {
				continue;
			} else {
				namePrefixes.push(collectionName + (++counter));
				values.putAll(generateFieldMappings(object));
			}
		}
		namePrefixes.pop();
		return values;
	}
}