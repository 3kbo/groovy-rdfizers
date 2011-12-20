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
	* The rdf:type used to represent the WsdlFile
	*/
   @Override
   def getRdfType() {"ei:WSDL" }
   
	/**
	* The file extension used to represent the WsdlFile
	*/
	@Override
	String getFileExtension() { ".wsdl" }
	
	
	def writeDependencies() {
		def wsdlXml = new XmlSlurper().parse(xmlFile)
		
		println "name: ${wsdlXml.@name}"
							
		targetNamespace = wsdlXml.@targetNamespace
		
		for (a in wsdlXml.attributes()) {
			println "attribute: ${a}"
		}
		
		getRdfFile().withWriterAppend(){ it << rdfDescription }
		
		writeRdfWsdlOperations(wsdlXml)
		
		def testForImports = wsdlXml.types.schema.import
		println "Xsd Schema Import count: ${testForImports.size()}"
		
		for (i in wsdlXml.types.schema.import) {
			println "import: ${i.@namespace}, ${i.@schemaLocation} "
			
			def uses ="""
${rdfURI} dp:uses <${i.@schemaLocation}> .
			"""
			
			def importedURI = getImportURI(i.@namespace.text(), i.@schemaLocation.text())
			
			getRdfFile().withWriterAppend(){ it << getRdfImportStatement(i.@namespace.text(), i.@schemaLocation.text()) }
									
			def xsd = new XsdFile(getImportFile(i.@schemaLocation.text()), rdfFile)
			println "xsd URI: ${xsd.rdfURI} "
			
			getRdfFile().withWriterAppend(){ it << xsd.rdfDescription }
			
			xsd.listImports()
			
			xsd.importsAsRDF()
		}
	}
	
	static main(args) {
		
		println "user.dir: ${System.getProperty('user.dir')}"
				
		def wsdl = new WsdlFile(new File("."), "schemas/interfaces/wsdls/incomesupport-services/v1.x/benefit.wsdl")
		println "wsdl name: ${wsdl.name}"
		println "wsdl targetNamespace: ${wsdl.targetNamespace}"
		println "wsdl absolutePath: ${wsdl.xmlFile.absolutePath}"
		println "wsdl canonicalPath: ${wsdl.xmlFile.canonicalPath}"
		
		
	}

}
