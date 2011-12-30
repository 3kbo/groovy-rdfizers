package com._3kbo.rdfizers.xml


/**
* Provides operations common to the RDFizing Xml schemas and WSDLs
* The RDF description of the Xml schemas and WSDLs is written out using the N3 / TURTLE syntax.
* (See http://www.w3.org/TeamSubmission/n3/)
* If the RDF file does not already exist a new RDF file is created in the same directory as the XML file
* 
* @author Richard Hancock
*
*/
abstract class XmlFile {
	
	//The Xml file, either an Xml Schema or WSDL
	//This is to be checked for additional imported or included files
	File xmlFile

	//Name of the Xml file
	String name
	
	//Protocol or URI Scheme (file: or http:) used to access the file or URL
	String protocol

	//The relative path to the directory containing the Xml File.
	//This is relative to the directory the application is run from
	String relativeDirectory

	//The targetNamespace of the Xml file
	String targetNamespace
	

	String contextURI

	//The rdf file listing the imports for this Xml file
	File rdfFile;
	
	/**
	* Set the name and targetNamespace by reading the Xml File
	* The targetNamespace may be null if the file can not be found
	* @param xmlFile
	* @return
	*/
   def setNameAndNamespace(File xmlFile) {
	   name = xmlFile.name
	   
	   if (xmlFile.exists()) {
		   def xml = new XmlSlurper().parse(xmlFile)
		   targetNamespace = xml.@targetNamespace.text()
	   }
	   else {
		   println "File not found: ${xmlFile.absolutePath}"
	   }

   }
   
   
   /**
	* Returns the URI Resource Name.
	* This is the file name without the file extension
	* @return
	*/
   String getResourceName() {
	   //Remove the file ending
	   def parts = name.tokenize('.')
	   return parts[0]
   }
   
   /**
   * Returns the URI Resource Name.
   * This is the file name without the file extension
   * @return
   */
  String getResourceName(String filename) {
	  //Remove the file ending
	  def parts = filename.tokenize('.')
	  return parts[0]
  }

   /**
	* The URI of the Xmlfile
	* @return URI
	*/
   String getURI() {
	   
	  if (targetNamespace.endsWith("/")) {
		  return "${targetNamespace}${resourceName}"
	  }
	  "${targetNamespace}/${resourceName}"
   }
   
   /**
   * The URI of the Xmlfile
   * @return URI
   */
  String getUri() {
	  if (targetNamespace.endsWith("/")) {
		  return "${targetNamespace}${resourceName}"
	  }
	  "${targetNamespace}/${resourceName}"
  }
   
   /**
   * RDF URI string for the Xmlfile
   * @return URI
   */
  String getRdfURI() {
	  if (targetNamespace.endsWith("/")) {
		  return "<${targetNamespace}${resourceName}>"
	  }
	  "<${targetNamespace}/${resourceName}>"
  }
  
  /**
  * RDF URI string for the Context of the Xmlfile
  * @return URI
  */
 String getContextRdfURI() {
	 return "<${contextURI}>"
 }
   
   /**
	* Get the Xml File
	* @return Xml File
	*/
   File getXmlFile() {
	   
	   if (xmlFile == null) {
		   String directory = System.getProperty('user.dir') + "/" + relativeDirectory
		   
		   println "WSDL Directory: ${directory}"
		   File wsdlDirectory = new File(directory)
		   
		   if (wsdlDirectory.exists()) {
			   xmlFile = new File(wsdlDirectory,name);
			   println "Xml File absolutePath: ${xmlFile.absolutePath}"
			   return xmlFile;
		   }
		   else {
			   return null;
		   }
	   }
	   else {
		   return xmlFile;
	   }
   }

   
   /**
	* Return the URI of the imported file
	* @param namespace
	* @param schemaLocation
	* @return URI
	*/
   String getImportURI(String namespace, String schemaLocation) {

	   String[] segments = schemaLocation.split("/")
	   def filename = segments[segments.length -1 ]
	   return "<${namespace}/${getResourceName(filename)}>"
   }

   /**
	* Returns the actual import file.
	* The path to the import file is generated using the import schemaLocation
	* and the parent directory of the current file
	*
	* @param schemaLocation
	* @return
	*/
   File getImportFile(String schemaLocation) {

	   def absolutePath

	   // If the path is relative add the current directory
	   if ( schemaLocation.startsWith("../")) {
		   absolutePath = xmlFile.parent + "/" + schemaLocation
	   }

	   File importedFile = new File(absolutePath)

	   if (importedFile.exists()) {
		   println "Imported File: ${importedFile.absolutePath}"
	   }
	   else {
		   println "Could not find Imported File: ${absolutePath}"
	   }
	   return importedFile
   }

   /**
	* List all the files imported by this Xml file
	* @return
	*/
   def listImports() {
	   def xml = new XmlSlurper().parse(xmlFile)
	   for (i in xml.import) {
		   println "xml import:  ${i.@schemaLocation} "
	   }
   }

   /**
	* Express the list imported files as RDF statements (using N3 notation)
	* @return
	*/
   def importsAsRDF() {
   }

   /**
	* Write the list imported files to the RDF file (using N3 notation)
	* @return
	*/
   def writeImportsToRdfFile(File rdfFile) {
   }

   def abstract getRdfType();

   /**
    * getDescribe provides an RDF graph based on information that can 
    * be read directly from the source Xml file.  
    * @return
    */
   def abstract getDescribe();
   
   /**
   * getImports recurses through the imported Xml Schemas, 
   * writing their RDF descriptions to the RDF file
   * @return
   */
  def abstract getImports();
  
  /**
  * getIncludes recurses through the included Xml Schemas,
  * writing their RDF descriptions to the RDF file
  * @return
  */
 def abstract getIncludes();
 
 /**
 * getFileDescription - generically describes the file
 * @return
 */
def getFileDescription() {
"""
${getRdfURI()} rdf:type ${rdfType};
  file:name "${name}"^^xsd:string ;
  file:absolutePath "${xmlFile.absolutePath}"^^xsd:string ;
  file:canonicalPath "${xmlFile.canonicalPath}"^^xsd:string .
"""
}

   /**
	* Generic RDF statements describing the Xml file
	* @return
	*/
   def getRdfDescription() {
   
	   return """
${getRdfURI()} rdf:type ${rdfType};
   rdfs:label "${name}"^^xsd:string ;
   dp:absolutePath "${xmlFile.absolutePath}"^^xsd:string .
"""
   }
   
   /**
   * RDF statement expressing the import of another the Xml file
   * @return
   */
  def getRdfImportStatement(String namespace, String schemaLocation) {
	  
	  def importURI = getImportURI(namespace,schemaLocation)
"""
${getRdfURI()} dp:imports ${importURI} ."""
  }

/**
* Get the RDF file that contains the description of XML file and its dependencies.
* If the RDF file does not already exist a new RDF file is created in the same 
* directory as the XML file with an extension of ".n3".
* RDF is written out using the N3 / TURTLE syntax.
* @return correlationIdFile
*/
   File getRdfFile() {
	   if (rdfFile == null && name != null) {

//		   String directory = System.getProperty('user.dir') + "/rdf"
//		   File parent = new File(directory)
//		   parent.mkdirs()
		   
		   File parent = new File(".")
		   String fileName = name + ".n3"
		   rdfFile = new File(parent,fileName);
		   rdfFile.delete()

		   println("Dependencies File: ${rdfFile.absolutePath}")

		   def header = """# Saved by TopBraid on Thu Oct 27 12:32:08 NZDT 2011
# baseURI: http://integration.msd.govt.nz/ontologies/datapower/example
# imports: http://integration.msd.govt.nz/ontologies/datapower

@prefix dp:      <http://integration.msd.govt.nz/ontologies/datapower#> .
@prefix dpx:     <http://integration.msd.govt.nz/ontologies/datapower/example/> .
@prefix owl:     <http://www.w3.org/2002/07/owl#> .
@prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#> .
@prefix xsd:     <http://www.w3.org/2001/XMLSchema#> .

<http://integration.msd.govt.nz/ontologies/datapower/example>
   rdf:type owl:Ontology ;
   owl:imports <http://integration.msd.govt.nz/ontologies/datapower> ;
   owl:versionInfo "Created with TopBraid Composer"^^xsd:string ."""

		   rdfFile.withWriter { file ->
			   file.writeLine(header)
		   }

	   }
	   return rdfFile;
   }
   
   String toString() { "name: ${name}, uri: ${getRdfURI()} absolutePath: ${xmlFile.absolutePath}, rdf:type: ${rdfType}" }
   
   String getFileExtension() { ".xml" }
		   
   /**
   * Rdfize files with the nominated file extension in the specified directory.
   * @param directory
   * @param fileExtension
   * @return
   */
   def int rdfizeDirectory(File directory, String fileExtension) {
   
	   println "Loading files from directory ${directory.absolutePath}"
	   
	   def pattern = ~/.*\${fileExtension}/
	   // def pattern = ~/.*\.xml/

	   println "File Extension: ${fileExtension}, Pattern: ${pattern}"
		
	   def count = 0
	   directory.eachFileRecurse {
		   // if (it.name =~ /.*\.wsdl/) {
		   if (it.name =~ /.*\${fileExtension}/) {
				println it;
				println "File: ${it.absolutePath}"
				count++
		   }
		 }
   

//		directory.eachFileMatch(pattern) {
//			println "File: ${it.absolutePath}"
//			count++
//		}
	   println "Number of files Loaded: ${count}"
	   return count
   }
	   
   /**
   * Rdfize files in the specified directory, selecting files using the fileExtension defined for the class.
   * @param directory
   * @return
   */
   def int rdfizeDirectory(File directory) {
	   return rdfizeDirectory(directory, fileExtension)
   }
   

}
