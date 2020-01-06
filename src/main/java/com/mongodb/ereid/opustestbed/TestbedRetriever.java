package com.mongodb.ereid.opustestbed;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.*;
import org.bson.Document;

import java.util.*;

public class TestbedRetriever {
	private static MongoClient mongoClient;
	private static MongoDatabase database;

	private static MongoCollection<Document> opusColl;
	private static MongoCollection<Document> functionsColl;
	private static MongoCollection<Document> scoreColl;
	
	public void TestbedFunctionRetriever(Document func, int level)
	{
		List<Integer> childFuncIDs = (List<Integer>)func.get("functions");
		List<Integer> scores = (List<Integer>)func.get("scores");
		
		TestbedUtils.indentedOutput("FUNCTION: " + func.toJson() + "...", level);

		if (childFuncIDs != null) 
		{
			for (Integer childFuncID : childFuncIDs) {

				Document child = functionsColl.find(eq("_id", childFuncID)).first();
				TestbedFunctionRetriever(child, level+1);
			}
		}
		
		if (scores != null) 
		{
			for (Integer scoreID : scores) {

				Document score = scoreColl.find(eq("_id", scoreID)).first();
				TestbedUtils.indentedOutput("SCORE: " + score.toJson() + "...", level+1);
			}
		}
	}
	
	public TestbedRetriever(TestbedOptions options)
	{
		mongoClient = new MongoClient(new MongoClientURI(options.URI));
		database = mongoClient.getDatabase("OPUS");
		opusColl = database.getCollection("opus");
		functionsColl = database.getCollection("functions");
		scoreColl = database.getCollection("scoreCommands");
		
		if (TestbedUtils.isOpus(options.ID))
		{
			TestbedUtils.indentedOutput(">>>Retrieve Opus Tree " + options.ID + "...", 0);

			// Note: w/Java driver, find().first() is a proper way to implement findOne()
			Document opusDoc = opusColl.find(eq("_id", options.ID)).first();
			if (opusDoc == null)
			{
				System.out.println("ERROR: No such Opus found");
				return;
			}
			TestbedUtils.indentedOutput("OPUS TREE: " + opusDoc.toJson() + "...", 0);

			List<Integer> funcIDs = (List<Integer>)opusDoc.get("functions");

			if (funcIDs != null)
			{
				for (Document doc : functionsColl.find(in("_id", funcIDs)))
				{
					TestbedFunctionRetriever(doc, 1);
				}
			}
		}
		else if (TestbedUtils.isFunction(options.ID))
		{
			TestbedUtils.indentedOutput(">>>Retrieve Function Tree " + options.ID + "...", 0);

			Document functionsDoc = functionsColl.find(eq("_id", options.ID)).first();
			TestbedUtils.indentedOutput("FUNCTION TREE: " + functionsDoc.toJson() + "...", 0);
			
			TestbedFunctionRetriever(functionsDoc, 1);
		}
		else if (TestbedUtils.isScore(options.ID))
		{
			TestbedUtils.indentedOutput(">>>Retrieve Score " + options.ID + "...", 0);

			Document scoreDoc = scoreColl.find(eq("_id", options.ID)).first();
			TestbedUtils.indentedOutput("SCORE: " + scoreDoc.toJson() + "...", 0);			
		}

	    mongoClient.close();
	}
}
