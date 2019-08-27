package io.github.thisisnozaku.pdfexporter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                          OutputStream destination, Map<String, String> overrideMappings) throws IOException {
        Map<String, String> initialMappings = this.extractor.generateFieldMappings(source);
        overrideMappings.entrySet().stream().forEach(e -> {
            initialMappings.put(e.getValue(), initialMappings.remove(e.getKey()));
        });
        writer.writePdf(originPdf, destination, initialMappings);
    }

    public void exportPdf(T source, InputStream originPdf,
                          OutputStream destination) throws IOException {
        exportPdf(source, originPdf, destination, Collections.EMPTY_MAP);
    }
}