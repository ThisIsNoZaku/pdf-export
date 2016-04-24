package io.github.thisisnozaku.pdfexporter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

/**
 * A simple implementation of {@link PdfFieldWriter}.
 *
 * @author Damien
 */
public class DefaultPdfWriter implements PdfFieldWriter {
    private Map<String, PDField> fields;

    @Override
    public void writePdf(InputStream originPdf, OutputStream destination,
                         Map<String, String> fieldMappings) throws IOException {
        fields = new HashMap<>();
        try (PDDocument document = PDDocument.load(originPdf)) {
            List<PDField> pdfFields = document.getDocumentCatalog().getAcroForm().getFields();
            for (PDField field : pdfFields) {
                calculateFullFieldName(field, new ArrayList<>());
            }
            for(Map.Entry<String, String> fieldMapping: fieldMappings.entrySet()){
                fields.get(fieldMapping.getKey()).setValue(fieldMapping.getValue());
            }
            document.save(destination);
            document.close();
        } catch (COSVisitorException e) {
            e.printStackTrace();
        }
    }

    private void calculateFullFieldName(COSObjectable field, List<String> precedingNameTokens) throws IOException {
        if (PDField.class.isAssignableFrom(field.getClass())) {
            List<COSObjectable> kids = ((PDField)field).getKids();
            precedingNameTokens.add(((PDField)field).getPartialName());
            if (kids != null) {
                for (COSObjectable kid : kids) {
                    calculateFullFieldName(kid, new ArrayList<>(precedingNameTokens));
                }
            } else {
                String fieldName = precedingNameTokens.stream().collect(Collectors.joining("."));
                fields.put(fieldName, (PDField) field);
            }
        }
    }
}
