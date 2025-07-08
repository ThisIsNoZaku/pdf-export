# What is this repository for?

* API for extracting values from objects and filling out form PDFs from those values.
* Version: 1.0.2

# How do I get set up? ###

The project is a Gradle project. Execute 'gradle build' to build the project followed by 'gradle publishToMavenLocal' to install the artifacts in the local maven repository.

This project makes use of [Apache PDFBox](https://pdfbox.apache.org/).

# How do I use it? ###
This project can be used in one of two ways: as a library or as a command line tool.

## As a command line tool
Build the project using gradle. This will generate a jar file in the `build/libs` directory.

To call the command line tool, use the following command:

```bash
java -jar pdf-export-<version>.jar <pdf to fill> <json input> <output pdf>
```

Where `<pdf to fill>` is the path to the PDF file you want to fill, `<json input>` is the path to a JSON file containing the data to fill the PDF with, and `<output pdf>` is the path where you want the filled PDF to be saved.

## As a library
## Including in your project

First, build the project using gradle and install into your local maven repository with the command `gradle publishToMavenLocal`.

Then, add the following dependency to your build.gradle file with the provider 'io.github.thisisnozaku' and the artifact name `pdf-export`.

In your project, create a PdfExporter instance, passing in an implementation of FieldValueExtractor and PdfFieldWriter.
The library provides ReflectionFieldExtractor for getting values from Java objects and DefaultPdfWriter for writing the pdfs.

Then call `exportPdf`, passing in the target object, a stream of the pdf to fill and an output stream to write the filled output to.

# How PDFs are filled
Filling occurs in two steps: first, an implementation of FieldValueExtractor is used which takes some sort of input and returns a Map which maps individual values to the pdf fields 
they should be entered to.

Second, the mappings are passed along with the pdf to fill into an instance of PdfFieldWriter which copies, fills and returns the form pdf.

## How do I format PDF field names? ##
This is currently up in the air, currently it is determined by how the field extractor decides it wants to name things and the pdf must match.

Each field name is represented as a path through named elements in a tree, representing a JSON object or object graph depending on the field mapping generator used.

**For JSON**
For example, if trying to fill a PDF with the following simple json:

```
#!json

{
   "first name": "Felix",
   "second name": "Pierre"
}
```
The value "Felix" will be inserted into the field, or fields, named "first name" and "Pierre" into the "second name" field.

In a more complicated scenario involving nested objects:
```
#!json
{
   "first name": "Felix",
   "second name": "Pierre",
   "address" : {
      "street name" : "Main st",
      "street number" : 101
   }
}
```
"Main st" will be mapped into "address.street name" and "101" into "address.street number".

Arrays are indicated by placing the array index in square brackets
```
#!json
{
   "first name": "Felix",
   "second name": "Pierre",
   "address" : {
      "street name" : "Main st",
      "street number" : 101
   },
   "phone" : [
     "555-5555",
     "555-1234"
   ]
}
```
To get the first phone number, a field would be named "phone[0]", the second "phone[1]" and so on.

Only Json primitives (boolean, string, number) can be mapped to individual fields. For example, a PDF with a field named "phone" would be left empty, since the FieldValueExtractor would not generate a mapping for the entire array, only the individual elements.

Placeholders can be used when the exact property name in the data is not known when creating the pdf.
```
#!json
{
   "first name": "Felix",
   "second name": "Pierre",
   "address" : {
      "street name" : "Main st",
      "street number" : 101
   },
   "phone" : {
     "home" : "555-5555",
     "work" : "555-1234"
   }
}
```

Here, the pdf could have fields for the values found in the "phone" property, without knowing the exact keys. This is achieved by using a placeholder: when naming the field, 
put brackets containing a number - like "{1}" in the unknown portion of the name. For example, the pdf might have a field `phone.{1}`. When the data is processed, the writer 
will see the placeholder value and attempt to find an incoming property name to match it. It will find `phone.home`, and ultimately the placeholder "{1}" will be replaced with 
the string "home".

Pdf field names may also have it so that rather than their value being found in the incoming data mappings, it is instead derived from the field name itself. This is done by putting a
portion of the field name in quotation marks, after replacing placeholder values. So a field named `phone.\"{1}"` and using the above placeholder mappings will have the value of "home".

 The mappings between the model properties and the fields can also be overridden at mapping time. Let's say your model has
 different fields for a persons home, mobile and work phone numbers, but the PDF only has generic "phone" fields. When performing
 the property mapping you can specify that those three model fields will be combined for the "phone" fields in the form.

```
#!json
{
   "first name": "Felix",
   "second name": "Pierre",
   "address" : {
      "street name" : "Main st",
      "street number" : 101
   },
   "phone" : {
     "home" : "555-5555",
     "work" : "555-1234",
     "mobile" : "555-9876"
   }
}
```

With this json, this mapping can be achieved with these overrides: 

```$xslt
phone.home -> phone[0]
phone.work -> phone[1]
phone.mobile -> phone[2]
```
 

# For Java #
Due to the wider variety of data structures in, the rules are a bit more complicated. Currently, the testing for this functionality is less robust, so use at your own risk.

ReflectionFieldExtractor provides an implementation of FieldValueExtractor that generates field mappings by traversing an object graph via reflection.

The object graph is traversed by field name. Thus, the field "first.second" would contain the value found by starting at the root object, then traversing to the object in the field "first" then taking the value in the field "second".

Values are translated into Strings. For objects this is by calling the toString method on the object; if mapping an object directly to a field, make sure toString for the method returns a String representation you're happy with.

Elements within indexable collections, such as arrays or lists, can be accesses by placing an index within brackets like for array access. For example, getting the first element in a collection named "collection" would be "collection[0]", the second would be "collection[1]" and so on.

Iterable collections can be access using the same format as indexable ones. In this case, instead of an index, the number represents the order in which elements are encountered during iteration. 0 is the first element, 1 the second, etc, with the exact order determined by the underlying iterable implementation.

Values stored in a Map can be accessed in the same way, by placing the key value inside the brackets, so long as the key is a numerical primitive (byte, short, int or long) or a String.