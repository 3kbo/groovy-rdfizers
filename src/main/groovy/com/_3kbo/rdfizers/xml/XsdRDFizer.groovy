package com._3kbo.rdfizers.xml

import org.apache.log4j.*
import groovy.util.logging.*

/**
* The XsdRDFizer class provides a command line interface for RDFizing Xml Schema (xsd) files.
* The command line interface provides options for selecting xsd files to RDFize.
* It utilizes the XsdFile class to provide RDF descriptions of each selected xsd file.
* @author Richard Hancock
*
*/
@Log4j
class XsdRDFizer {
	
	private static Logger LOG = Logger.getLogger(com._3kbo.rdfizers.xml.XsdRDFizer.class)
	
	def execute() {
		log.debug 'Execute HelloWorld.'
		log.info 'Simple sample to show log field is injected.'
		
		LOG.debug 'A Execute HelloWorld.'
		LOG.info 'A Simple sample to show log field is injected.'
	}

	static main(args) {
		LOG.debug "XsdRDFizer"
		
		final def usageString =
		"""XsdRDFizer provides the following command line options for creating RDF descriptions of Xml Schema (.xsd) files.
		
		e.g: java -jar groovy-datapower-1.0.0-jar-with-dependencies.jar -n proxyName -f export.xml	"""
						
		def cli = new CliBuilder(usage : usageString)
		cli.h( longOpt: 'help', required: false, 'show usage information' )
		cli.l( longOpt: 'list', argName: 'dir', required: true, args: 1, 'list Xml Schemas in the directory')
		cli.d( longOpt: 'directory', argName: 'dir', required: true, args: 1, 'create RDF descriptions of all the Xml Schemas files in the directory ' )
//		cli.d( longOpt: 'directory', argName: 'dir', required: true, argName: 'filename', required: true, args: 2, 'create RDF descriptions of all the WSDL files in the directory ' )
		cli.f( longOpt: 'file', argName: 'path', required: true, args: 1, 'Full path to the Xml Schema (.xsd) file')
		cli.r( longOpt: 'relative', argName: 'path', required: true, args: 1, 'Relative path to the Xml Schema (.xsd) file')
		println "\nXsdRDFizer - arguments:"
		for (arg in args) println "arg: " + arg
		
		def opt = cli.parse(args)
		if (!opt) { return
		}
		
		if (opt.h) {
			cli.usage();
			return
		}
		
		XsdRDFizer x = new XsdRDFizer()
		x.execute()
		
		String path = "/Users/richardhancock/projects/JavaThing/ear-schemas"
		
		new File(path).eachFileRecurse {
			if (it.name =~ /.*\.xsd/) println it;
		}
		
		// Specific file
		
		// File path relative to parent
	}

}
