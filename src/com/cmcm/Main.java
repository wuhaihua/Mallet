package com.cmcm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.session.SqlSession;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import cc.mallet.classify.tui.Text2Vectors;
import cc.mallet.topics.tui.Vectors2Cluster;
import cc.mallet.types.InstanceList;
import cc.mallet.types.NormalizedDotProductMetric;
import cc.mallet.types.SparseVector;
import cc.mallet.cluster.Clustering;
import cc.mallet.types.Metric;
import cc.mallet.util.CommandOption;
import db.dao.dbmodel.CmsContent;
import db.dao.dbmodel.NewsClassStat;
import db.dao.inter.CmsContentDao;
import db.dao.redis.Redis_Info;
import db.dao.service.DBservice;

import com.cmcm.NeighborSimilarity;
import com.cmcm.DocumentDescription;

public class Main {
	
	static CommandOption.String languagePick = new CommandOption.String
			(Main.class, "pick-language", "eng|chn", false, "eng",
			 "Value eng indicates English path is picked, value chn indicates Chinese path is picked.  ", null);
	
	private static final Log log = LogFactory.getLog(Main.class);
	
	//cid  1 - Science; 2 - Celebrities; 3 - Media; 4 - Sports; 5 - Health; 6 - News; 7 - Technology; 8 - Entertainment; 9 - Politics; 10 - Business; 11 - Finance; 12 - Game
	//         OFF			ON				OFF			ON			OFF			ON			ON				ON					ON			ON				ON			OFF
	private static final Double TopicRatio_Eng[] = {1.0, 0.34, 1.0, 0.1, 1.0, 0.15, 0.13, 0.21, 0.23, 0.25, 0.2, 1.0};
	private static final Double StoryRatio_Eng[] = {1.0, 0.57, 1.0, 0.24, 1.0, 0.36, 0.27, 0.46, 0.45, 0.62, 0.44, 1.0};
	private static final boolean bClusterEnable_Eng[] = {false, true, false, true, false, true, true, true, true, true, true, false};
	private static final String category_stoplist_Eng[] = {"science.txt", "celebrities.txt", "media.txt", "sports.txt", "health.txt", "news.txt", "technology.txt", "entertainment.txt", "politics.txt", "business.txt", "finance.txt", "game.txt"};
	
	// 1- NULL	2 - 时事 ;  3 - 体育; 4 - 娱乐; 5 - 互联网; 6 - 财经; 7 - 科技数码; 8 - 军事; 9 - 房产;  10 - 科学探索 
	private static final boolean bClusterEnable_Chn[] = {false, true, true, true, true, true, true, true, false, true};
	private static final Double TopicRatio_Chn[] = {1.0, 0.15, 0.1, 0.21, 0.13, 0.2, 0.13, 0.13, 1.0, 0.13};
	private static final Double StoryRatio_Chn[] = {1.0, 0.36, 0.24, 0.46, 0.27, 0.44, 0.27, 0.27, 1.0, 0.27};
	private static final String category_stoplist_Chn[] = {"", "news_chn.txt", "sports_chn.txt", "entertainment_chn.txt", "internet_chn.txt", "finance_chn.txt", "digital_device_chn.txt", "military_chn.txt", "realestate_chn.txt", "technology_chn.txt"};

	
	private static boolean bClusterEnable[] = {false, false, false, false, false, false, false, false, false, false, false, false};
	private static Double TopicRatio[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
	private static Double StoryRatio[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
	private static String category_stoplist[] ={"", "", "", "", "", "", "", "", "", "", "", ""};

	
	public static void main(String[] args) {

		CommandOption.setSummary (Main.class,
				  "A tool for estimating, saving and printing diagnostics for topic Cluster, LDA + KMeans.");
		CommandOption.process (Main.class, args);
		
		
		Jedis jedis = null;
		try {
			JedisPool jedispool = Redis_Info.initRedis();
			jedis = jedispool.getResource();
			if (jedis!=null&&jedis.get("isRun") == null
					|| jedis.get("isRun").equals("false")) {// isRun涓篺alse鎴栦负null锛屽彲浠ヨ繍琛�
				jedis.set("isRun", "true");
				// --input all --output autorial_all2.mallet --keep-sequence
				// --remove-stopwords
				String[] inArgs1 = { "--input", "all", "--output",
						"autorial_all3.mallet", "--keep-sequence",
						"--stoplist-file", "", "--extra-stopwords", "" };
				// --input autorial_all2.mallet --num-topics 5
				// --optimize-interval 20
				// --output-doc-topics autorial_compostion_all2.txt --k 5
				String[] inArgs2 = { "--input", "autorial_all3.mallet",
						"--num-topics", "5", "--optimize-interval", "20",
						"--output-doc-topics", "autorial_compostion_all2.txt",
						"--k", "5" };			
				

				// select count(*) as ldaN from cms_content group by Cid where
				// editDate < >
				
				DBservice db = new DBservice(languagePick.value.toString());
				
				String max_Time = db.getCurrentTime("yyyyMMdd",1);
				//String max_Time = db.getCurrentTime("yyyyMMdd",0);
				String min_Time = db.getCurrentTime("yyyyMMdd",-3);
				//String min_Time = db.getCurrentTime("yyyyMMdd",-1);
				List<NewsClassStat> list = db.DBgetCountGroupCid(max_Time,min_Time);
				log.debug("浠庢暟鎹簱涓彇鏁版嵁瀹屾瘯");
				int count=0;
				for(NewsClassStat ncs:list){
					count+=ncs.getNum();
				}
				log.debug("count:"+count);
				// topicModelNum = ldaN/10

				// 鎶�2涓垎绫讳粠mysql鍐欏埌鏂囦欢澶逛腑
				log.debug("max_time:"+max_Time);
				log.debug("min_time:"+min_Time);
				List<CmsContent> cmsContent = db.getContent(max_Time,min_Time, languagePick.value.toString());				
				
				
				db.writeInFile("/data/app/mysql", cmsContent, languagePick.value.toString());
				log.debug("mysql涓暟鎹啓鍏ユ枃浠跺畬姣�");
				
				
				Metric metric = new NormalizedDotProductMetric(); // cosine similarity
				
				log.debug("NormalizeDotProductMetric is done");

				//
				
				for (int i = 0; i < list.size(); i++) {
					List<DocumentDescription> category_data = null;
					
					NewsClassStat ncs = list.get(i);
					String cid = ncs.getCid();
					int num = ncs.getNum();
					// 鏀瑰彉杈撳叆鍊�
					//inArgs1[1] = "mysql/" + cid;
					inArgs1[1] = "/data/app/mysql/" + cid;
					inArgs1[3] = languagePick.value.toString() + "_" + cid + ".mallet";
					

					if ( languagePick.value.toString().equals("eng")) {
						
						bClusterEnable = bClusterEnable_Eng;
						TopicRatio		= TopicRatio_Eng;
						StoryRatio		= StoryRatio_Eng;
						category_stoplist = category_stoplist_Eng;
						//inArgs1[6] = "stoplists/en.txt";
						inArgs1[6] = "/data/app/v3/stoplists/en.txt";
						
					} else if ( languagePick.value.toString().equals("chn")) {
						
						bClusterEnable = bClusterEnable_Chn;
						TopicRatio		= TopicRatio_Chn;
						StoryRatio		= StoryRatio_Chn;
						category_stoplist = category_stoplist_Chn;
						//inArgs1[6] = "stoplists/chn.txt";
						inArgs1[6] = "/data/app/v3/stoplists/chn.txt";
						
						System.out.println("Prepare arguments for CHN path \n");
						
					}					
						
					//inArgs1[8] = "stoplists/"  +  category_stoplist[Integer.valueOf(cid)];
					inArgs1[8] = "/data/app/v3/stoplists/"  +  category_stoplist[Integer.valueOf(cid)];
					
					if ( bClusterEnable[Integer.valueOf(cid)] ) {
						
						//Topic Model and Clustering is required
						inArgs2[1] = inArgs1[3];
						
						int topic_num = (int)Math.floor(num * TopicRatio[Integer.valueOf(cid)] + 0.5);
						
						if ( topic_num == 0 ) {
							topic_num++;
						}
						
						if ( topic_num > 200 ) {
							topic_num = 200;
						}
							
						inArgs2[3] = topic_num + "";
						
						int story_num = (int)Math.floor(num * StoryRatio[Integer.valueOf(cid)] + 0.5);
						
						if (story_num == 0) {
							story_num++;
						}
						
						inArgs2[9]  = story_num + "";
						inArgs2[7] = languagePick.value.toString() + "_compostion" + cid + ".txt";
						
						System.out.println("Start Text2Vectors  \n");
						
						Text2Vectors.preMain(inArgs1);
						log.debug("Text2Vectors done");
						
						System.out.println("Start Vectors2Cluster  \n");
						// LDA-KMEANS
						Clustering  clustering = Vectors2Cluster.preMain(inArgs2);
						log.debug("Vectors2Cluster done");						
						category_data = Convert(clustering, metric);
						
					} else {
						//Skip Topic Model and Clustering for specific category
						InstanceList instances = Text2Vectors.preMain(inArgs1);
						log.debug("Text2Vectors done");
						
						category_data = Convert_NonClustering(instances);
					}
					
					

									
					//Redis_Info.saveInRedis(clusters);
					Redis_Info.saveInRedisII(category_data, cid);	
					log.debug("杈撳叆redis瀹屾瘯");
					//writeInFile(inArgs2, clusters);
					log.debug("淇℃伅鍐欏叆鏂囦欢瀹屾瘯");
				}
				
				log.debug("cmsContent.size():"+cmsContent.size());
			} else {
				System.exit(-1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
//			if (jedis == null) {
//				log.debug("jedis is null");
//				System.out
//						.println(new JedisConnectionException("jedis is null")
//								.getStackTrace());
//				throw new JedisConnectionException("jedis is null");
//			}
			jedis.set("isRun", "false");
		}
	}

	
	public static List<NewsClassStat> DBgetCountGroupCid(String max_editdata,
			String min_editdata) {
		try {
			// Reader cmUnAccReader =
			// Resources.getResourceAsReader("Configuration_api.xml");
			//
			// SqlSessionFactory sessionFactory = new
			// SqlSessionFactoryBuilder().build(cmUnAccReader);
			// DBservice db = new DBservice();
			SqlSession cmUnAccSession = (new DBservice("eng")).getSqlSessionApi();
			// SqlSession cmUnAccSession = sessionFactory.openSession();
			CmsContentDao cmsContentDao = cmUnAccSession
					.getMapper(CmsContentDao.class);
			List<NewsClassStat> list = cmsContentDao.getCountByCid(
					max_editdata, min_editdata);
			return list;

		} catch (Exception e) {
			e.printStackTrace();

		}
		return null;
	}

	private static Clustering algorithmExec(String[] inArgs1,
			String[] inArgs2) {
		try {
			// NLP
			Text2Vectors.preMain(inArgs1);
			log.debug("Text2Vectors done");
			// LDA-KMEANS
			Clustering  clustering = Vectors2Cluster.preMain(inArgs2);
			log.debug("Vectors2Cluster done");

			return clustering;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static List<DocumentDescription> Convert_NonClustering (InstanceList instances) {
		
		List<DocumentDescription> list_one_category = new ArrayList<DocumentDescription>();
		
		for ( int i = 0; i < instances.size(); i++ ) {
			
			
			String filePath = instances.get(i).getName().toString();
			
			int a = filePath.lastIndexOf(".");
			int b = filePath.lastIndexOf("/");
			String sub = filePath.substring(b+1, a);
			String[] sA = sub.split("_");
			String fileID = sA[0];
			int PR = Integer.valueOf(sA[1]);
			int time = Integer.valueOf(sA[2]);
			int Layout_Score = Integer.valueOf(sA[3]);
			
			int c = filePath.lastIndexOf("/", b-1);
			String cid = filePath.substring(c+1, b);			  
			  
			DocumentDescription cur_doc = new DocumentDescription(fileID, i, filePath, Layout_Score, time, PR, 1);
			
			list_one_category.add(cur_doc);			
			
		}
		
		return list_one_category;
	}

	private static List<DocumentDescription> Convert (Clustering clustering, Metric metric) {
		
		//Map<String,Map<String,Double>> map = new HashMap<String,Map<String,Double>>();
		
		List<DocumentDescription> list_one_category = new ArrayList<DocumentDescription>();
		
		int numClusters = clustering.getNumClusters();
		InstanceList instances =  clustering.getInstances();
		int numInstances = instances.size();
		InstanceList[] clusters = clustering.getClusters();		
		
		//StringBuilder builder = new StringBuilder();
		
		//final String Reg = "/\\d+";
		//Pattern p = Pattern.compile(Reg);
		
		
		for ( int i = 0; i < numClusters; i++ ) {
			  
			  for (int j = 0; j < clusters[i].size(); j++) {
				  
				  if ( clusters[i].get(j).getName() != null ) {
					  
					  String filePath = clusters[i].get(j).getName().toString();
						//filePath:/D:/pcloud/Mallet/db/1/10753_0_1409154849_1.txt
						//                             c b                    a   
			  
					  int a = filePath.lastIndexOf(".");
					  int b = filePath.lastIndexOf("/");
					  String sub = filePath.substring(b+1, a);
					  
					  String[] sA = sub.split("_");
					  String fileID = sA[0];
					  int PR = Integer.valueOf(sA[1]);
					  int time = Integer.valueOf(sA[2]);
					  int Layout_Score = Integer.valueOf(sA[3]);					  
					  int c = filePath.lastIndexOf("/", b-1);
					  String cid = filePath.substring(c+1, b);
					  
					  
					  DocumentDescription cur_doc = new DocumentDescription(fileID, i, filePath, Layout_Score, time, PR, clusters[i].size());
					  
					  if ( clusters[i].size() > 1 ) {
						  
						  for ( int k = 0; k < clusters[i].size(); k++) {
							  
							  if ( k == j ) {
								  continue;
								  }
							  
							  double similarity = metric.distance((SparseVector)clusters[i].get(j).getData(), (SparseVector)clusters[i].get(k).getData());
							  String negighbor_filePath = clusters[i].get(k).getName().toString();
							  
							  int Neighbor_a = negighbor_filePath.lastIndexOf(".");
							  int Neighbor_b = negighbor_filePath.lastIndexOf("/");
							  String Neighbor_sub = negighbor_filePath.substring(Neighbor_b+1, Neighbor_a);
							  
							  String[] Neighbor_sA = Neighbor_sub.split("_");
							  String Neighbor_fileID = Neighbor_sA[0];
							  
							  cur_doc.Add_Neigbor_Similarity(Neighbor_fileID, similarity);					  
						  
							  
						  }
						  
					  }
					  
					  list_one_category.add(cur_doc);
					  
					  
					  
					  //System.out.println(clusters[i].get(j).getName() + "Cluster: " + i + "\n");			  
					  
					  
					  
						
					}
				  
				}
			  
			  
		  }
		
		
		
		
		return list_one_category;
	}
	
	// 鎶婂弬鏁板拰缁撴灉鍐欏叆鏂囦欢
	public static void writeInFile(String[] inArgs2, InstanceList[] clusters) {
		try {
			// inArgs2[7] = "autorial_compostion"+cid+".txt";
			RandomAccessFile raf = new RandomAccessFile(inArgs2[7], "rw");
			raf.seek(raf.length());
			raf.writeBytes("\n\r");
			// 鍐欏叆鍙傛暟
			for (int i = 0; i < inArgs2.length; i++) {
				raf.writeBytes(inArgs2[i] + " ");
			}
			raf.writeBytes("\n\r");
			// 鍐欏叆缁撴灉
			for (int i = 0; i < clusters.length; i++) {
				InstanceList instList = clusters[i];
				raf.writeBytes(i + ":");
				for (int j = 0; j < instList.size(); j++) {
					raf.writeBytes(instList.get(j).getName().toString() + "|");
				}
				raf.writeBytes("\r\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
