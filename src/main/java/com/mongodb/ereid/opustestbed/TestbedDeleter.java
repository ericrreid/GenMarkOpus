package com.mongodb.ereid.opustestbed;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.*;
import org.bson.Document;

import java.util.*;

public class TestbedDeleter {
	private static MongoClient mongoClient;
	private static MongoDatabase database;

	private static MongoCollection<Document> opusColl;
	private static MongoCollection<Document> functionsColl;
	private static MongoCollection<Document> scoreColl;
	
	public void TestbedFunctionDeleter(Document func)
	{
		Integer funcID = func.getInteger("_id");
	    Document parentFunction = functionsColl.find(eq("_id", func.getInteger("groupFunctionID"))).first();
	    Document parentOpus = null;
		List<Integer> childFuncIDs = (List<Integer>)func.get("functions");
		List<Integer> scores = (List<Integer>)func.get("scores");
	    
	    if (parentFunction == null) {
		    parentOpus = functionsColl.find(eq("opusID", funcID)).first();
	    }
		assert parentFunction != null || parentOpus != null : "ERROR: Cannot find parent for Function" + funcID;
		
		// TODO: Start Transaction

		// Remove parent's link to this Function
		// a) Parent is a function itself
		if (parentFunction != null) 
		{
			functionsColl.updateOne(new Document("_id", parentFunction.get("_id")), 
					new Document("$pull", new Document("functions", funcID)));
		}
		
		// b) Parent is an Opus
		if (parentOpus != null)
		{	
			opusColl.updateOne(new Document("_id", parentOpus.get("_id")), 
					new Document("$pull", new Document("functions", funcID)));
		}
	    
		// Remove all children
		
		if (childFuncIDs != null) 
		{
			for (Integer childFuncID : childFuncIDs) {

				Document child = functionsColl.find(eq("_id", childFuncID)).first();
				TestbedFunctionDeleter(child);
			}
		}
		
		if (scores != null) 
		{
			for (Integer scoreID : scores) {

				Document score = scoreColl.find(eq("_id", scoreID)).first();
				TestbedScoreDeleter(score);
			}
		}
			
		// Remove this Function - must be done last
		System.out.println("Delete Function " + funcID);
		functionsColl.deleteOne(new Document("_id", funcID));
		
		// TODO: End Transaction

	}
	
	// Assuming only one level of Scores, each with only one parent Function
	public void TestbedScoreDeleter(Document score)
	{
		Integer scoreID = score.getInteger("_id");
	    Document parentFunction = functionsColl.find(eq("scores", scoreID)).first();
		assert score == null : "ERROR: Score" + scoreID + "cannot be found";
		assert parentFunction == null : "ERROR: Score" + scoreID + "does not have a parent Function";
		
		// TODO: Start Transaction
		
		// Remove parent's link to this Score
		functionsColl.updateOne(new Document("_id", parentFunction.get("_id")), 
				new Document("$pull", new Document("scores", scoreID)));
		
		// Remove this Score
		System.out.println("Delete Score " + scoreID);
		scoreColl.deleteOne(new Document("_id", scoreID));
		
		// TODO: End Transaction

	}
	
	public TestbedDeleter(TestbedOptions options)
	{
		mongoClient = new MongoClient(new MongoClientURI(options.URI));
		database = mongoClient.getDatabase("OPUS");
		opusColl = database.getCollection("opus");
		functionsColl = database.getCollection("functions");
		scoreColl = database.getCollection("scoreCommands");
		
		if (TestbedUtils.isOpus(options.ID))
		{
			TestbedUtils.indentedOutput(">>>Delete Opus Tree " + options.ID + "...", 0);

			Document opusDoc = opusColl.find(eq("_id", options.ID)).first();
			if (opusDoc == null)
			{
				System.out.println("ERROR: No such Opus found");
				return;
			}
			List<Integer> funcIDs = (List<Integer>)opusDoc.get("functions");

			if (funcIDs != null)
			{
				for (Document doc : functionsColl.find(in("_id", funcIDs)))
				{
					TestbedFunctionDeleter(doc);
				}
			}
			
			// Remove this Opus
			System.out.println("Delete Opus " + options.ID);
			opusColl.deleteOne(new Document("_id", options.ID));
		}
		else if (TestbedUtils.isFunction(options.ID))
		{
			TestbedUtils.indentedOutput(">>>Delete Function Tree " + options.ID + "...", 0);

			Document functionsDoc = functionsColl.find(eq("_id", options.ID)).first();
			
			TestbedFunctionDeleter(functionsDoc);
		}
		else if (TestbedUtils.isScore(options.ID))
		{
			TestbedUtils.indentedOutput(">>>Delete Score " + options.ID + "...", 0);

			Document scoreDoc = scoreColl.find(eq("_id", options.ID)).first();
			
			TestbedScoreDeleter(scoreDoc);

		}

	    mongoClient.close();
	}
}