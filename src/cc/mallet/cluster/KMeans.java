/*
 * Copyright (C) 2003 Univ. of Massachusetts Amherst, Computer Science Dept.
 * This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
 * http://www.cs.umass.edu/~mccallum/mallet This software is provided under the
 * terms of the Common Public License, version 1.0, as published by
 * http://www.opensource.org. For further information, see the file `LICENSE'
 * included with this distribution.
 */

/**
 * Clusters a set of point via k-Means. The instances that are clustered are
 * expected to be of the type FeatureVector.
 * 
 * EMPTY_SINGLE and other changes implemented March 2005 Heuristic cluster
 * selection implemented May 2005
 * 
 * @author Jerod Weinman <A
 *         HREF="mailto:weinman@cs.umass.edu">weinman@cs.umass.edu</A>
 * @author Mike Winter <a href =
 *         "mailto:mike.winter@gmail.com">mike.winter@gmail.com</a>
 * 
 */

package cc.mallet.cluster;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Metric;
import cc.mallet.types.SparseVector;
import cc.mallet.util.VectorStats;

/**
 * KMeans Clusterer
 * 
 * Clusters the points into k clusters by minimizing the total intra-cluster
 * variance. It uses a given {@link Metric} to find the distance between
 * {@link Instance}s, which should have {@link SparseVector}s in the data
 * field.
 * 
 */
public class KMeans extends Clusterer  {

  private static final long serialVersionUID = 1L;

  // Stop after movement of means is less than this
  //static double MEANS_TOLERANCE = 1e-2;
  static double MEANS_TOLERANCE = 1e-3;

  // Maximum number of iterations
  static int MAX_ITER = 100;

  // Minimum fraction of points that move
  //static double POINTS_TOLERANCE = .01;
  static double POINTS_TOLERANCE = .0;

  /**
   * Treat an empty cluster as an error condition.
   */
  public static final int EMPTY_ERROR = 0;
  /**
   * Drop an empty cluster
   */
  public static final int EMPTY_DROP = 1;
  /**
   * Place the single instance furthest from the previous cluster mean
   */
  public static final int EMPTY_SINGLE = 2;

  Random randinator;
  Metric metric;
  int numClusters;
  int emptyAction;
  ArrayList<SparseVector> clusterMeans;
  
  ExecutorService executor;
  private int numThreads;
  private boolean executeInThreadPool;

  private static Logger logger = Logger
      .getLogger("edu.umass.cs.mallet.base.cluster.KMeans");

  /**
   * Construct a KMeans object
   * 
   * @param instancePipe Pipe for the instances being clustered
   * @param numClusters Number of clusters to use
   * @param metric Metric object to measure instance distances
   * @param emptyAction Specify what should happen when an empty cluster occurs
   */
  public KMeans(Pipe instancePipe, int numClusters, Metric metric,
      int emptyAction, boolean bMultiThread) {

    super(instancePipe);

    this.emptyAction = emptyAction;
    this.metric = metric;
    this.numClusters = numClusters;

    this.clusterMeans = new ArrayList<SparseVector>(numClusters);
    this.randinator = new Random();
    
    this.numThreads = Runtime.getRuntime().availableProcessors();
	this.executor = Executors.newFixedThreadPool(this.numThreads);
	this.executeInThreadPool = bMultiThread;

  }

  /**
   * Construct a KMeans object
   * 
   * @param instancePipe Pipe for the instances being clustered
   * @param numClusters Number of clusters to use
   * @param metric Metric object to measure instance distances <p/> If an empty
   *        cluster occurs, it is considered an error.
   */
  public KMeans(Pipe instancePipe, int numClusters, Metric metric) {
    this(instancePipe, numClusters, metric, EMPTY_ERROR, false);
    this.numThreads = Runtime.getRuntime().availableProcessors();
	this.executor = Executors.newFixedThreadPool(this.numThreads);	
  }

  /**
   * Cluster instances
   * 
   * @param instances List of instances to cluster
   */
  @Override
  public Clustering cluster(InstanceList instances) {

	    assert (instances.getPipe() == this.instancePipe);

	    // Initialize clusterMeans
	    // Initialize clusterMeans
	    long start_time = System.currentTimeMillis();
	    //initializeMeansSample(instances, this.metric);
	    initializeMeansSample_DP(instances, this.metric);
	    long end_time = System.currentTimeMillis();
	    long init_time = end_time - start_time; 
	    
	    System.out.println("KMeans Centers Initialization cost: " + init_time);

	    int clusterLabels[] = new int[instances.size()];
	    ArrayList<InstanceList> instanceClusters = new ArrayList<InstanceList>(
	        numClusters);
	    int instClust;
	    double instClustDist, instDist;
	    double deltaMeans = Double.MAX_VALUE;
	    double deltaPoints = (double) instances.size();
	    int iterations = 0;
	    SparseVector clusterMean;
	    int stopCondition;

	    for (int c = 0; c < numClusters; c++) {
	      instanceClusters.add(c, new InstanceList(instancePipe));
	    }

	    logger.info("Entering KMeans iteration");
	    
	    //start_time = System.currentTimeMillis();

	    while (deltaMeans > MEANS_TOLERANCE && iterations < MAX_ITER
	        && deltaPoints > instances.size() * POINTS_TOLERANCE) {

	      iterations++;
	      deltaPoints = 0.0;

	      // For each instance, measure its distance to the current cluster
	      // means, and subsequently assign it to the closest cluster
	      // by adding it to an corresponding instance list
	      // The mean of each cluster InstanceList is then updated.
	      stopCondition = instances.size();
			KMeansTaskForInstanceDistance.tasksLock = this;
			KMeansTaskForInstanceDistance.stopCondition = stopCondition;
	      for (int n = 0; n < instances.size(); n++) {
					if (!executeInThreadPool) {
						instClust = 0;
						instClustDist = Double.MAX_VALUE;

						for (int c = 0; c < numClusters; c++) {
							instDist = metric.distance(clusterMeans.get(c),
									(SparseVector) instances.get(n).getData());

							if (instDist < instClustDist) {
								instClust = c;
								instClustDist = instDist;
							}
						}
						// Add to closest cluster & label it such
						instanceClusters.get(instClust).add(instances.get(n));

						if (clusterLabels[n] != instClust) {
							clusterLabels[n] = instClust;
							deltaPoints++;
						}
					} else {
						Runnable distanceTask = new KMeansTaskForInstanceDistance(instances, numClusters, 
								instanceClusters, metric, clusterMeans, clusterLabels,
								deltaPoints, n, stopCondition);
						executor.execute(distanceTask);
					}
	      }
	      
	      deltaMeans = 0.0;
	      
	      if ( executeInThreadPool ) {
				synchronized (this) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				deltaPoints = KMeansTaskForInstanceDistance.deltaPoints;
		  }
	      

	      
	      
	      if ( executeInThreadPool ) {
	    	  KMeansRecalculateMeanPointTask.deltaMeans = deltaMeans;
	    	  KMeansRecalculateMeanPointTask.tasksLock = this;
	    	  KMeansRecalculateMeanPointTask.stopCondition = numClusters;
	      }
	      
	      for (int c = 0; c < numClusters; c++) {
	    	if (executeInThreadPool){
	    		Runnable meanTask = new KMeansRecalculateMeanPointTask(instanceClusters,
	    				 metric, clusterMeans, c, instancePipe, this);
				executor.execute(meanTask);
	    	} else

	        if (instanceClusters.get(c).size() > 0) {
	          clusterMean = VectorStats.mean(instanceClusters.get(c));

	          deltaMeans += metric.distance(clusterMeans.get(c), clusterMean);

	          clusterMeans.set(c, clusterMean);

	          instanceClusters.set(c, new InstanceList(instancePipe));

	        } else {

	          logger.info("Empty cluster found.");

	          switch (emptyAction) {
	            case EMPTY_ERROR:
	              return null;
	            case EMPTY_DROP:
	              logger.fine("Removing cluster " + c);
	              clusterMeans.remove(c);
	              instanceClusters.remove(c);
	              for (int n = 0; n < instances.size(); n++) {

	                assert (clusterLabels[n] != c) : "Cluster size is "
	                    + instanceClusters.get(c).size()
	                    + "+ yet clusterLabels[n] is " + clusterLabels[n];

	                if (clusterLabels[n] > c)
	                  clusterLabels[n]--;
	              }

	              numClusters--;
	              c--; // <-- note this trickiness. bad style? maybe.
	              // it just means now that we've deleted the entry,
	              // we have to repeat the index to get the next entry.
	              break;

	            case EMPTY_SINGLE:

	              // Get the instance the furthest from any centroid
	              // and make it a new centroid.

	              double newCentroidDist = 0;
	              int newCentroid = 0;
	              InstanceList cacheList = null;

	              for (int clusters = 0; clusters < clusterMeans.size(); clusters++) {
	                SparseVector centroid = clusterMeans.get(clusters);
	                InstanceList centInstances = instanceClusters.get(clusters);

	                // Dont't create new empty clusters.

	                if (centInstances.size() <= 1)
	                  continue;
	                for (int n = 0; n < centInstances.size(); n++) {
	                  double currentDist = metric.distance(centroid,
	                      (SparseVector) centInstances.get(n).getData());
	                  if (currentDist > newCentroidDist) {
	                    newCentroid = n;
	                    newCentroidDist = currentDist;
	                    cacheList = centInstances;

	                  }
	                }
	              }
	              if (cacheList == null) {
	                logger.info("Can't find an instance to move.  Exiting.");
	                // Can't find an instance to move.
	                return null;
	              } else clusterMeans.set(c, (SparseVector) cacheList.get(
	                  newCentroid).getData());

	            default:
	              return null;
	          }
	        }
	      }
	      
	      if ( executeInThreadPool ) {
				synchronized (this) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
	           deltaMeans = KMeansRecalculateMeanPointTask.deltaMeans;
	      }

	      logger.info("Iter " + iterations + " deltaMeans = " + deltaMeans);
	    }
	    
	    //end_time = System.currentTimeMillis();
	    //init_time = end_time - start_time; 
	    
	    //System.out.println("KMeans iterations cost: " + init_time);

	    if (deltaMeans <= MEANS_TOLERANCE)
	      logger.info("KMeans converged with deltaMeans = " + deltaMeans);
	    else if (iterations >= MAX_ITER)
	      logger.info("Maximum number of iterations (" + MAX_ITER + ") reached.");
	    else if (deltaPoints <= instances.size() * POINTS_TOLERANCE)
	      logger.info("Minimum number of points (np*" + POINTS_TOLERANCE + "="
	          + (int) (instances.size() * POINTS_TOLERANCE)
	          + ") moved in last iteration. Saying converged.");

	    //should move to finalize method
	    try {
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
		} catch (InterruptedException ignored) {
		}

	    return new Clustering(instances, numClusters, clusterLabels);

	  }
  
  /*
  public Clustering cluster(InstanceList instances) {

	    assert (instances.getPipe() == this.instancePipe);

	    // Initialize clusterMeans
	    long start_time = System.currentTimeMillis();
	    //initializeMeansSample(instances, this.metric);
	    initializeMeansSample_DP(instances, this.metric);
	    long end_time = System.currentTimeMillis();
	    long init_time = end_time - start_time; 
	    
	    System.out.println("KMeans Centers Initialization cost: " + init_time);
	    
	 

	    int clusterLabels[] = new int[instances.size()];
	    ArrayList<InstanceList> instanceClusters = new ArrayList<InstanceList>(
	        numClusters);
	    int instClust;
	    double instClustDist, instDist;
	    double deltaMeans = Double.MAX_VALUE;
	    double deltaPoints = (double) instances.size();
	    int iterations = 0;
	    SparseVector clusterMean;
	    int stopCondition;

	    for (int c = 0; c < numClusters; c++) {
	      instanceClusters.add(c, new InstanceList(instancePipe));
	    }

	    logger.info("Entering KMeans iteration");

	    while (deltaMeans > MEANS_TOLERANCE && iterations < MAX_ITER
	        && deltaPoints > instances.size() * POINTS_TOLERANCE) {

	      //System.out.println("KMeans iteration: " + iterations + "\n");
	    	
	      iterations++;
	      deltaPoints = 0;    
	      
	      stopCondition = instances.size();
	      KMeansTaskForInstanceDistance.tasksLock = this;
	      KMeansTaskForInstanceDistance.stopCondition = stopCondition;
	      	

	      //Haihua - Comments   Step-1 Start
	      // For each instance, measure its distance to the current cluster
	      // means, and subsequently assign it to the closest cluster
	      // by adding it to an corresponding instance list
	      // The mean of each cluster InstanceList is then updated.
	      
	      for (int n = 0; n < instances.size(); n++) {

	    	  if (!executeInThreadPool) {
	    		  instClust = 0;
	    		  instClustDist = Double.MAX_VALUE;
	    		  
	    		  for (int c = 0; c < numClusters; c++) {
	    			  instDist = metric.distance(clusterMeans.get(c), (SparseVector) instances.get(n).getData());
	    			  if (instDist < instClustDist) {
	    				  instClust = c;
	    				  instClustDist = instDist;
	    			  }
	    		  }
	    		  
	    		  // Add to closest cluster & label it such
	    		  instanceClusters.get(instClust).add(instances.get(n));
	    		  
	    		  if (clusterLabels[n] != instClust) {
	    			  clusterLabels[n] = instClust;
	    			  deltaPoints++;
	    		  }
	    	  } else {
	    		  Runnable distanceTask = new KMeansTaskForInstanceDistance(instances, numClusters, 
							instanceClusters, metric, clusterMeans, clusterLabels,
							deltaPoints, n, stopCondition);
	    		  executor.execute(distanceTask);
	    		  
	    	  }

	      }
	      
	    //Haihua - Comments   Step-1 End, Sync all working threads here
	      
	     if (executeInThreadPool)
	    	 synchronized (this) {
	    		 try {
	    			 this.wait();
	    		 } catch (InterruptedException e) {
	    			e.printStackTrace();
	    		 }
	    	}
	    

	      deltaPoints = KMeansTaskForInstanceDistance.deltaPoints;
	      
	      KMeansRecalculateMeanPointTask.deltaMeans = deltaMeans;
	      KMeansRecalculateMeanPointTask.tasksLock = this;
	      KMeansRecalculateMeanPointTask.stopCondition = numClusters;    
	      
	    }
	      
	    //Haihua - Comments   Step-2 Start
	    deltaMeans = 0;
	      
	        

	      for (int c = 0; c < numClusters; c++) {
	    	  
	    	  if (executeInThreadPool){
	    		  Runnable meanTask = new KMeansRecalculateMeanPointTask(instanceClusters,
	    				  metric, clusterMeans, c, instancePipe, this);
	    		  executor.execute(meanTask);
	    	  } else if (instanceClusters.get(c).size() > 0) {
	    		  clusterMean = VectorStats.mean(instanceClusters.get(c));

	    		  deltaMeans += metric.distance(clusterMeans.get(c), clusterMean);

	    		  clusterMeans.set(c, clusterMean);

	    		  instanceClusters.set(c, new InstanceList(instancePipe));

	    	  } else {

	    		  logger.info("Empty cluster found.");

	    		  switch (emptyAction) {
	    		  	case EMPTY_ERROR:
	    		  		return null;
	    		  	case EMPTY_DROP:
	    		  		logger.fine("Removing cluster " + c);
	    		  		clusterMeans.remove(c);
	    		  		instanceClusters.remove(c);
	    		  		for (int n = 0; n < instances.size(); n++) {

	    		  			assert (clusterLabels[n] != c) : "Cluster size is "
	    		  					+ instanceClusters.get(c).size()
	    		  					+ "+ yet clusterLabels[n] is " + clusterLabels[n];

	    		  			if (clusterLabels[n] > c)
	    		  				clusterLabels[n]--;
	    		  			}

	    		  		numClusters--;
	    		  		c--; // <-- note this trickiness. bad style? maybe.
	    		  		// it just means now that we've deleted the entry,
	    		  		// we have to repeat the index to get the next entry.
	    		  		break;
	    		  	case EMPTY_SINGLE:

	    		  		//Get the instance the furthest from any centroid
	    		  		// and make it a new centroid.

	    		  		double newCentroidDist = 0;
	    		  		int newCentroid = 0;
	    		  		InstanceList cacheList = null;

	    		  		for (int clusters = 0; clusters < clusterMeans.size(); clusters++) {
	    		  			SparseVector centroid = clusterMeans.get(clusters);
	    		  			InstanceList centInstances = instanceClusters.get(clusters);

	    		  			// Dont't create new empty clusters.

	    		  			if (centInstances.size() <= 1)
	    		  				continue;
	    		  			for (int n = 0; n < centInstances.size(); n++) {
	    		  				double currentDist = metric.distance(centroid,
	    		  						(SparseVector) centInstances.get(n).getData());
	    		  				if (currentDist > newCentroidDist) {
	    		  					newCentroid = n;
	    		  					newCentroidDist = currentDist;
	    		  					cacheList = centInstances;

	    		  				}
	    		  			}
	    		  		}
	    		  		if (cacheList == null) {
	    		  			logger.info("Can't find an instance to move.  Exiting.");
	    		  			// Can't find an instance to move.
	    		  			return null;
	    		  		} else clusterMeans.set(c, (SparseVector) cacheList.get(
	    		  				newCentroid).getData());

	    		  				default:
	    		  				return null;
	    		  		}
	    		  }
	    	  
	      }
	      
	      
	      if (executeInThreadPool) {
	    	  synchronized (this) {
	    		  try {
	    			  this.wait();
	    		  } catch (InterruptedException e) {
	    			  e.printStackTrace();
				  }
			  }
	    	  deltaMeans = KMeansRecalculateMeanPointTask.deltaMeans;
	      }
	      
	    //Haihua - Comments   Step-2 End   Sync all working threads here

	      logger.info("Iter " + iterations + " deltaMeans = " + deltaMeans);
	    

	    if (deltaMeans <= MEANS_TOLERANCE)
	      logger.info("KMeans converged with deltaMeans = " + deltaMeans);
	    else if (iterations >= MAX_ITER)
	      logger.info("Maximum number of iterations (" + MAX_ITER + ") reached.");
	    else if (deltaPoints <= instances.size() * POINTS_TOLERANCE)
	      logger.info("Minimum number of points (np*" + POINTS_TOLERANCE + "="
	          + (int) (instances.size() * POINTS_TOLERANCE)
	          + ") moved in last iteration. Saying converged.");

	    //should move to finalize method
	    try {
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
		} catch (InterruptedException ignored) {
		}
	    
	    return new Clustering(instances, numClusters, clusterLabels);

	  }
  */
  
  
  //Debug Only
  public Clustering cluster_withDump(InstanceList instances) throws IOException {

    assert (instances.getPipe() == this.instancePipe);

    // Initialize clusterMeans
    long start_time = System.currentTimeMillis();
    initializeMeansSample(instances, this.metric);
    //initializeMeansSample_DP(instances, this.metric);
    long end_time = System.currentTimeMillis();
    long init_time = end_time - start_time; 
    
    System.out.println("KMeans Centers Initialization cost: " + init_time);
    
 
    
    PrintWriter dump_writer = new PrintWriter (new FileWriter ((new File("dumped_init_centers.txt"))));
    StringBuilder builder = new StringBuilder();
    
    for ( int i = 0; i < numClusters; i++ ) {
    	builder.append("Cluser "+ i + ": ");
    		
    	SparseVector center = clusterMeans.get(i);
    	builder.append(center);
    	builder.append("\n");
    		
    }
    	
    dump_writer.println(builder);
    dump_writer.close();
    
    
    clusterMeans.clear();
    
    
    initializeMeansSample_DP(instances, this.metric);
    
    PrintWriter dump_writer2 = new PrintWriter (new FileWriter ((new File("dumped_init_centers_new.txt"))));
	StringBuilder builder2 = new StringBuilder();
	
	for ( int i = 0; i < numClusters; i++ ) {
		builder2.append("Cluser "+ i + ": ");
		
		SparseVector center = clusterMeans.get(i);
		builder2.append(center);
		//for ( int j = 0; j < center.getNumDimensions(); j++ ) {
			
		//}
		builder2.append("\n");
		
	}
	
	dump_writer2.println(builder2);
	dump_writer2.close();
    

    int clusterLabels[] = new int[instances.size()];
    ArrayList<InstanceList> instanceClusters = new ArrayList<InstanceList>(
        numClusters);
    int instClust;
    double instClustDist, instDist;
    double deltaMeans = Double.MAX_VALUE;
    double deltaPoints = (double) instances.size();
    int iterations = 0;
    SparseVector clusterMean;
    int stopCondition;

    for (int c = 0; c < numClusters; c++) {
      instanceClusters.add(c, new InstanceList(instancePipe));
    }

    logger.info("Entering KMeans iteration");

    while (deltaMeans > MEANS_TOLERANCE && iterations < MAX_ITER
        && deltaPoints > instances.size() * POINTS_TOLERANCE) {

      //System.out.println("KMeans iteration: " + iterations + "\n");
    	
      iterations++;
      deltaPoints = 0;    
      
      stopCondition = instances.size();
      KMeansTaskForInstanceDistance.tasksLock = this;
      KMeansTaskForInstanceDistance.stopCondition = stopCondition;
      	

      //Haihua - Comments   Step-1 Start
      // For each instance, measure its distance to the current cluster
      // means, and subsequently assign it to the closest cluster
      // by adding it to an corresponding instance list
      // The mean of each cluster InstanceList is then updated.
      
      for (int n = 0; n < instances.size(); n++) {

    	  if (!executeInThreadPool) {
    		  instClust = 0;
    		  instClustDist = Double.MAX_VALUE;
    		  
    		  for (int c = 0; c < numClusters; c++) {
    			  instDist = metric.distance(clusterMeans.get(c), (SparseVector) instances.get(n).getData());
    			  if (instDist < instClustDist) {
    				  instClust = c;
    				  instClustDist = instDist;
    			  }
    		  }
    		  
    		  // Add to closest cluster & label it such
    		  instanceClusters.get(instClust).add(instances.get(n));
    		  
    		  if (clusterLabels[n] != instClust) {
    			  clusterLabels[n] = instClust;
    			  deltaPoints++;
    		  }
    	  } else {
    		  Runnable distanceTask = new KMeansTaskForInstanceDistance(instances, numClusters, 
						instanceClusters, metric, clusterMeans, clusterLabels,
						deltaPoints, n, stopCondition);
    		  executor.execute(distanceTask);
    		  
    	  }

      }
      
    //Haihua - Comments   Step-1 End, Sync all working threads here
      
      synchronized (this) {
    	  try {
    		  this.wait();
    	  } catch (InterruptedException e) {
    		  e.printStackTrace();
    	  }
	  }

      deltaPoints = KMeansTaskForInstanceDistance.deltaPoints;
      
    //Haihua - Comments   Step-2 Start
      deltaMeans = 0;
      
      KMeansRecalculateMeanPointTask.deltaMeans = deltaMeans;
      KMeansRecalculateMeanPointTask.tasksLock = this;
      KMeansRecalculateMeanPointTask.stopCondition = numClusters;      

      for (int c = 0; c < numClusters; c++) {
    	  
    	  if (executeInThreadPool){
    		  Runnable meanTask = new KMeansRecalculateMeanPointTask(instanceClusters,
    				  metric, clusterMeans, c, instancePipe, this);
    		  executor.execute(meanTask);
    	  } else if (instanceClusters.get(c).size() > 0) {
    		  clusterMean = VectorStats.mean(instanceClusters.get(c));

    		  deltaMeans += metric.distance(clusterMeans.get(c), clusterMean);

    		  clusterMeans.set(c, clusterMean);

    		  instanceClusters.set(c, new InstanceList(instancePipe));

    	  } else {

    		  logger.info("Empty cluster found.");

    		  switch (emptyAction) {
    		  	case EMPTY_ERROR:
    		  		return null;
    		  	case EMPTY_DROP:
    		  		logger.fine("Removing cluster " + c);
    		  		clusterMeans.remove(c);
    		  		instanceClusters.remove(c);
    		  		for (int n = 0; n < instances.size(); n++) {

    		  			assert (clusterLabels[n] != c) : "Cluster size is "
    		  					+ instanceClusters.get(c).size()
    		  					+ "+ yet clusterLabels[n] is " + clusterLabels[n];

    		  			if (clusterLabels[n] > c)
    		  				clusterLabels[n]--;
    		  			}

    		  		numClusters--;
    		  		c--; // <-- note this trickiness. bad style? maybe.
    		  		// it just means now that we've deleted the entry,
    		  		// we have to repeat the index to get the next entry.
    		  		break;
    		  	case EMPTY_SINGLE:

    		  		//Get the instance the furthest from any centroid
    		  		// and make it a new centroid.

    		  		double newCentroidDist = 0;
    		  		int newCentroid = 0;
    		  		InstanceList cacheList = null;

    		  		for (int clusters = 0; clusters < clusterMeans.size(); clusters++) {
    		  			SparseVector centroid = clusterMeans.get(clusters);
    		  			InstanceList centInstances = instanceClusters.get(clusters);

    		  			// Dont't create new empty clusters.

    		  			if (centInstances.size() <= 1)
    		  				continue;
    		  			for (int n = 0; n < centInstances.size(); n++) {
    		  				double currentDist = metric.distance(centroid,
    		  						(SparseVector) centInstances.get(n).getData());
    		  				if (currentDist > newCentroidDist) {
    		  					newCentroid = n;
    		  					newCentroidDist = currentDist;
    		  					cacheList = centInstances;

    		  				}
    		  			}
    		  		}
    		  		if (cacheList == null) {
    		  			logger.info("Can't find an instance to move.  Exiting.");
    		  			// Can't find an instance to move.
    		  			return null;
    		  		} else clusterMeans.set(c, (SparseVector) cacheList.get(
    		  				newCentroid).getData());

    		  				default:
    		  				return null;
    		  		}
    		  }
    	  
      }
      
      synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
      
      deltaMeans = KMeansRecalculateMeanPointTask.deltaMeans;
      
    //Haihua - Comments   Step-2 End   Sync all working threads here

      logger.info("Iter " + iterations + " deltaMeans = " + deltaMeans);
    }

    if (deltaMeans <= MEANS_TOLERANCE)
      logger.info("KMeans converged with deltaMeans = " + deltaMeans);
    else if (iterations >= MAX_ITER)
      logger.info("Maximum number of iterations (" + MAX_ITER + ") reached.");
    else if (deltaPoints <= instances.size() * POINTS_TOLERANCE)
      logger.info("Minimum number of points (np*" + POINTS_TOLERANCE + "="
          + (int) (instances.size() * POINTS_TOLERANCE)
          + ") moved in last iteration. Saying converged.");
    
    
    PrintWriter dump_writer_final = new PrintWriter (new FileWriter ((new File("dumped_final_clusters.txt"))));
    StringBuilder builder_final = new StringBuilder();
    
    for ( int i = 0; i < numClusters; i++ ) {
    	builder_final.append("Cluser "+ i + ": ");
    		
    	SparseVector center = clusterMeans.get(i);
    	builder_final.append(center);
    	builder_final.append("\n");
    		
    }
    	
    dump_writer_final.println(builder_final);
    dump_writer_final.close();
    
    
    
    
    
    
    clusterMeans.clear();

    //should move to finalize method
    try {
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
	} catch (InterruptedException ignored) {
	}
    
    return new Clustering(instances, numClusters, clusterLabels);

  }
  
  public void printClusterResult(PrintWriter out, Clustering clustering, boolean referDoc, boolean propVector) {
	  
	  int numClusters = clustering.getNumClusters();
	  InstanceList instances =  clustering.getInstances();
	  int numInstances = instances.size();
	  InstanceList[] clusters = clustering.getClusters();
	  
	  StringBuilder builder = new StringBuilder();
	  
	  final String Reg = "/\\d+";
	  Pattern p = Pattern.compile(Reg);
	  
	  
	  for ( int i = 0; i < numClusters; i++ ) {
		  
		  for (int j = 0; j < clusters[i].size(); j++) {
			  
			  if ( clusters[i].get(j).getName() != null ) {
				  
				  builder.append(clusters[i].get(j).getName().toString());
				  
				  String filename = clusters[i].get(j).getName().toString();
				  Matcher m = p.matcher(filename);
				  
				  if ( m.find()) {
					  String fileID = filename.substring(m.start()+1, m.end());
					  
					  //builder.append(clusters[i].get(j).getName());
					  builder.append(" Article_ID# " + fileID);					  
					  builder.append(" Cluster_ID# " + i + " ");
					  builder.append(" Cluster_Number#" + clusters[i].size() + " ");			  
					  
					  
					  
					  if (clusters[i].size() > 1) {
						  
						  builder.append(" ref ");
						  
						  for ( int k = 0; k < clusters[i].size(); k++) {
							  
							  if ( k == j ) {
								  continue;
								  }
							  

							  
							  
							  String negighbor_filename = clusters[i].get(k).getName().toString();
							  Matcher m_neighbor = p.matcher(negighbor_filename);
						  
							  if ( m_neighbor.find()) {  
								  String fileID_neighbor = negighbor_filename.substring(m_neighbor.start()+1, m_neighbor.end());
								  
								//Debugging
								  //if ( fileID.equals("123535") &&  fileID_neighbor.equals("117798")) {
								  if ( fileID.equals("124932")) {
									  System.out.println("checking point is found \n");
								  }
								  
								  if ( fileID.equals("124932") &&  fileID_neighbor.equals("117855")) {
									  System.out.println("checking point is found \n");
								  }
								  
								  double similarity = 1.0 - metric.distance((SparseVector)clusters[i].get(j).getData(), (SparseVector)clusters[i].get(k).getData());
									  
									  
								  builder.append(fileID_neighbor + " " + similarity + " ");
							  }
						  }
						  
						  
						  
					  }
					  
					  
				  }
				  
				  //System.out.println(clusters[i].get(j).getName() + "Cluster: " + i + "\n");
				  
				  
				  
				  builder.append("\n");
					
				}
			  
			}
		  
		  
	  }
	  
	  out.println(builder);
	  
  }

  /**
   * Uses a MAX-MIN heuristic to seed the initial cluster means..
   * 
   * @param instList List of instances.
   * @param metric Distance metric.
   */

  private void initializeMeansSample(InstanceList instList, Metric metric) {

    // InstanceList has no remove() and null instances aren't
    // parsed out by most Pipes, so we have to pre-process
    // here and possibly leave some instances without
    // cluster assignments.

    ArrayList<Instance> instances = new ArrayList<Instance>(instList.size());
    for (int i = 0; i < instList.size(); i++) {
      Instance ins = instList.get(i);
      SparseVector sparse = (SparseVector) ins.getData();
      if (sparse.numLocations() == 0)
        continue;

      instances.add(ins);
    }

    // Add next center that has the MAX of the MIN of the distances from
    // each of the previous j-1 centers (idea from Andrew Moore tutorial,
    // not sure who came up with it originally)

    for (int i = 0; i < numClusters; i++) {
      double max = 0;
      int selected = 0;
      for (int k = 0; k < instances.size(); k++) {
        double min = Double.MAX_VALUE;
        Instance ins = instances.get(k);
        SparseVector inst = (SparseVector) ins.getData();
        for (int j = 0; j < clusterMeans.size(); j++) {
          SparseVector centerInst = clusterMeans.get(j);
          double dist = metric.distance(centerInst, inst);
          if (dist < min)
            min = dist;

        }
        if (min > max) {
          selected = k;
          max = min;
        }
      }

      Instance newCenter = instances.remove(selected);
      clusterMeans.add((SparseVector) newCenter.getData());
    }

  }
  
  private void initializeMeansSample_DP(final InstanceList instList, final Metric metric) {

	    // InstanceList has no remove() and null instances aren't
	    // parsed out by most Pipes, so we have to pre-process
	    // here and possibly leave some instances without
	    // cluster assignments.
	  int inst_num = instList.size();

	  final double[][] similarity = new double[inst_num][inst_num];
	  ArrayList<Integer> points_index = new ArrayList<Integer>(numClusters);
	  ArrayList<Integer> centers_index = new ArrayList<Integer>(0);
	  
	  for (int i = 0; i < inst_num; i++) {
		  points_index.add(i);
	  }
	  
/* 修改成多线程版本，注释 by zhouzhenhua 
	  for ( int i = 0; i < inst_num; i++ ) {
		  
		  for ( int j = 0; j < inst_num; j++ ) {
			  
			  similarity[i][j] = metric.distance((SparseVector)instList.get(i).getData(), (SparseVector)instList.get(j).getData());
		  }
		  
	  }
*/

		ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for (int i = 0; i < inst_num; i++) {
			for (int j = 0; j < inst_num; j++) {
				final int m = i, n = j;
				Runnable run = new Runnable() {
					@Override
					public void run() {
						similarity[m][n] = metric.distance((SparseVector) instList.get(m).getData(), (SparseVector) instList.get(n).getData());
					}
				};
				service.execute(run);
			}
		}
		service.shutdown();

		try {
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	  
	  ArrayList<Instance> instances = new ArrayList<Instance>(instList.size());
	  for (int i = 0; i < instList.size(); i++) {
	  Instance ins = instList.get(i);
	  SparseVector sparse = (SparseVector) ins.getData();
	  if (sparse.numLocations() == 0)
		  continue;
	  	  instances.add(ins);
	  }

	 // Add next center that has the MAX of the MIN of the distances from
	 // each of the previous j-1 centers (idea from Andrew Moore tutorial,
	 // not sure who came up with it originally)

	 for (int i = 0; i < numClusters; i++) {
	      double max = 0;
	      int selected = 0;
	      for (int k = 0; k < points_index.size(); k++) {
	        double min = Double.MAX_VALUE;
	        for (int j = 0; j < centers_index.size(); j++) {
	          double dist = similarity[points_index.get(k)][centers_index.get(j)];
	          if (dist < min)
	            min = dist;
	
	        }
	        if (min > max) {
	          selected = k;
	          max = min;
	        }
	      }
	
		  Integer newCenter = points_index.remove(selected);
		  centers_index.add(newCenter);
		  clusterMeans.add((SparseVector)instList.get(newCenter).getData());
	}
	
  }

  /**
   * Return the ArrayList of cluster means after a run of the algorithm.
   * 
   * @return An ArrayList of Instances.
   */

  public ArrayList<SparseVector> getClusterMeans() {
    return this.clusterMeans;
  }
}

class KMeansTaskForInstanceDistance implements Runnable {
	InstanceList instances;
	int numClusters;
	ArrayList<InstanceList> instanceClusters;
	Metric metric;
	ArrayList<SparseVector> clusterMeans;
	int[] clusterLabels;
	static double deltaPoints;
	static Object tasksLock;
	int index;
	static int stopCondition;
	
	KMeansTaskForInstanceDistance(InstanceList instances, int numClusters, ArrayList<InstanceList> instanceClusters,
			Metric metric, ArrayList<SparseVector> clusterMeans, int[]clusterLabels,
			Double deltaPoints, int index, Integer stopCondition){
		this.numClusters = numClusters;
		this.instanceClusters = instanceClusters;
		this.instances = instances;
		this.metric = metric;
		this.clusterMeans = clusterMeans;
		this.clusterLabels = clusterLabels;
		this.index = index;
	}
	
	public void run() {
		int instClust = 0;
		double instClustDist = Double.MAX_VALUE;
		double instDist;

		for (int c = 0; c < numClusters; c++) {
			instDist = metric.distance(clusterMeans.get(c),
					(SparseVector) instances.get(index).getData());
			if (instDist < instClustDist) {
				instClust = c;
				instClustDist = instDist;
			}
		}
		// Add to closest cluster & label it such
		synchronized(instanceClusters.get(instClust)){
			instanceClusters.get(instClust).add(instances.get(index));
		}
		
		if (clusterLabels[index] != instClust) {
			clusterLabels[index] = instClust;
			synchronized (KMeansTaskForInstanceDistance.tasksLock) {
				KMeansTaskForInstanceDistance.deltaPoints++;
			}
		}
		
		synchronized(KMeansTaskForInstanceDistance.tasksLock){
			KMeansTaskForInstanceDistance.stopCondition --;
			//System.out.println("Stop condition is " + stopCondition);
			if (stopCondition == 0){
				tasksLock.notify();
			}
		}
	}

}

class KMeansRecalculateMeanPointTask implements Runnable {
	ArrayList<InstanceList> instanceClusters;
	Metric metric;
	ArrayList<SparseVector> clusterMeans;
	int index;
	static double deltaMeans;
	Pipe instancePipe;
	static Object tasksLock;
	static int stopCondition;
	

	KMeansRecalculateMeanPointTask(ArrayList<InstanceList> instanceClusters,
			Metric metric, ArrayList<SparseVector> clusterMeans, int index,
			Pipe instancePipe, Object poolLock) {
		this.instanceClusters = instanceClusters;
		this.metric = metric;
		this.clusterMeans = clusterMeans;
		this.index = index;
		this.instancePipe = instancePipe;
	}

	public void run() {
    	SparseVector clusterMean;
        if (instanceClusters.get(index).size() > 0) {
            clusterMean = VectorStats.mean(instanceClusters.get(index));

            deltaMeans += metric.distance(clusterMeans.get(index), clusterMean);

            clusterMeans.set(index, clusterMean);

            instanceClusters.set(index, new InstanceList(instancePipe));
        }
        
		synchronized(tasksLock){
			stopCondition --;
			//System.out.println("Stop condition is " + stopCondition);
			if (stopCondition == 0){
				tasksLock.notify();
			}
		}
	}
}