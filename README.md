### What is this repository for? ###

* API for extracting values from objects and filling out form PDFs from those values. Interfaces describing the API are contained within the com.thinoza.pdfexporter package, while a reflection-based extractor and combined filler-and-writer implementation are provided in the implementations package.
* Version: 0.0.1

### How do I get set up? ###

The project is a Gradle project. Execute 'gradle build' to build the project followed by 'gradle publishToMavenLocal' to install the artifacts in the local maven repository.

This project makes use of [Apache PDFBox](https://pdfbox.apache.org/).