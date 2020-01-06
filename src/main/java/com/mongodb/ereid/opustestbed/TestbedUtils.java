package com.mongodb.ereid.opustestbed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.WriteModel;

public class TestbedUtils {
	
	public static int maxSubFunctions = 10;     
	public static int maxFunctionLevels = 5; // Careful - the combinatorics skyrocket as this increases
	public static int maxScore = 10;
	public static int numOpii = 20000;
	private static final int WRITEBATCHSIZE = 1000; // 1000 operations standard for Java driver

	private static Random rand = new Random();
	
	public static class TestbedProgress {
		private int total = 0;
		private int tenPercent = 0;
		private int progress = 0;
		private boolean first = true;
		private String header;
		
		public TestbedProgress(int max, String s)
		{
			total = max;
			tenPercent = total / 10;
			header = s;
			System.out.println();
		}
		
		public void update(int current)
		{
			if (first) 
			{
				System.out.print(header);
				first = false;
			}

			if (current%tenPercent == 0) 
		    {
				System.out.print(progress+"%...");
				progress += 10;
		    }
		}
	}
	
	public static class TestbedWriteBatch {
		public MongoCollection<Document> coll;
		public int counter = 0;
		public List<WriteModel<Document>> pendingDocs = new ArrayList<WriteModel<Document>>();
	}

	public static void addInsertToBatch(TestbedWriteBatch batch, Document doc)
	{
		batch.pendingDocs.add(new InsertOneModel<Document>(doc));
		if (++(batch.counter) >= WRITEBATCHSIZE) flushBatch(batch);
	}

	public static void addUpdateToBatch(TestbedWriteBatch batch, Document filter, Document update)
	{
		batch.pendingDocs.add(new UpdateOneModel<Document>(filter, update));
		if (++(batch.counter) >= WRITEBATCHSIZE) flushBatch(batch);
	}

	public static void addDeleteToBatch(TestbedWriteBatch batch, Document filter)
	{
		batch.pendingDocs.add(new DeleteOneModel<Document>(filter));
		if (++(batch.counter) >= WRITEBATCHSIZE) flushBatch(batch);
	}

	public static void flushBatch(TestbedWriteBatch batch)
	{
		if (batch.counter > 0)
		{
			batch.coll.bulkWrite(batch.pendingDocs, new BulkWriteOptions().ordered(true));
			batch.counter = 0;
			batch.pendingDocs = null;
			batch.pendingDocs = new ArrayList<WriteModel<Document>>();
		}
	}

	public static int expDistInteger(int min, int max)
	{
		return min + (int)Math.pow(max-min,rand.nextFloat());
	}	
	
	public static Date pastDatetime()
	{
	    long endMillis = new Date().getTime();
	    long randLong = Math.abs(rand.nextLong());
	    long limit = 5L*365L*24L*60L*60L*1000L; // Five years
	    long bounded = randLong % limit;
	    long startMillis = endMillis - bounded; 
	    long randomMillisSinceEpoch = ThreadLocalRandom.current().nextLong(startMillis, endMillis);
	 
	    return new Date(randomMillisSinceEpoch);

	}
	
	public static boolean isOpus(int ID)
	{
		return (ID <= numOpii);
	}
	
	public static boolean isFunction(int ID)
	{
		return (ID > numOpii && ID <= (int)Math.pow(TestbedUtils.maxSubFunctions, TestbedUtils.maxFunctionLevels));
	}
	
	public static boolean isScore(int ID)
	{
		return (ID > (int)Math.pow(TestbedUtils.maxSubFunctions, TestbedUtils.maxFunctionLevels));
	}
	
	public static void indentedOutput(String s, int level)
	{
		for (int r=level; r>0; r--) System.out.print("  ");
		System.out.println(s);
	}
}
