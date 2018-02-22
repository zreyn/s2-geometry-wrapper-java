package edu.vt.bi.google.util;

import java.util.Arrays;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;


public class MongoInterfaceTest {

	public static void main(String args[]) {
		
		MongoClientURI connectionString = new MongoClientURI("mongodb://googler:q2wert@zreyn-opt.bi.vt.edu:27017/?authSource=google&authMechanism=SCRAM-SHA-1");
		MongoClient mongoClient = new MongoClient(connectionString);
		MongoDatabase database = mongoClient.getDatabase("google");
		MongoCollection<Document> collection = database.getCollection("test");
		
		Document doc = new Document("name", "MongoDB")
                .append("type", "database")
                .append("count", 1)
                .append("versions", Arrays.asList("v3.2", "v3.0", "v2.6"))
                .append("info", new Document("x", 203).append("y", 102));
		
		collection.insertOne(doc);
		
		Block<Document> printBlock = new Block<Document>() {
		     @Override
		     public void apply(final Document document) {
		         System.out.println(document.toJson());
		     }
		};

		collection.find(gt("count", 0)).forEach(printBlock);
		
		mongoClient.close();
	}
}
