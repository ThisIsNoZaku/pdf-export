### What is this repository for? ###

* API for extracting values from objects and filling out form PDFs from those values. Interfaces describing the API are contained within the com.thinoza.pdfexporter package, while a reflection-based extractor and combined filler-and-writer implementation are provided in the implementations package.
* Version: 0.0.1

### How do I get set up? ###

The project is a Gradle project. Execute 'gradle build' to build the project followed by 'gradle publishToMavenLocal' to install the artifacts in the local maven repository.

This project makes use of [Apache PDFBox](https://pdfbox.apache.org/).

### How do I use it? ###
Filling occurs in two steps: first, an implementation of FieldValueExtractor is used which takes some sort of input and returns a Map which maps individual values to the pdf fields they should be entered to.

Second, the mappings are passed along with the pdf to fill into an instance of PdfFieldWriter which copies, fills and returns the form pdf.

### How do I format PDF field names? ###
Field names are based on the Java Expression Language.

Each field name is represented as a path through named elements in a tree, representing a JSON object or non-cyclic object graph depending on the field mapping generator used.

For example, if trying to fill a PDF with the following simple json:

```
#!json

{
"first name": "Felix", "
second name": "Pierre"}
```
The value "Felix" will be inserted into the field, or fields, named "first name" and "Pierre" into the "second name" field.

In a more