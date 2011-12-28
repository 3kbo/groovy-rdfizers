package com._3kbo.rdfizers.xml


/**
* Provides operations common to the different types of Xml files
* used in the Datapower Appliance, i.e. Xml schemas, WSDLs, XSLT Transforms.
* In particular support is provided for working with imported files
* 
* @author Richard Hancock
*
*/
class WsdlFile extends XmlFile {
	
	/**
	* Create a WsdlFile from the WSDL file passed as a parameter
	* @param wsdl file
	*/
   WsdlFile(File wsdl)
   {
	   this.xmlFile = wsdl
	   setNameAndNamespace(xmlFile)
   }
   
   /**
   * Create a WsdlFile from a parent directory and the relative path of the WSDL to the parent directory
   * @param wsdl file
   */
  WsdlFile(File parent, String path)
  {
	  this.xmlFile = new File(parent,path)
	  setNameAndNamespace(xmlFile)
  }
  
  /**
  * Create a WsdlFile based on the WSDL itself and a shared rdfFile.
  * Information from a number of different WSDLs and their imported Xml Schemas
  * may be written to the shared rdfFile. 
  * This constructor is used when iterating over one or more directory and 
  * @param wsdl file
  * @param rdfFile file
  */
 WsdlFile(File wsdl, File rdfFile)
 {
	 this.xmlFile = wsdl
	 this.rdfFile = rdfFile
	 setNameAndNamespace(xmlFile)
 }
	
	/**
	* The rdf:type used to represent the WsdlFile
	*/
   @Override
   def getRdfType() {"wsdl:WSDL" }
   
   /**
   * getDescribeIncludes recurses through the included Xml Schemas,
   * writing their RDF descriptions to the RDF file
   * @return
   */
   @Override
  def getDescribe() {
	String description = fileDescription + operations + imports + includes
  	return description
  }
   
   /**
   * getDescribe - information specific to a WSDL file
   * @return
   */
/*  
   @Override
  def getFileDescription() {
"""
${getRdfURI()} rdf:type ${rdfType};
  rdfs:label "${name}"^^xsd:string ;
  file:name "${name}"^^xsd:string ;
  file:absolutePath "${xmlFile.absolutePath}"^^xsd:string ;
  file:canonicalPath "${xmlFile.canonicalPath}"^^xsd:string .
"""
  }
*/  

def getOperations() {

	def wsdlXml = new XmlSlurper().parse(xmlFile)
	
	String serviceName = wsdlXml.service.@name
	
	String operations = """\n${rdfURI} wsdl:serviceName "${serviceName}"^^xsd:string ."""	
	operations += """\n${rdfURI} rdfs:label "${serviceName}"^^xsd:string ."""
		
	String version = wsdlXml.types.schema.@version
	
	if (version != null) {
		operations += """\n${rdfURI} wsdl:version "${version}"^^xsd:string ."""
	}
	
	println "wsdlXml.binding ${wsdlXml.binding.@name.text()}"
	
	for (i in wsdlXml.binding.operation) {
		println "i: ${i.@name.text()}"
		operations += """\n${rdfURI} wsdl:operation <${uri}/${i.@name.text()}> ."""
		operations += """\n<${uri}/${i.@name.text()}> rdf:type wsdl:Operation ;"""
		operations += """\n  rdfs:label "${serviceName}.${i.@name.text()}"^^xsd:string ;"""		
		operations += """\n  wsdl:operationName "${i.@name.text()}"^^xsd:string ."""
	}
	return operations
}
  
  
  /**
  * getImports recurses through the imported Xml Schemas,
  * writing their RDF descriptions to the RDF file
  * @return
  */
@Override
def getImports() {

	def wsdlXml = new XmlSlurper().parse(xmlFile)
	
	def testForImports = wsdlXml.types.schema.import
	println "Wsdl Import count: ${testForImports.size()}"
	
	String imports = ""
	
	for (i in wsdlXml.types.schema.import) {
		imports += getRdfImportStatement(i.@namespace.text(), i.@schemaLocation.text()) 
	}
	return imports	
}

/**
* RDF statement expressing the import of another the Xml file
* @return
*/
@Override
def getRdfImportStatement(String namespace, String schemaLocation) {
   
   def importURI = getImportURI(namespace,schemaLocation)
"""
${getRdfURI()} wsdl:import ${importURI} ."""
}
 
 /**
 * getDescribeIncludes recurses through the included Xml Schemas,
 * writing their RDF descriptions to the RDF file
 * @return
 */
 @Override
def getIncludes() {
	def wsdlXml = new XmlSlurper().parse(xmlFile)
	
	def testForIncludes = wsdlXml.types.schema.include
	println "Wsdl Include count: ${testForIncludes.size()}"
	
	String includes = ""
	
	for (i in wsdlXml.types.schema.include) {
		includes += getRdfImportStatement(i.@namespace.text(), i.@schemaLocation.text())
	}
	return includes
}
   
	/**
	* The file extension used to represent the WsdlFile
	*/
	@Override
	String getFileExtension() { ".wsdl" }
	
	/**
	 * Export the RDF description of the WSDL file in N3 format.
	 * The export includes the RDF descriptions of all the imported Xml Schemas,
	 * including the Xml Schemas that they import or include, etc...
	 * @return
	 */
	def export() {				
		getRdfFile().withWriterAppend(){ it << describe }		
		def wsdlXml = new XmlSlurper().parse(xmlFile)
				
		for (i in wsdlXml.types.schema.import) {
			try {
				def xsd = new XsdFile(getImportFile(i.@schemaLocation.text()), rdfFile)
				println "xsd URI: ${xsd.rdfURI} "			
				//getRdfFile().withWriterAppend(){ it << xsd.describe }
				xsd.export()				
			}
			catch (e) {
				println "Error: ${e}"
			}			
		}
		"""exported: ${name}"""
	}
	
	static main(args) {
		
		println "user.dir: ${System.getProperty('user.dir')}"
				
		def wsdl = new WsdlFile(new File("/Users/richardhancock/projects/JavaThing"), "ear-schemas/income-support-services-schemas/schemas/interfaces/wsdls/incomesupport-services/v1.x/benefit.wsdl")
		println "wsdl name: ${wsdl.name}"
		println "wsdl targetNamespace: ${wsdl.targetNamespace}"
		println "wsdl describe: ${wsdl.describe}"
		println "${wsdl.export()}"
	}

}
