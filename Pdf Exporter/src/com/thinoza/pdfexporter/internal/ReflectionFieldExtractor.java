package com.thinoza.pdfexporter.internal;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.thinoza.pdfexporter.FieldValueExtractor;
import com.thinoza.pdfexporter.PdfExportable;

/**
 * This implementation of FieldValueExtractor uses reflection to traverse an
 * object graph. Each object in the graph is examined to determine if it
 * contains any fields annotated by {@link PdfExportable} and the field values.
 * Each object will be analyzed no more than once.
 * 
 * This implementations provides special handling of annotated
 * {@link Collection}s and {@link Map}s. Each element in a Collection, and each
 * value in a Map, is treated as a separate field with the same annotation as
 * their containing collection. When determining their names, the fieldValue of
 * the annotation on the field is used, followed by a '#' and, for Collections,
 * a number for the order in which the items are iterated over, or, for Maps,
 * the key of the entry. For example, the string form of the first element in a
 * Collection annotated with the value "Numbers" will be mapped to 'Numbers#1'
 * while in a Map<String, Number> annotated the same, the value mapped to the
 * key "one" would go into field "Numbers#one".
 * 
 * @author Damien
 *
 */
public class ReflectionFieldExtractor implements FieldValueExtractor {
	private Set<Object> visitedObjects = new HashSet<>();

	public @Override Map<String, String> generateFieldMappings(Object object) {
		if (visitedObjects.contains(object)) {
			return Collections.<String, String> emptyMap();
		}
		visitedObjects.add(object);
		Map<String, String> values = new HashMap<>();
		Set<Field> fields = new HashSet<>();
		Class<? extends Object> annotatedObjectType = object.getClass();
		do {
			fields.addAll(Arrays.asList(object.getClass().getDeclaredFields()));
		} while ((annotatedObjectType = annotatedObjectType.getSuperclass()) != null);
		for (Field field : fields) {
			try {
				String fieldName = field.getAnnotation(PdfExportable.class).fieldName();
				if (field.getAnnotation(PdfExportable.class) != null) {
					if (Collection.class.isAssignableFrom(field.getType())) {
						values.putAll(processCollection(fieldName, Collection.class.cast(field.get(object))));
					} else if (Map.class.isAssignableFrom(field.getType())) {
						values.putAll(processMap(fieldName, Map.class.cast(field.get(object))));
					} else {
						values.put(fieldName, field.get(object).toString());
					}
				} else {
					values.putAll(generateFieldMappings(field.get(object)));
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return values;
	}

	private Map<String, String> processMap(String mapName,
			Map<? extends Object, ? extends Object> map) {
		Map<String, String> values = new HashMap<>();
		for (Entry<? extends Object, ? extends Object> entry : map.entrySet()) {
			if (visitedObjects.contains(entry.getValue())) {
				continue;
			} else {
				values.put(mapName + "#" + entry.getKey().toString(), entry.getValue().toString());
			}
		}
		return values;
	}

	private Map<String, String> processCollection(String collectionName,
			Collection<? extends Object> collection) {
		Map<String, String> values = new HashMap<>();
		int counter = 0;
		for (Object object : collection) {
			if (visitedObjects.contains(object)) {
				continue;
			} else {
				values.put(collectionName + "#" + (++counter), object.toString());
			}
		}
		return values;
	}
}