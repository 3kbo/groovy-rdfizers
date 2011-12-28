package com._3kbo.rdfizers.xml

import java.io.File;

/**
* Xml Schema file RDFizer
* @author Richard Hancock
*
*/

class XsdFile extends XmlFile {
	
	/**
	* Create a XsdFile from the Xml Schema file passed as a parameter
	* @param xsd file
	*/
   XsdFile(File xsd)
   {
	   this.xmlFile = xsd
	   setNameAndNamespace(xmlFile)
   }
	
	/**
	* Create a XsdFile from a parent directory and the relative path of the Xml Schema to the parent directory
	* @param parent file
	* @param relative path	
	*/
   XsdFile(File parent, String path)
   {
	   this.xmlFile = new File(parent,path)
	   setNameAndNamespace(xmlFile)
   }
   
   /**
   * Create a XsdFile based on the Xml Schema itself and a shared rdfFile.
   * Information from a number of different WSDLs and their imported Xml Schemas
   * may be written to the shared rdfFile.
   * @param xsd file
   * @param rdfFile file
   */
  XsdFile(File xsd, File rdfFile)
  {
	  this.xmlFile = xsd
	  this.rdfFile = rdfFile
	  setNameAndNamespace(xmlFile)
  }
	
	def getRdfType() {"xsd:XSD" }
	
	/**
	* RDF statement expressing the import of another the Xml file
	* @return
	*/
	@Override
	def getRdfImportStatement(String namespace, String schemaLocation) {
	   
	   def importURI = getImportURI(namespace,schemaLocation)
	   """\n${getRdfURI()} xsd:import ${importURI} ."""
	}
	
	/**
	* RDF statement expressing the import of another the Xml file
	* @return
	*/
	@Override
	def getRdfIncludeStatement(String namespace, String schemaLocation) {
	   
	   def importURI = getImportURI(namespace,schemaLocation)
	   """\n${getRdfURI()} xsd:include ${importURI} ."""
	}
	
	/**
	* getDescribe - information specific to an XSD file
	* @return
	*/
	@Override
   def getDescribe() {
	   String description = """\n${getRdfURI()} rdfs:label "${name}"^^xsd:string ."""
	   description += fileDescription + imports + includes
	   return description
   }
   
   /**
   * getDescribeImports recurses through the imported Xml Schemas,
   * writing their RDF descriptions to the RDF file
   * @return
   */
   @Override
  def getImports() {
	  if (!xmlFile.exists()) {
		  println "Missing Xsd Import File: ${xmlFile.absolutePath}"
		  return ""
	  }
	  
	 String imports = ""
	 def xml = new XmlSlurper().parse(xmlFile)
	 
	 for (i in xml.import) {
		 imports += getRdfImportStatement(i.@namespace.text(), i.@schemaLocation.text())
	 }
	 return imports
  }
  
  /**
  * getDescribeIncludes recurses through the included Xml Schemas,
  * writing their RDF descriptions to the RDF file
  * @return
  */
  @Override
 def getIncludes() {
	 if (!xmlFile.exists()) {
		 println "Missing Xsd Include File: ${xmlFile.absolutePath}"
		 return
	 }

	 String includes = ""
	 def xml = new XmlSlurper().parse(xmlFile)
	 
	 for (i in xml.include) {
		 includes += getRdfIncludeStatement(i.@namespace.text(), i.@schemaLocation.text())
	 }
	 return includes
 }
	
	/**
	* The file extension used to represent the Xml Schema File
	*/
	@Override
	String getFileExtension() { ".xsd" }
	
	def export() {
		getRdfFile().withWriterAppend(){ it << describe }
		def xml = new XmlSlurper().parse(xmlFile)
				
		for (i in xml.import) {
			try {
				def xsd = new XsdFile(getImportFile(i.@schemaLocation.text()), rdfFile)
				println "xsd URI: ${xsd.rdfURI} "
				// getRdfFile().withWriterAppend(){ it << xsd.describe }
				xsd.export()
			}
			catch (e) {
				println "Error: ${e}"
			}
		}
		
		for (i in xml.include) {
			try {
				def xsd = new XsdFile(getImportFile(i.@schemaLocation.text()), rdfFile)
				println "xsd URI: ${xsd.rdfURI} "
				// getRdfFile().withWriterAppend(){ it << xsd.describe }
				xsd.export()
			}
			catch (e) {
				println "Error: ${e}"
			}
		}
		"""exported: ${name}"""
	}
	
	static main(args) {
		
		println "XsdFile extends XmlFile"
		def xsd = new XsdFile(new File("/Users/richardhancock/projects/JavaThing"), "ear-schemas/income-support-services-schemas/schemas/interfaces/schemas/incomesupport-services/v3.x/benefit.xsd")
		println "xsd name: ${xsd.name}"
		println "xsd targetNamespace: ${xsd.targetNamespace}"
		println "xsd describe: ${xsd.describe}"
	}

}
