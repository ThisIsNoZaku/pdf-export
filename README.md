# What is this repository for? #

* API for extracting values from objects and filling out form PDFs from those values. Interfaces describing the API are contained within the com.thinoza.pdfexporter package, while a reflection-based extractor and combined filler-and-writer implementation are provided in the implementations package.
* Version: 0.0.1

# How do I get set up? #

The project is a Gradle project. Execute 'gradle build' to build the project followed by 'gradle publishToMavenLocal' to install the artifacts in the local maven repository.

This project makes use of [Apache PDFBox](https://pdfbox.apache.org/).

# How do I use it? #
Filling occurs in two steps: first, an implementation of FieldValueExtractor is used which takes some sort of input and returns a Map which maps individual values to the pdf fields they should be entered to.

Second, the mappings are passed along with the pdf to fill into an instance of PdfFieldWriter which copies, fills and returns the form pdf.

## How do I format PDF field names? ##
Field names are based on the Java Expression Language.

Each field name is represented as a path through named elements in a tree, representing a JSON object or non-cyclic object graph depending on the field mapping generator used.

# For JSON #
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

Only Json primitives can be mapped to fields. For example, a PDF with a field named "phone" would be left empty, since the FieldValueExtractor would not generate a mapping for the entire array, only the individual elements.

# For Java #
Due to the wider variety of data structures in, the rules are a bit more complicated.

ReflectionFieldExtractor provides an implementation of FieldValueExtractor that generates field mappings by traversing an object graph.


The object graph is traversed by field name. Thus, the field "first.second" would contain the value found by starting at the root object, then traversing to the object in the field "first" then taking the value in the field "second".


Values are translated into Strings. For objects this is by calling the toString method on the object; if mapping an object directly to a field, make sure toString for the method returns a String representation you're happy with.


Elements within indexable collections, such as arrays or lists, can be accesses by placing an index within brackets like for array access. For example, getting the first element in a collection named "collection" would be "collection[0]", the second would be "collection[1]" and so on.


Iterable collections can be access using the same format as indexable ones. In this case, instead of an index, the number represents the order in which elements are encountered during iteration. 0 is the first element, 1 the second, etc, with the exact order determined by the underlying implementation.


Values stored in a Map can be accessed in the same way, by placing the key value inside the brackets, so long as the key is a numerical primitive (byte, short, int or long) or a String.