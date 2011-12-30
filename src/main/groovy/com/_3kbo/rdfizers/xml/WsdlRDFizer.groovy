package com._3kbo.rdfizers.xml

import java.io.File;

/**
 * The WsdlRDFizer class provides a command line interface for RDFizing wsdl files.
 * The command line interface provides options for selecting wsdl files to RDFize.
 * It utilizes the WsdlFile class to provide RDF descriptions of each selected wsdl file. 
 * @author Richard Hancock
 *
 */
class WsdlRDFizer {
	
	static boolean isWsdl(File file) {
		file.name.endsWith(".wsdl")
	}
	
/**
* Write the RDF header to the RDF file that will contain the descriptions of the WSDL and XML Schema files.
* RDF is written out using the N3 / TURTLE syntax.
* @return correlationIdFile
*/
   static def writeRdfHeader(File rdfFile) {

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

   		rdfFile.withWriter { file -> file.writeLine(header) }
   }

	static main(args) {
		
		println("WsdlRDFizer ...")
		
		final def usageString =
		"""WsdlRDFizer provides the following command line options for creating RDF descriptions of wsdl files.
		
		e.g: java -jar groovy-datapower-1.0.0-jar-with-dependencies.jar -n proxyName -f export.xml	"""
						
		def cli = new CliBuilder(usage : usageString)
		cli.h( longOpt: 'help', required: false, 'show usage information' )		
		cli.l( longOpt: 'list', argName: 'list', required: false, args: 1, 'List all the WSDL files recursively in the specified directory <dir>.')
		cli.d( longOpt: 'dir', argName: 'dir', required: false, args: 1, 'Create RDF descriptions for all the WSDL files in the directory, writing the RDF descroptions to the output file if specified, otherwise write to standard out' )
		cli.f( longOpt: 'file', argName: 'file', required: false, args: 1, 'Full path to a specific WSDL (.wsdl) file')
		cli.r( longOpt: 'relative', argName: 'relative', required: false, args: 1, 'Relative path to the Wsdl (.wsdl) file. The path is relative to the current directory')
		cli.o( longOpt: 'output', argName: 'output', required: false, args: 1, 'The output file RDF descriptions are written to.')
		
		def opt = cli.parse(args)
		
		if (!opt || opt.h) { 
			cli.usage();
			return
		}
		
		if (opt.l) {
			println "Listing WSDLs in: ${opt.list}"
		
			int i = 0
			new File(opt.list).eachFileRecurse {
				if (it.name =~ /.*\.wsdl/) {
					println "${++i}: ${it}";
				}
			}
		}
		else if (opt.d) {
			println "Creating RDF descriptions for all WSDLs in the directory: ${opt.dir}"
			
			if (opt.o) {
				println "Writing to output file: ${opt.output}"
				File output = new File(opt.output)
				writeRdfHeader(output)
				
				int i = 0
				new File(opt.dir).eachFileRecurse {
					if (it.name =~ /.*\.wsdl/) {
						println "${++i}: ${it}";
						def wsdlFile = new WsdlFile(new File(it.toString()))
						wsdlFile.export(output)
					}
				}
			}
						
		}
		else if (opt.f) {
			println "Creating an RDF description for: ${opt.file}"
			
			File wsdl = new File(opt.f)
			
			if (isWsdl(wsdl)) {
				if (opt.o) {
					println "Writing to output file: ${opt.output}"					
					File output = new File(opt.output)
					writeRdfHeader(output)
					def wsdlFile = new WsdlFile(wsdl)
					wsdlFile.export(output)
				}
				else {
					println "Creating default output file"
					def wsdlFile = new WsdlFile(wsdl)
					wsdlFile.export()
				}
			}
			else {
				println "${opt.file} is not a WSDL"
			}
		}
		else if (opt.r) {
			println "Creating an RDF description for: ${opt.relative}"
			
			if (isWsdl(opt.relative)) {
				if (opt.o) {
					println "Writing to output file: ${opt.output}"					
					File output = new File(opt.output)
					writeRdfHeader(output)
					def wsdlFile = new WsdlFile(new File("."), opt.relative)
					wsdlFile.export(output)
				}
				else {
					println "Creating default output file"
					def wsdlFile = new WsdlFile(new File("."), opt.relative)
					wsdlFile.export()
				}
			}
			else {
				println "${opt.relative} is not a WSDL"
			}
		}
	
	}

}
