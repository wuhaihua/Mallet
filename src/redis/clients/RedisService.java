package redis.clients;

import redis.clients.jedis.Jedis;


public class RedisService {

	
	public Jedis getJedis(){
	  try{	
//		AppContext app = AppContext.INSTANCE;
//		app.init();
//		return app.getRedisPool().getResource();
		  return null;
	  }catch(Exception e){e.printStackTrace(); return null;}
	  
   }
	
}
