package com.mongodb.ereid.opustestbed;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;

import org.bson.Document;

import java.util.*;

// Generates a synthetic OPUS database based on a loose set of rules

public class TestbedGenerator 
{
	private static MongoClient mongoClient;
	private static MongoDatabase database;

	private static int numOpus;
	private static int functionCounter;
	private static int scoreCounter;

	private static String opusXML;
	private static TestbedUtils.TestbedProgress opusProgress;
	
	private static TestbedUtils.TestbedWriteBatch opusBatch = new TestbedUtils.TestbedWriteBatch();
	private static TestbedUtils.TestbedWriteBatch functionsBatch = new TestbedUtils.TestbedWriteBatch();
	private static TestbedUtils.TestbedWriteBatch scoreBatch = new TestbedUtils.TestbedWriteBatch();

	private static List<String> operatorIDs = Arrays.asList("kyeruva","mnguyen","ereid","test1","test2");
	private static List<String> descriptions = Arrays.asList("Impedance Checks in WARNING mode", "Opus Description 2", "Opus Description 3");
	private static List<String> commandNames = Arrays.asList("Set Motor Position", "Set User Action Settings.", "Heater Control", "GenMarkDx.NG.ScoreProtocolLibrary.Messages.DelayExecution");
	private static List<String> functionNames = Arrays.asList("01 Pre-Run", "02 Sample, Lysis, Oil, and Binding.", "02-2 Binding release", "05 Sample Prep and Primer resuspension (10 min)");
	private static List<String> coreFunctionNames = Arrays.asList("Blister Motor", "Pump", "Sleep", "Voltage");
	private static List<String> cartridgeModels = Arrays.asList("Cartridge6528R12.xml", "Cartridge1234R1.xml", "Cartridge323R5.xml", "OtherCartridge");
	private static Random rand = new Random();

	private static final int NOPARENT = -1;
	private static final int FIRSTLEVEL = -1;

	private static void createScores(int num, int parent) 
	{
		
		for (int i=0; i<num; i++)
		{
			Document scoreDoc = new Document();
			scoreDoc.put("_id", scoreCounter);
			scoreDoc.put("position", i+1);

			scoreDoc.put("opus", opusXML);
			scoreDoc.put("electrodeNumber", (rand.nextFloat() < 0.5)?1:-1);
			scoreDoc.put("commandCode", (int)(300*rand.nextFloat()));
			scoreDoc.put("functionID", parent);
			scoreDoc.put("commandName", commandNames.get(rand.nextInt(commandNames.size())));
			
			TestbedUtils.addInsertToBatch(scoreBatch, scoreDoc);
			TestbedUtils.addUpdateToBatch(functionsBatch, new Document("_id", parent), new Document("$addToSet", new Document("scores",scoreCounter)));
			
			scoreCounter++;
		}

	}

	private static void createFunctionLevel(int opusID, int parent, int remainingLevels) 
	{
		int numFunctions = TestbedUtils.expDistInteger(1, TestbedUtils.maxSubFunctions);
		
		for (int f = 0; f < numFunctions; f++)
		{
			Document functionsDoc = new Document();
			int numLevels = 0;

			if (parent == NOPARENT) 
			{
				numLevels = TestbedUtils.expDistInteger(1, TestbedUtils.maxFunctionLevels);
				functionsDoc.put("opusID", opusID);
			}
			else
			{
				functionsDoc.put("groupFunctionID", parent);
				numLevels = remainingLevels - 1;
			} 

			functionsDoc.put("_id", ++functionCounter);
			functionsDoc.put("libFunctionID", 0);
			functionsDoc.put("position", 1+(int)(6.0 * rand.nextFloat()));
			if (rand.nextFloat() < 0.8) functionsDoc.put("coreFunctionName", coreFunctionNames.get(rand.nextInt(coreFunctionNames.size())));
			functionsDoc.put("functionName", functionNames.get(rand.nextInt(functionNames.size())));
			functionsDoc.put("repetitionCount", f);
		    functionsDoc.put("revisionDate", TestbedUtils.pastDatetime());	
			functionsDoc.put("enabledFunction", true);
			if (rand.nextFloat() < 0.2) functionsDoc.put("hasParallelChildren", true);

			TestbedUtils.addInsertToBatch(functionsBatch, functionsDoc);

			if (parent == NOPARENT)
			{
				TestbedUtils.addUpdateToBatch(opusBatch, new Document("_id", opusID), new Document("$addToSet", new Document("functions", functionCounter)));
		//		System.out.println("      Add Function " + functionCounter + " to Opus " + opusID );
			}
			else 
			{
				TestbedUtils.addUpdateToBatch(functionsBatch, new Document("_id", parent), new Document("$addToSet", new Document("functions", functionCounter)));
		//		System.out.println("      Add Function " + functionCounter + " to Function " + parent );
			}

			if (numLevels > 1) 
				createFunctionLevel(opusID, functionCounter, numLevels);
			else if (parent != NOPARENT && numLevels == 1) 
			{
				int numScores = 1+(int)(TestbedUtils.maxScore * rand.nextFloat());
				createScores(numScores, functionCounter); 	
			}

		}

	}

	// Create top-level Opus document and underlying function/score tree
	private static void createOpus(int id) 
	{
		opusProgress.update(id);

		Document opusDoc = new Document();
		opusDoc.put("_id", id);
		opusDoc.put("operatorID", operatorIDs.get(rand.nextInt(operatorIDs.size())));
		opusDoc.put("opusName", "MongoTestOpus"+id);
		opusDoc.put("description", descriptions.get(rand.nextInt(descriptions.size())));
		opusDoc.put("version", 1+(int)(3.0 * rand.nextFloat()));
		opusDoc.put("cartridgeModel", cartridgeModels.get(rand.nextInt(cartridgeModels.size())));
		opusDoc.put("runDuration", (int)(300.0 * rand.nextFloat()));
		if (rand.nextFloat() < 0.2) opusDoc.put("used", true);
		opusDoc.put("editMode", 1+(int)(2.0 * rand.nextFloat()));
		if (rand.nextFloat() < 0.3) opusDoc.put("parentID", (int)(numOpus * rand.nextFloat()));
		if (rand.nextFloat() < 0.2) opusDoc.put("inactive", true);
		
		TestbedUtils.addInsertToBatch(opusBatch, opusDoc);

		createFunctionLevel(id, NOPARENT, FIRSTLEVEL);
	}
	
	public TestbedGenerator(TestbedOptions options)
	{
		numOpus = options.numOpii;
		functionCounter = numOpus + 1000;    // FunctionIDs start 1000 above max OpusID
		scoreCounter = functionCounter + (int)Math.pow(TestbedUtils.maxSubFunctions, TestbedUtils.maxFunctionLevels); // scoreIDs start here
		mongoClient = new MongoClient(new MongoClientURI(options.URI));
		database = mongoClient.getDatabase("OPUS");
		opusBatch.coll = database.getCollection("opus");
		functionsBatch.coll = database.getCollection("functions");
		scoreBatch.coll = database.getCollection("scoreCommands");
		
		System.out.println("   Function: Generate DB...");

		// Create a simple 50-field XML
		opusXML = "<XML><OPUS>\n";
		for (int j=0; j<50; j++) opusXML += "<FIELD" + j + ">Value" + rand.nextInt() + "</FIELD" + j + ">\n";
		opusXML += "</OPUS></XML>\n";
		
		// Drop all three collections first
		System.out.println("      Drop collections...");
		opusBatch.coll.drop();
		functionsBatch.coll.drop();
		scoreBatch.coll.drop();

		opusProgress = new TestbedUtils.TestbedProgress(numOpus, "      Create Data...");
		
		for (int i = 1; i <= numOpus; i++) createOpus(i);

		// Flush batch caches before closing connection
		TestbedUtils.flushBatch(opusBatch);
		TestbedUtils.flushBatch(functionsBatch);
		TestbedUtils.flushBatch(scoreBatch);
		
		// Warning: indexes should NEVER be created in application code in production
		System.out.println();
		System.out.println("      Create indexes...");
		opusBatch.coll.createIndex(Indexes.ascending("functions"));
		functionsBatch.coll.createIndex(Indexes.ascending("functions"));
		scoreBatch.coll.createIndex(Indexes.ascending("functionID"));

		mongoClient.close();
	}

}