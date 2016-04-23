package io.github.thisisnozaku.pdfexporter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

/**
 * A simple implementation of {@link PdfFieldWriter}.
 *
 * @author Damien
 *
 */
public class DefaultPdfWriter implements PdfFieldWriter {

    @Override
    public void writePdf(InputStream originPdf, OutputStream destination,
                         Map<String, String> fieldMappings) throws IOException {
        try (PDDocument document = PDDocument.load(originPdf)) {
            List<PDField> pdfFields = document.getDocumentCatalog().getAcroForm().getFields();
            for (PDField field : pdfFields) {
                field.setValue(fieldMappings.get(field.getFullyQualifiedName()));
            }
            document.save(destination);
            document.close();
        } catch (COSVisitorException e) {
            e.printStackTrace();
        }
    }
}
