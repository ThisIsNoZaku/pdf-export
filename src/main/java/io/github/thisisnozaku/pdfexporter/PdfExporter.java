package io.github.thisisnozaku.pdfexporter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Fills and saves a pdf form, combining the functionality of the value extractor and
 *
 * @author Damien
 */
public class PdfExporter<T> {
    private final PdfFieldWriter writer;
    private final FieldValueExtractor extractor;

    /**
     * Parameterized constructor, which allows a custom implementation of
     * PdfWriter to be provided.
     *
     * @param writer the writer
     */
    public PdfExporter(PdfFieldWriter writer, FieldValueExtractor<T> fieldValueExtractor) {
        if (writer == null || fieldValueExtractor == null) {
            throw new IllegalStateException();
        }
        this.writer = writer;
        this.extractor = fieldValueExtractor;
    }

    /**
     * Fills the pdf from data and writes the result to destination.
     *
     * @param source      the object to extract the values from
     * @param originPdf   the pdf
     * @param destination the destination
     * @throws IOException
     */
    public void exportPdf(T source, InputStream originPdf,
                          OutputStream destination) throws IOException {
        writer.writePdf(originPdf, destination, this.extractor.generateFieldMappings(source));
    }
}