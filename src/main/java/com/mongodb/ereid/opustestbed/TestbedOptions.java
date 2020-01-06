package com.mongodb.ereid.opustestbed;

import org.apache.commons.cli.CommandLine;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class TestbedOptions {
	
	String URI = "mongodb://localhost:27017/";
	int ID = 0;
	int numOpii = TestbedUtils.numOpii;
	boolean helpOnly = false;
	boolean generate = false;
	boolean retrieve = false;
	boolean check = false;
	boolean delete = false;

	TestbedOptions(String[] args)
	{
		CommandLineParser parser = new DefaultParser();
		
		Options cliopt = new Options();
		
		cliopt.addOption("g","generate",true,"Generate N Opus trees from scratch (default " + TestbedUtils.numOpii + ")");
		cliopt.addOption("r","ID",true,"Retrieve/print tree or sub-tree, given id");
		cliopt.addOption("h","help",false,"Show Help");
		cliopt.addOption("c","check",false,"Run sanity checks on Opus tree");
		cliopt.addOption("d","delete",true,"Remove any tree or sub-tree");
		cliopt.addOption("u","uri",true,"Connection String (URI) (default '" + URI + "' )");

		CommandLine cmd = null;
		try {
			cmd = parser.parse(cliopt, args);
		} catch (ParseException e) {
			System.out.println("Fatal Error: " + e.getMessage());
			return;
		}

		if(cmd.hasOption("g"))
		{
			generate = true;
			numOpii = Integer.parseInt(cmd.getOptionValue("g"));
		}
		
		if(cmd.hasOption("r"))
		{
			retrieve = true;
			ID = Integer.parseInt(cmd.getOptionValue("r"));
		}		
		
		if(cmd.hasOption("d"))
		{
			delete = true;
			ID = Integer.parseInt(cmd.getOptionValue("d"));
		}
		
		// automatically generate the help statement
		if(cmd.hasOption("h"))
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "Testbed", cliopt );
			helpOnly = true;
		}
		
		if(cmd.hasOption("u"))
		{
			URI = cmd.getOptionValue("u");
		}		
		
		if(cmd.hasOption("c"))	
		{
			check = true;
		}
		
	}
}