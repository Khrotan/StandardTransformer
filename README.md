# Standard Transformer

A tool to transform XML messages.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisities

What things you need to install the software and how to install them

```
Give examples
```

### Installing

A step by step series of examples that tell you have to get a development env running

Stay what the step will be

```
Give the example
```

And repeat

```
until finished
```

End with an example of getting some data out of the system or using it for a little demo

## Mapping File

Mapping file is the required `.csv` file that stores the information about relationships between elements. Every line represents a mapping, and a **mapping** consists of three information. These three information are separated with semicolon.

* First section is the name of the element. This section is provided to ease the debugging, may ignored with any character.

* Second section is the XPath of the source document's element. This element's value will be fetched and put into the target element.

* Third section is the XPath of the target document's element. 

`responseType;/alert/info/responseType;/RequestResource/MessageRecall/RecallType`



## Standard Template Document

Template document is the required `xml` file to transform one message to another. Example templates for [Common Alerting Protocol](SampleXmlFiles/Templates/CAPTemplate.xml), [Resource Messaging](SampleXmlFiles/Templates/RMTemplate.xml) and [Sensor Markup Language](SampleXmlFiles/Templates/SensorMlTemplate.xml) are provided in the repository.

* Template document should have exactly one copy from each element, even from optional elements.
* Each element's text context should be filled with
`££££`.

* If the element has unbounded cardinality
`cardinality="unbounded"` attribute should be appended.
* If the element has minOccurs=0, i.e it is required,
`required="true"` attribute should be appended.

* If the element should not have text context
`haveNoTextContext="true"` attribute should be appended.

e.g 

	<sml:keywords cardinality="unbounded">
		<sml:KeywordList>
			<swe:extension cardinality="unbounded">
			</swe:extension>
			<swe:identifier>££££</swe:identifier>
			<swe:label>££££</swe:label>
			<swe:description>££££</swe:description>
			<sml:codeSpace/>
			<sml:keyword cardinality="unbounded" required="true">££££</sml:keyword>
		</sml:KeywordList>
	</sml:keywords>



## Example Usage

In order to convert a message to another standard, both template document of target standard and mapping `csv` file should be provided.  


## Authors

* **Arda Güney** - [Khrotan](https://github.com/Khrotan)

## License

This project is licensed under the Apache License, Version 2.0 - see the [LICENSE.md](LICENSE.md) file for details