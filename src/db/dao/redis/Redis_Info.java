/**
 * 
 */
/**
 * @author pc
 *
 */
package db.dao.redis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.derby.iapi.util.StringUtil;
import org.omg.CORBA.portable.UnknownException;

import com.cmcm.AppContext;
import com.cmcm.DocumentDescription;
import com.cmcm.NeighborSimilarity;
import com.cmcm.Main;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import cc.mallet.types.InstanceList;

 

public class Redis_Info{
	
	private static final Log log = LogFactory.getLog(Main.class);
	private JedisPool redisPool;
	
	public static JedisPool initRedis(){
		try{
		  JedisPoolConfig jedisConf = new JedisPoolConfig();
		  jedisConf.setMaxTotal(2000);//"10.33.41.135"  //"127.0.0.1",6380
		  //return new JedisPool(new JedisPoolConfig(), "10.33.41.135",6379, 2000);
		  //return new JedisPool(new JedisPoolConfig(), "10.33.20.66",6402, 2000);	//LinGuang's machine for debugging
//		  return new JedisPool(new JedisPoolConfig(), "10.10.16.5",6380, 2000, "b73e852ae4c79a1932f58eb67e05ca38");
		  if(AppContext.INSTANCE.isTestRunMod()){//test environment
			  return new JedisPool(new JedisPoolConfig(), 
					  AppContext.INSTANCE.getConfigValue("test.redis.host"),
					  Integer.parseInt(AppContext.INSTANCE.getConfigValue("test.redis.port")), 
					  Integer.parseInt(AppContext.INSTANCE.getConfigValue("test.redis.timeout")));
		  }else{
			  return new JedisPool(new JedisPoolConfig(), 
					  AppContext.INSTANCE.getConfigValue("redis.host"),
					  Integer.parseInt(AppContext.INSTANCE.getConfigValue("redis.port")), 
					  Integer.parseInt(AppContext.INSTANCE.getConfigValue("redis.timeout")), 
					  AppContext.INSTANCE.getConfigValue("redis.pwd"));
		  }
		
		}catch(JedisConnectionException e){
			e.printStackTrace();
			throw e;
		}
	}
	
	
	//存clusters到redis中
	public static void saveInRedis(InstanceList[] clusters){
		JedisPool jedispool  = null;
		Jedis jedis = null;
	   try{	    
		//    cid       PR_id  editdata
		Map<String,Map<String,Double>> map = parseClusters(clusters,"a");
		
//		System.out.println("map.size()"+map.get("1").size());
	    jedispool = Redis_Info.initRedis();
	    jedis = jedispool.getResource();
	    
	    Iterator<String> it = map.keySet().iterator();
	    while(it.hasNext()){
	        String cid = it.next();
	        //f_news_class_cid
	        jedis.del("f_news_class_"+cid);
	        jedis.zadd("f_news_class_"+cid, map.get(cid));	        
	    }
	    
	   }catch(JedisConnectionException e){
			e.printStackTrace();
			jedispool.returnBrokenResource(jedis);
			throw e;
	   }catch(Exception e){
		   e.printStackTrace();
		   jedispool.returnBrokenResource(jedis);
	   } finally {
		   jedispool.returnResource(jedis);
		   jedispool.destroy();
	   }
	   
	}
	
	public static void saveInRedisII(List<DocumentDescription> category_data, String category_id){
		JedisPool jedispool = null;
	    Jedis jedis = null;  
		try{	    
			//    cid       PR_id  editdata
			Map<String,Double> map = parseDocumentDescription(category_data);
//			
		    jedispool = Redis_Info.initRedis();
		    jedis = jedispool.getResource();
		    
		    jedis.del("f_news_class_"+category_id);
		    jedis.zadd("f_news_class_"+category_id, map);		    	
		    
		   }catch(JedisConnectionException e){
				e.printStackTrace();
				jedispool.returnBrokenResource(jedis);
				throw e;
		   }catch(Exception e){
			   e.printStackTrace();
			   jedispool.returnBrokenResource(jedis);
		   }finally{
			   jedispool.returnResource(jedis);
			   jedispool.destroy();
		   }
		   
		}

		
	//clusters-Map<Cid,Map<String,Double>> PR-EditDate
	//参数ao, a-把所有记录都放到redis中，o-只把每类第一个元素放到redis中
	public static Map<String,Map<String,Double>> parseClusters(InstanceList[] clusters,String ao){
		Map<String,Map<String,Double>> map = new HashMap<String,Map<String,Double>>();
		for(int i=0;i<clusters.length;i++){
			InstanceList inst = clusters[i];
			int count=5;
		    if(ao.equals("o")){
			      String filePath = inst.get(0).getName().toString();
//			   System.out.println("filePath:"+filePath);
			      fileInMap(map, filePath,5);
		    }else if(ao.equals("a")){
		       for(int j=0;j<inst.size();j++){
		    	   String filePath = inst.get(j).getName().toString();
		    	   log.debug("filePath："+filePath);
//				   System.out.println("filePath:"+filePath);
		    	   //PR|ID|editdata|score
		    	   //5-1, 4-0.8, 3-0.6, 2-0.4, 1-0.2,其余0	    	   
				   fileInMap(map, filePath,count);
				   count--;
		       }
		    }else{
		       throw new IllegalArgumentException("ao参数错误");
		    }
			
		}
//		System.out.println("mapInter.size:"+mapInter.size());
		return map;
	}
	
	
	public static Map<String,Double> parseDocumentDescription(List<DocumentDescription> category_data){
		Map<String,Double> map = new HashMap<String,Double>();
		for(int i=0;i<category_data.size();i++){
			DocumentDescription doc_inst = category_data.get(i);
			//int count=5;
			
			StringBuilder builder = new StringBuilder();
			
			
			
			//builder.append(doc_inst.Get_Article_ID() + "|" + doc_inst.Get_PageRank() + "|" + doc_inst.Get_time() + "|" + doc_inst.Get_Layout_Score() + "|" + doc_inst.Get_Cluster_Number());
			builder.append(doc_inst.Get_PageRank() + 
					"|" + doc_inst.Get_Article_ID() + 
					"|"  + doc_inst.Get_time()  + 
					"|" + doc_inst.Get_Cluster_Number() + 
					"|" + doc_inst.Get_Layout_Score() + 
					"|" + doc_inst.Get_Cluster_ID() +
					"|" + doc_inst.Get_Copy_Number() +
					"|" + doc_inst.Get_Pic_Number() +
					"|" + doc_inst.Get_Content_Length());
			
			if (doc_inst.Get_Article_ID().equals("141327")) {
				log.info("Article ID = " + doc_inst.Get_Article_ID());
			}
			
			
			int count = doc_inst.Get_Neighbor_Size();
			
			for (int j = 0; j < count; j++){
				
				NeighborSimilarity neibhbor = doc_inst.GetNeighbor(j);
				builder.append("|" + neibhbor.Get_ID() + "|" + neibhbor.Get_Similarity());				
				
			}
			
			map.put(builder.toString(), 0D);
		    
			
		}
//		System.out.println("mapInter.size:"+mapInter.size());
		return map;
	}


	public static Map<String, Double> fileInMap(Map<String, Map<String, Double>> map,
			String filePath,int count) {
		Map<String, Double> mapInter;
		//filePath:/D:/pcloud/Mallet/db/1/10753_0_1409154849_1.txt
		//                             c b                    a   
		int a = filePath.lastIndexOf(".");
		int b = filePath.lastIndexOf("/");
		String sub = filePath.substring(b+1, a);    	
		String[] sA = sub.split("_");
		String id = sA[0];
		String PR = sA[1];
		int editDate = Integer.valueOf(sA[2]);
		int importantLevel = Integer.valueOf(sA[3]);
		int c = filePath.lastIndexOf("/", b-1);
		String cid = filePath.substring(c+1, b);
//			System.out.println("id:"+id+" editDate:"+editDate+" PR:"+PR+" cid:"+cid);
		//PR|ID|editdata|score
		String score = getScore(count);
		
		
		if(null==(mapInter=map.get(cid))){
			Map<String,Double> mapNew = new HashMap<String,Double>();
			map.put(cid, mapNew);
			log.debug("keyName:"+PR+"|"+id+"|"+editDate+"|"+score+"|"+importantLevel);
			mapNew.put(PR+"|"+id+"|"+editDate+"|"+score+"|"+importantLevel, 0D);
		}else{
			log.debug("keyName:"+PR+"|"+id+"|"+editDate+"|"+score+"|"+importantLevel);
			mapInter.put(PR+"|"+id+"|"+editDate+"|"+score+"|"+importantLevel, 0D);
		}
		
		
		return mapInter;
	}


	public static String getScore(int count) {
		String score="";
		if(count<=0){
			score=0+"";
		}else{
		    score = (double)count/5+"";
		}
		return score;
	}
	
	//��clusters��ÿ��Ԫ�ض�����redis�� PR|id|editdata|score
	public void saveInRedis1(InstanceList[] clusters){
		JedisPool jedispool = null;
		Jedis jedis = null;
		try{		    
					//    cid       PR_id  editdata
			Map<String,Map<String,Double>> map = parseClusters1(clusters);
					
			jedispool = this.initRedis();
			jedis = jedispool.getResource();
				    
			Iterator<String> it = map.keySet().iterator();
			while(it.hasNext()){
				 String cid = it.next();
				 //��ɾ��֮ǰ��f_news_class_cid
				 jedis.del("f_news_class_"+cid);
				 jedis.zadd("f_news_class_"+cid, map.get(cid));
			}
				    
		}catch(JedisConnectionException e){
			 e.printStackTrace();
			 jedispool.returnBrokenResource(jedis);
			 throw e;
		}finally{
			jedispool.returnResource(jedis);
			jedispool.destroy();
		}
    }	
	
	//clusters��Ϣ����Map<Cid,Map<String,Double>> PR-EditDate
	public Map<String,Map<String,Double>> parseClusters1(InstanceList[] clusters){
		Map<String,Map<String,Double>> map = new HashMap<String,Map<String,Double>>();
		Map<String,Double> mapInter = null;
		for(int i=0;i<clusters.length;i++){
			InstanceList inst = clusters[i];
			//filePath �� file:/D:/pcloud/Mallet/db/1/10753_0_1409154849.txt
			//                                    c b                  a   
		  for(int j=0;j<inst.size();j++){	
			String filePath = inst.get(j).getName().toString();
            mapInter = fileInMap(map, filePath,0);		
			
		}
		}
		System.out.println("mapInter.size:"+mapInter.size());
		return map;
	}
}