package com.mongodb.ereid.opustestbed;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

import java.util.List;

import org.bson.Document;

public class TestbedChecker {
	private static MongoClient mongoClient;
	private static MongoDatabase database;

	private static MongoCollection<Document> opusColl;
	private static MongoCollection<Document> functionsColl;
	private static MongoCollection<Document> scoreColl;
	
	public TestbedChecker(TestbedOptions options)
	{
		mongoClient = new MongoClient(new MongoClientURI(options.URI));
		database = mongoClient.getDatabase("OPUS");
		opusColl = database.getCollection("opus");
		functionsColl = database.getCollection("functions");
		scoreColl = database.getCollection("scoreCommands");
		
		System.out.print("   Function: Check DB...");

		//   Opus
		//      Downwards: For each Opus's functions (if any), does each (if any) exist?

		try (MongoCursor<Document> cursor = opusColl.find().iterator())
		{
			TestbedUtils.TestbedProgress Progress = new TestbedUtils.TestbedProgress((int)opusColl.count(), "      Opii - Dangling Pointers...");
			int counter = 0;
			while (cursor.hasNext()) {
		    	Document opusDoc = cursor.next();
				List<Integer> childFuncIDs = (List<Integer>)opusDoc.get("functions");
				
				Progress.update(counter++);

				if (childFuncIDs != null) 
				{
					for (Integer childFuncID : childFuncIDs) {
						if ((functionsColl.count(new Document("_id", childFuncID))) != 1)
							System.out.println("         ERROR: Opus " + opusDoc.get("_id") + " / Function " + childFuncID + " does not match with a single Function document");
					}
				}
				else 
				{
					System.out.println("         WARNING: Opus " + opusDoc.get("_id") + " does not have any Functions");
				}
			}

		}
		
		//   Functions
		//      Upwards: Does each functions.opusID or functions.groupFunctionID point to an existing parent?

		try (MongoCursor<Document> cursor = functionsColl.find().iterator())
		{
			TestbedUtils.TestbedProgress Progress = new TestbedUtils.TestbedProgress((int)functionsColl.count(), "      Functions - 1:1...");
			int counter = 0;
			while (cursor.hasNext()) {
		    	Document functionDoc = cursor.next();
		    	Integer functionID = functionDoc.getInteger("_id");
		    	
				Progress.update(counter++);
		    	
		    	// Assumption: Each Function doc is 'owned' by exactly one Function or exactly one Opus

				if ((opusColl.count(new Document("functions", functionID)) + functionsColl.count(new Document("functions", functionID))) != 1)
				{
					System.out.println("         ERROR: Function " + functionID + " does not match with a single parent document");
				}
		    }		
		}
		
		//   Functions
		//      Downwards: Does each child document exist?
		
		try (MongoCursor<Document> cursor = functionsColl.find().iterator())
		{
			TestbedUtils.TestbedProgress Progress = new TestbedUtils.TestbedProgress((int)functionsColl.count(), "      Functions - Dangling Pointers...");
			int counter = 0;
			while (cursor.hasNext()) {
		    	Document functionDoc = cursor.next();
		    	int functionID = functionDoc.getInteger("_id");
				List<Integer> childFuncIDs = (List<Integer>)functionDoc.get("functions");
				List<Integer> scores = (List<Integer>)functionDoc.get("scores");
				
				Progress.update(counter++);

				if (childFuncIDs != null) 
				{
					for (Integer childFuncID : childFuncIDs) {
						if (functionsColl.count(new Document("_id", childFuncID)) != 1)
							System.out.println("         ERROR: Function " + functionID + " / " + childFuncID + " does not map to only one Function document");
					}
				}
				
				if (scores != null) 
				{
					for (Integer scoreID : scores) {
						if (scoreColl.count(new Document("_id", scoreID)) != 1)
							System.out.println("         ERROR: Function " + functionID + " / " + scoreID + " does not map to only one Score document");
					}
				}
			}

		}
		
		//   Score 
		//      Does each Score have exactly one parent? (1:1 Mapping Check, includes orphans)

		try (MongoCursor<Document> cursor = scoreColl.find().iterator())
		{
			TestbedUtils.TestbedProgress Progress = new TestbedUtils.TestbedProgress((int)scoreColl.count(), "      Scores - 1:1...");
			int counter = 0;
		    while (cursor.hasNext()) {
		    	Document scoreDoc = cursor.next();
		    	Integer scoreID = scoreDoc.getInteger("_id");
		    	
				Progress.update(counter++);
		    	
		    	// Assumption: Each Score is 'owned' by exactly one Function

				if (functionsColl.count(new Document("scores", scoreID)) != 1)
				{
					System.out.println("         ERROR: Score " + scoreID + " is not mapped to by exactly one Function");
				}
		    }		
		}
		
	    mongoClient.close();
	}
}
