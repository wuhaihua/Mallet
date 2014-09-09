package cc.mallet.topics.tui;


//import cc.mallet.util.CommandOption;
//import cc.mallet.util.Randoms;
import cc.mallet.types.*;
import cc.mallet.util.*;
//import cc.mallet.types.Alphabet;
//import cc.mallet.types.InstanceList;
//import cc.mallet.types.FeatureSequence;
//import cc.mallet.types.Metric;
//import cc.mallet.types.NormalizedDotProductMetric;
import cc.mallet.topics.*;
import cc.mallet.cluster.Clustering;
import cc.mallet.cluster.KMeans;
import cc.mallet.pipe.iterator.DBInstanceIterator;

import java.io.*;

import java.util.*;
import java.util.regex.*;
import java.net.*;

public class Vectors2Cluster {
	
	
	static CommandOption.String inputFile = new CommandOption.String
			(Vectors2Cluster.class, "input", "FILENAME", true, null,
			 "The filename from which to read the list of training instances.  Use - for stdin.  " +
			 "The instances must be FeatureSequence or FeatureSequenceWithBigrams, not FeatureVector", null);
	
	static CommandOption.SpacedStrings languageInputFiles = new CommandOption.SpacedStrings
			(Vectors2Cluster.class, "language-inputs", "FILENAME [FILENAME ...]", true, null,
			"Filenames for polylingual topic model. Each language should have its own file, " +
			"with the same number of instances in each file. If a document is missing in " + 
			"one language, there should be an empty instance.", null);
	
	static CommandOption.String testingFile = new CommandOption.String
			(Vectors2Cluster.class, "testing", "FILENAME", false, null,
			 "The filename from which to read the list of instances for empirical likelihood calculation.  Use - for stdin.  " +
			 "The instances must be FeatureSequence or FeatureSequenceWithBigrams, not FeatureVector", null);
		
	static CommandOption.String outputModelFilename = new CommandOption.String
			(Vectors2Cluster.class, "output-model", "FILENAME", true, null,
			 "The filename in which to write the binary topic model at the end of the iterations.  " +
			 "By default this is null, indicating that no file will be written.", null);

	static CommandOption.String inputModelFilename = new CommandOption.String
			(Vectors2Cluster.class, "input-model", "FILENAME", true, null,
			 "The filename from which to read the binary topic model to which the --input will be appended, " +
			 "allowing incremental training.  " +
			 "By default this is null, indicating that no file will be read.", null);

	static CommandOption.String inferencerFilename = new CommandOption.String
			(Vectors2Cluster.class, "inferencer-filename", "FILENAME", true, null,
			 "A topic inferencer applies a previously trained topic model to new documents.  " +
			 "By default this is null, indicating that no file will be written.", null);

	static CommandOption.String evaluatorFilename = new CommandOption.String
			(Vectors2Cluster.class, "evaluator-filename", "FILENAME", true, null,
			 "A held-out likelihood evaluator for new documents.  " +
			 "By default this is null, indicating that no file will be written.", null);

	static CommandOption.String stateFile = new CommandOption.String
			(Vectors2Cluster.class, "output-state", "FILENAME", true, null,
			 "The filename in which to write the Gibbs sampling state after at the end of the iterations.  " +
			 "By default this is null, indicating that no file will be written.", null);

	static CommandOption.String topicKeysFile = new CommandOption.String
			(Vectors2Cluster.class, "output-topic-keys", "FILENAME", true, null,
			 "The filename in which to write the top words for each topic and any Dirichlet parameters.  " +
			 "By default this is null, indicating that no file will be written.", null);

	static CommandOption.String topicWordWeightsFile = new CommandOption.String
			(Vectors2Cluster.class, "topic-word-weights-file", "FILENAME", true, null,
			 "The filename in which to write unnormalized weights for every topic and word type.  " +
			 "By default this is null, indicating that no file will be written.", null);

	static CommandOption.String wordTopicCountsFile = new CommandOption.String
			(Vectors2Cluster.class, "word-topic-counts-file", "FILENAME", true, null,
			 "The filename in which to write a sparse representation of topic-word assignments.  " +
			 "By default this is null, indicating that no file will be written.", null);

	static CommandOption.String topicReportXMLFile = new CommandOption.String
			(Vectors2Cluster.class, "xml-topic-report", "FILENAME", true, null,
			 "The filename in which to write the top words for each topic and any Dirichlet parameters in XML format.  " +
			 "By default this is null, indicating that no file will be written.", null);

	static CommandOption.String topicPhraseReportXMLFile = new CommandOption.String
			(Vectors2Cluster.class, "xml-topic-phrase-report", "FILENAME", true, null,
			"The filename in which to write the top words and phrases for each topic and any Dirichlet parameters in XML format.  " +
			"By default this is null, indicating that no file will be written.", null);

	static CommandOption.String docTopicsFile = new CommandOption.String
			(Vectors2Cluster.class, "output-doc-topics", "FILENAME", true, null,
			"The filename in which to write the topic proportions per document, at the end of the iterations.  " +
			"By default this is null, indicating that no file will be written.", null);

	static CommandOption.Double docTopicsThreshold = new CommandOption.Double
			(Vectors2Cluster.class, "doc-topics-threshold", "DECIMAL", true, 0.0,
			 "When writing topic proportions per document with --output-doc-topics, " +
			 "do not print topics with proportions less than this threshold value.", null);

	static CommandOption.Integer docTopicsMax = new CommandOption.Integer
			(Vectors2Cluster.class, "doc-topics-max", "INTEGER", true, -1,
			 "When writing topic proportions per document with --output-doc-topics, " +
			 "do not print more than INTEGER number of topics.  "+
			 "A negative value indicates that all topics should be printed.", null);

	static CommandOption.Integer numTopics = new CommandOption.Integer
			(Vectors2Cluster.class, "num-topics", "INTEGER", true, 10,
			 "The number of topics to fit.", null);

	static CommandOption.Integer numThreads = new CommandOption.Integer
			(Vectors2Cluster.class, "num-threads", "INTEGER", true, 1,
			 "The number of threads for parallel training.", null);

	static CommandOption.Integer numIterations = new CommandOption.Integer
			(Vectors2Cluster.class, "num-iterations", "INTEGER", true, 1000,
			 "The number of iterations of Gibbs sampling.", null);

	static CommandOption.Integer randomSeed = new CommandOption.Integer
			(Vectors2Cluster.class, "random-seed", "INTEGER", true, 0,
			 "The random seed for the Gibbs sampler.  Default is 0, which will use the clock.", null);

	static CommandOption.Integer topWords = new CommandOption.Integer
			(Vectors2Cluster.class, "num-top-words", "INTEGER", true, 20,
			 "The number of most probable words to print for each topic after model estimation.", null);

	static CommandOption.Integer showTopicsInterval = new CommandOption.Integer
			(Vectors2Cluster.class, "show-topics-interval", "INTEGER", true, 50,
			 "The number of iterations between printing a brief summary of the topics so far.", null);

	static CommandOption.Integer outputModelInterval = new CommandOption.Integer
			(Vectors2Cluster.class, "output-model-interval", "INTEGER", true, 0,
			 "The number of iterations between writing the model (and its Gibbs sampling state) to a binary file.  " +
			 "You must also set the --output-model to use this option, whose argument will be the prefix of the filenames.", null);

	static CommandOption.Integer outputStateInterval = new CommandOption.Integer
			(Vectors2Cluster.class, "output-state-interval", "INTEGER", true, 0,
			 "The number of iterations between writing the sampling state to a text file.  " +
			 "You must also set the --output-state to use this option, whose argument will be the prefix of the filenames.", null);

	static CommandOption.Integer optimizeInterval = new CommandOption.Integer
			(Vectors2Cluster.class, "optimize-interval", "INTEGER", true, 0,
			 "The number of iterations between reestimating dirichlet hyperparameters.", null);

	static CommandOption.Integer optimizeBurnIn = new CommandOption.Integer
			(Vectors2Cluster.class, "optimize-burn-in", "INTEGER", true, 200,
			 "The number of iterations to run before first estimating dirichlet hyperparameters.", null);

	static CommandOption.Boolean useSymmetricAlpha = new CommandOption.Boolean
			(Vectors2Cluster.class, "use-symmetric-alpha", "true|false", false, false,
			 "Only optimize the concentration parameter of the prior over document-topic distributions. This may reduce the number of very small, poorly estimated topics, but may disperse common words over several topics.", null);

	static CommandOption.Boolean useNgrams = new CommandOption.Boolean
			(Vectors2Cluster.class, "use-ngrams", "true|false", false, false,
			 "Rather than using LDA, use Topical-N-Grams, which models phrases.", null);

	static CommandOption.Boolean usePAM = new CommandOption.Boolean
			(Vectors2Cluster.class, "use-pam", "true|false", false, false,
			 "Rather than using LDA, use Pachinko Allocation Model, which models topical correlations." +
			 "You cannot do this and also --use-ngrams.", null);

	static CommandOption.Double alpha = new CommandOption.Double
			(Vectors2Cluster.class, "alpha", "DECIMAL", true, 50.0,
			 "Alpha parameter: smoothing over topic distribution.",null);

	static CommandOption.Double beta = new CommandOption.Double
			(Vectors2Cluster.class, "beta", "DECIMAL", true, 0.01,
			 "Beta parameter: smoothing over unigram distribution.",null);

	static CommandOption.Double gamma = new CommandOption.Double
			(Vectors2Cluster.class, "gamma", "DECIMAL", true, 0.01,
			 "Gamma parameter: smoothing over bigram distribution",null);

	static CommandOption.Double delta = new CommandOption.Double
			(Vectors2Cluster.class, "delta", "DECIMAL", true, 0.03,
			 "Delta parameter: smoothing over choice of unigram/bigram",null);

	static CommandOption.Double delta1 = new CommandOption.Double
			(Vectors2Cluster.class, "delta1", "DECIMAL", true, 0.2,
			 "Topic N-gram smoothing parameter",null);

	static CommandOption.Double delta2 = new CommandOption.Double
			(Vectors2Cluster.class, "delta2", "DECIMAL", true, 1000.0,
			 "Topic N-gram smoothing parameter",null);
		
	static CommandOption.Integer pamNumSupertopics = new CommandOption.Integer
			(Vectors2Cluster.class, "pam-num-supertopics", "INTEGER", true, 10,
			 "When using the Pachinko Allocation Model (PAM) set the number of supertopics.  " +
			 "Typically this is about half the number of subtopics, although more may help.", null);

	static CommandOption.Integer pamNumSubtopics = new CommandOption.Integer
			(Vectors2Cluster.class, "pam-num-subtopics", "INTEGER", true, 20,
			 "When using the Pachinko Allocation Model (PAM) set the number of subtopics.", null);
	
	static CommandOption.Integer numClusters = new CommandOption.Integer
			(Vectors2Cluster.class, "k", "[# of clusters]", true, 12,
			 "The number of clusters into which articles are grouped.", null);	
	
		

	
	
	public static void main (String[] args) throws java.io.IOException
	{
		//--input autorial_all2.mallet --num-topics 5 --optimize-interval 20 
		//--output-doc-topics autorial_compostion_all2.txt --k 5
		String[] inputArgs = {"--input","autorial_all3.mallet","--num-topics","5",
				"--optimize-interval","20","--output-doc-topics","autorial_compostion_all2.txt",
				"--k","5"};
		preMain(inputArgs);

	}


	public static InstanceList[] preMain(String[] args) throws IOException {
		// TODO Auto-generated method stub
		CommandOption.setSummary (Vectors2Cluster.class,
				  "A tool for estimating, saving and printing diagnostics for topic Cluster, LDA + KMeans.");
		CommandOption.process (Vectors2Cluster.class, args);
		
		//Only support LDA as Topic Model at this moment
		ParallelTopicModel topicModel = null;
		
		InstanceList training = null;
		
		try {
			if (inputFile.value.startsWith("db:")) {
				training = DBInstanceIterator.getInstances(inputFile.value.substring(3));
			}
			else {
				training = InstanceList.load (new File(inputFile.value));
			}
		} catch (Exception e) {
			System.err.println("Unable to restore instance list " + 
							   inputFile.value + ": " + e);
			System.exit(1);					
		}
		
		
		System.out.println ("Data loaded.");

		if (training.size() > 0 &&
			training.get(0) != null) {
			Object data = training.get(0).getData();
			if (! (data instanceof FeatureSequence)) {
				System.err.println("Topic modeling currently only supports feature sequences: use --keep-sequence option when importing data.");
				System.exit(1);
			}
		}

		topicModel = new ParallelTopicModel (numTopics.value, alpha.value, beta.value);
		if (randomSeed.value != 0) {
			topicModel.setRandomSeed(randomSeed.value);
		}

		topicModel.addInstances(training);
		
		
		
		topicModel.setTopicDisplay(showTopicsInterval.value, topWords.value);
	

		topicModel.setNumIterations(numIterations.value);
		topicModel.setOptimizeInterval(optimizeInterval.value);
		topicModel.setBurninPeriod(optimizeBurnIn.value);
		topicModel.setSymmetricAlpha(useSymmetricAlpha.value);

		if (outputStateInterval.value != 0) {
			topicModel.setSaveState(outputStateInterval.value, stateFile.value);
		}

		if (outputModelInterval.value != 0) {
			topicModel.setSaveSerializedModel(outputModelInterval.value, outputModelFilename.value);
		}

		topicModel.setNumThreads(numThreads.value);			
		
		
		topicModel.estimate();
		
		InstanceList instances = topicModel.OutputDocumentTopics();
		
		Metric metric = new NormalizedDotProductMetric(); // cosine similarity
		
		KMeans kmeans = new KMeans(instances.getPipe(), numClusters.value, metric, KMeans.EMPTY_DROP);
		Clustering clustering = kmeans.cluster(instances);
		InstanceList[] clusters = clustering.getClusters();
		
		for (int i = 0; i < numClusters.value; i++ ) {
			
			System.out.println("Cluser #"+i+"\n");
			
			for (int j = 0; j < clusters[i].size(); j++) {
				if ( clusters[i].get(j).getName() != null ) {
					System.out.println(clusters[i].get(j).getName() + "\n");				
					
				}
				
			}
			
		}
		
		return clusters;
	}

}
