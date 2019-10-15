package io.github.thisisnozaku.pdfexporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length != 3 && args.length != 4) {
            throw new IllegalArgumentException(format("Need 3 or 4 arguments but %d received", args.length));
        }
        Path pdfPath = Paths.get(".", args[0]);
        if (!Files.exists(pdfPath)) {
            throw new IOException("PDF input file not found");
        }
        Path dataJsonPath = Paths.get(".", args[1]);
        if (!Files.exists(dataJsonPath)) {
            throw new IOException("Data input file not found");
        }

        Map<String, String> fieldMappings = Optional.ofNullable(args.length == 4 ? args[3] : null)
                .map(path -> {
                    try {
                        return new ObjectMapper().readValue(Files.newInputStream(Paths.get(".", path)), Map.class);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
        }).orElse(Collections.emptyMap());
        PdfExporter<String> objectPdfExporter = new PdfExporter<>(
                new DefaultPdfWriter(),
                new JsonFieldValueExtractor()
        );
        String content = IOUtils.toString(Files.newInputStream(dataJsonPath));
        InputStream pdfInput = Files.newInputStream(pdfPath);
        OutputStream pdfOutput = Files.newOutputStream(Paths.get(".", args[2]));
        objectPdfExporter.exportPdf(content, pdfInput, pdfOutput, fieldMappings);
        System.out.println(format("Written to %s", args[2]));
    }

    private void printUsage(){
        System.out.println("Required path to pdf, path to json data and path to output.");
    }
}
