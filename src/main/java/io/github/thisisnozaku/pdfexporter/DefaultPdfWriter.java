package io.github.thisisnozaku.pdfexporter;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckbox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioCollection;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextbox;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simple implementation of {@link PdfFieldWriter}.
 * <p>
 * This expects field names in a format that resembles traversing a Javascript object.
 * <p>
 * Each token represents a field name, separated by the "." character. Elements in an array
 * are instead referenced by a number contained within square brackets: "[]".
 * <p>
 * Fields whose exact name is unknown until runtime can contain special placeholder indicators.
 * This placeholder consists of a number within a pair of braces : "{x}". The writer will examine
 * the incoming field mappings to find any matching fields that are also NOT mapped to any other
 * fields and will replace that placeholder value.
 * <p>
 * For example, if you have a pdf field named "phone number.{1}" and the input contains a mapping for
 * "phone numbers.home" which is not already mapped to a placeholder, then anywhere that "{1}" appears, it will be
 * replaced by "home".
 * <p>
 * You can instead insert a property name, or a portion of one, rather than its value, by surrounding the portion of the property name to
 * insert with quotation marks. For example, if the above property were instead 'phone number."{1}"' with the same mapping,
 * the value of the fields would be "home".
 *
 * @author Damien
 */
public class DefaultPdfWriter implements PdfFieldWriter {
    private Map<String, PDField> fields;

    @Override
    public void writePdf(InputStream originPdf, OutputStream destination,
                         Map<String, String> fieldMappings) throws IOException {
        fields = new TreeMap<>();
        try (PDDocument document = PDDocument.load(originPdf)) {
            List<PDField> pdfFields = document.getDocumentCatalog().getAcroForm().getFields();
            //Get the names of all the fields in the pdf.
            for (PDField field : pdfFields) {
                calculateFullFieldName(field, new ArrayList<>());
            }

            Map<String, PDField> valueFields = new TreeMap<>();
            Map<String, PDField> literalFields = new TreeMap<>();

            //Find every pdf field where the value will be a literal from the property name.
            Map<String, String> literalFieldValues = new HashMap<>();
            for (String fieldName : this.fields.keySet()) {
                Matcher m = Pattern.compile(".*\"(.*)\".*").matcher(fieldName);
                if (m.matches()) {
                    literalFields.put(fieldName.replaceAll("\"", ""), this.fields.get(fieldName));
                    literalFieldValues.put(fieldName.replaceAll("\"", ""), m.group(1));
                } else {
                    valueFields.put(fieldName, this.fields.get(fieldName));
                }
            }

            //Get placeholder replacements
            Map<String, String> placeholderReplacements = new PlaceholderMapper()
                    .mapPlaceholdersToValues(
                            Stream.concat(valueFields.keySet().stream(), literalFields.keySet().stream())
                                    .filter(e -> e.matches(".*\\{.*\\}.*")).collect(Collectors.toSet()),
                            fieldMappings.keySet());

            //Replace all placeholders in property names with the real values.
            for (String placeholder : placeholderReplacements.keySet()) {
                Optional<String> valueKey = valueFields.keySet().stream().filter(e->e.matches(String.format(".*\\{%s\\}.*", placeholder)))
                        .findFirst();
                if(valueKey.isPresent()){
                    String keyWithReplacement = valueKey.get().replaceAll(
                            String.format("\\{%s\\}", placeholder), placeholderReplacements.get(placeholder));
                    valueFields.put(keyWithReplacement, valueFields.get(valueKey.get()));
                    valueFields.remove(valueKey.get());
                }
                Optional<String> literalKey = literalFields.keySet().stream().filter(e->e.matches(String.format(".*\\{%s\\}.*", placeholder)))
                        .findFirst();
                if(literalKey.isPresent()){
                    String keyWithReplacement = literalKey.get().replaceAll(String.format("\\{%s\\}", placeholder),
                            placeholderReplacements.get(placeholder));
                    String valueWithReplacement = literalFieldValues.get(literalKey.get()).replaceAll(String.format("\\{%s\\}", placeholder),
                            placeholderReplacements.get(placeholder));
                    literalFields.put(keyWithReplacement, literalFields.get(literalKey.get()));
                    literalFields.remove(literalKey.get());
                    literalFieldValues.put(keyWithReplacement,valueWithReplacement);
                    literalFieldValues.remove(literalKey.get());
                }
            }

            //Discard any incoming mappings that don't have a matching pdf field
            Set<String> propertiesWithMatchingFields = fieldMappings.keySet().stream().filter((String field) -> {
                return valueFields.containsKey(field) || literalFields.containsKey(field);
            }).collect(Collectors.toSet());

            for (String fieldMapping : propertiesWithMatchingFields) {
                PDField valueField = valueFields.get(fieldMapping);
                if (valueField != null) {
                    Class fieldClass = valueField.getClass();
                    if (valueField.getClass().equals(PDTextbox.class)) {
                        valueField.setValue(fieldMappings.get(fieldMapping));
                    } else if (valueField.getClass().equals(PDCheckbox.class)) {
                        ((PDCheckbox) valueField).getKids();
                    } else if (valueField.getClass().equals(PDRadioCollection.class)) {
                        ((PDRadioCollection) valueField).setValue(fieldMappings.get(fieldMapping));
                    }
                }
                PDField literalField = literalFields.get(fieldMapping);
                if(literalField != null){
                    literalField.setValue(literalFieldValues.get(fieldMapping));
                }
            }
            document.save(destination);
            document.close();
        } catch (
                COSVisitorException e
                )

        {
            e.printStackTrace();
        }

    }

    private void calculateFullFieldName(PDField field, List<String> precedingNameTokens) throws IOException {
        if (PDField.class.isAssignableFrom(field.getClass())) {
            if (((PDField) field).getPartialName() != null) {
                precedingNameTokens.add(((PDField) field).getPartialName());
            }
            Optional<List<COSObjectable>> kids = Optional.empty();
            if(field.getKids() != null) {
                List<COSObjectable> filtered = field.getKids().stream()
                        .filter(e -> PDField.class.isAssignableFrom(e.getClass())
                        ).collect(Collectors.toList());
                if(filtered.size() == 0){
                    kids = Optional.empty();
                } else {
                    kids = Optional.of(filtered);
                }

            }
            if (kids.isPresent() && !field.getClass().equals(PDRadioCollection.class)) {
                for (COSObjectable kid : kids.get()) {
                    if(PDField.class.isAssignableFrom(kid.getClass())) {
                        calculateFullFieldName((PDField)kid, new ArrayList<>(precedingNameTokens));
                    }
                }

            } else {
                String fieldName = precedingNameTokens.stream().collect(Collectors.joining("."));
                fields.put(fieldName, (PDField) field);
            }
        }
    }
}
