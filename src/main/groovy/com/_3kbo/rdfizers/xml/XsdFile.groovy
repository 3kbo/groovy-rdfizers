package com._3kbo.rdfizers.xml

/**
* Xml Schema file RDFizer
* @author rhanc004
*
*/

class XsdFile extends XmlFile {
	
	def getRdfType() {"ei:XSD" }
	
	/**
	* The file extension used to represent the Xml Schema File
	*/
	@Override
	String getFileExtension() { ".xsd" }
	
	static main(args) {
		
		println "XsdFile extends XmlFile"
	}

}
