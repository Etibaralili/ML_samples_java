package org.ml.java;

import org.apache.spark.ml.clustering.KMeansModel;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import java.util.ArrayList;
import java.util.Arrays;

public class KMeansExpt {
	public static void main(String[] args) {

		SparkSession spark = SparkSession.builder()
				.master("local[8]")
				.appName("KMeansExpt")
				.getOrCreate();
 
		// Load and parse data
		String filePath = "data/covtypeNorm.csv";

		// Loads data.
		Dataset<Row> inDataset = spark.read()
				.format("com.databricks.spark.csv")
				.option("header", "true")
				.option("inferSchema", true)
				.load(filePath);
		ArrayList<String> inputColsList = new ArrayList<String>(Arrays.asList(inDataset.columns()));
		
		//Make single features column for feature vectors 
		inputColsList.remove("class");
		String[] inputCols = inputColsList.parallelStream().toArray(String[]::new);
		
		//Prepare dataset for training with all features in "features" column
		VectorAssembler assembler = new VectorAssembler().setInputCols(inputCols).setOutputCol("features");
		Dataset<Row> dataset = assembler.transform(inDataset);

		KMeans kmeans = new KMeans().setK(27).setSeed(1L);
		KMeansModel model = kmeans.fit(dataset);

		// Evaluate clustering by computing Within Set Sum of Squared Errors.
		double WSSSE = model.computeCost(dataset);
		System.out.println("Within Set Sum of Squared Errors = " + WSSSE);

		// Shows the result.
		Vector[] centers = model.clusterCenters();
		System.out.println("Cluster Centers: ");
		for (Vector center: centers) {
		  System.out.println(center);
		}
		
		spark.stop();
	}
}
