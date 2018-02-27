package edu.vt.bi.google.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQLException;

import com.google.common.geometry.S2CellId;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import edu.vt.bi.google.GeoToolsWrapper;
import edu.vt.bi.google.S2CellIdSet;
import edu.vt.bi.google.S2Feature;
import edu.vt.bi.google.S2Wrapper;

public class S2CoveringsToMongo {

	private MongoClientURI connectionString;
	private MongoClient mongoClient;
	private MongoDatabase database;
	private MongoCollection<Document> collection;
	public Block<Document> printBlock;


	public S2CoveringsToMongo(String connectionString, String database, String collection) {
		super();
		this.connectionString = new MongoClientURI(connectionString);
		this.mongoClient = new MongoClient(this.connectionString);
		this.database = this.mongoClient.getDatabase(database);
		this.collection = this.database.getCollection(collection);
		this.printBlock = new Block<Document>() {
			@Override
			public void apply(final Document document) {
				System.out.println(document.toJson());
			}
		};
	}


	public void insert(Document doc) {
		collection.insertOne(doc);
	}

	public FindIterable<Document> find(Bson query) {
		return collection.find(query);
	}

	public void close() {
		mongoClient.close();
	}

	public Document featureToDoc(S2Feature feature) {		
		Document doc = new Document();

		// Add all of the attributes at the top level
		HashMap<String,String> attributes = feature.getAttributes();
		Iterator<Entry<String,String>> it = attributes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String,String> pair = (Map.Entry<String,String>) it.next();
			doc.append(pair.getKey(), pair.getValue());
		}

		//		// Add the polygon
		//		doc.append("s2poly", value)

		//		// Add the cells
		//		doc.append("cells", feature.getCellIds().getTokens());

		return doc;
	}


	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		mongoClient.close();
	}


	public static void main(String[] args) throws IOException {
		String cellFile = args[0];
		String sourceFile = args[1];
		String filterCQL = args[2];
		int targetLevel = Integer.parseInt(args[3]);
		boolean interiorCoveringOnly = Boolean.parseBoolean(args[4]);
		int maxLevel = targetLevel + 10;
		if (maxLevel > 30) maxLevel = 30;

		HashSet<String> attrToIgnore = new HashSet<String>();
		attrToIgnore.add("the_geom");

		System.out.println("Reading file...");

		// Get the file
		FileDataStore store = null;
		SimpleFeatureSource featureSource = null;
		try {
			store = FileDataStoreFinder.getDataStore(new File(sourceFile));

			// Get the features and filter
			featureSource = store.getFeatureSource();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Iterate the features with a filter and put the features into a list.
		System.out.println("Getting features and converting shapes to S2Polygons...");

		ArrayList<S2Feature> s2features = new ArrayList<S2Feature>();
		try {
			if (filterCQL.length() < 1) filterCQL = null;
			s2features = GeoToolsWrapper.featuresToS2Features(featureSource, filterCQL, attrToIgnore);
		} catch (CQLException e1) {
			e1.printStackTrace();
		}


		// Get coverings for the features and write to mongo
		System.out.println("Getting coverings for "+s2features.size()+" polygons...");

		for (S2Feature feature : s2features) {

			ArrayList<S2CellId> covering = S2Wrapper.getCovering(feature.getS2poly(), targetLevel, maxLevel, interiorCoveringOnly);
			ArrayList<String> cells = new S2CellIdSet(covering).getTokens();

			System.out.println("    " + cells.size() + " cells");

			BufferedWriter out = null;
			try  
			{
				FileWriter fstream = new FileWriter(cellFile, true);
				out = new BufferedWriter(fstream);

				for (String fingerprint : cells) {
					out.write(fingerprint+",US\n");
				}

			}
			catch (IOException e)
			{
				System.err.println("Error: " + e.getMessage());
			}
			finally
			{
				if(out != null) {
					out.close();
				}
			}

		}


	}

}
