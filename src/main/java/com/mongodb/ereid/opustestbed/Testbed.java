package com.mongodb.ereid.opustestbed;

import java.net.UnknownHostException;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.time.StopWatch;

public class Testbed 
{

	public static void main(String[] args) throws UnknownHostException 
    
	{  
		TestbedOptions options;
		
        System.out.println("Opus MongoDB Testbed - version 0.2");
        
        // Turn off all but the most important logging output
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("org.mongodb.driver").setLevel(Level.ERROR);
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        options = new TestbedOptions(args);
        // Quit after displaying help message
        if (options.helpOnly) {
        	return;
        }
        else if (options.generate) {
        	new TestbedGenerator(options);
            new TestbedChecker(options);
        } 
        else if (options.retrieve){
        	new TestbedRetriever(options);
        }
        else if (options.check){
            new TestbedChecker(options);
        }
        else if (options.delete){
            new TestbedDeleter(options);
        }

        stopWatch.split();
        System.out.println();
        System.out.println("   Operation completed in: " + stopWatch.toSplitString());
        
    }

}