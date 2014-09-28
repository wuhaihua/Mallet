package com.cmcm;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import db.dao.redis.Redis_Info;

public class Util {
	public static void main(String[] args) {
		Jedis jedis = null;
		JedisPool jedispool = Redis_Info.initRedis();
		jedis = jedispool.getResource();
		jedis.del("isRun");

	}
}
