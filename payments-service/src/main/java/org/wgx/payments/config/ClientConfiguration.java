package org.wgx.payments.config;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.wgx.payments.clients.AssembledShardedJedis;
import org.wgx.payments.clients.RedisClient;
import org.wgx.payments.clients.RedisClientImpl;

import redis.clients.jedis.Tuple;

@Configuration
public class ClientConfiguration {

    private static final String PASSWORD = "org.wgx.payments.redis.password";
    private static final String SENTINELS = "org.wgx.payments.redis.sentinels";
    private static final String MASTERS = "org.wgx.payments.redis.sentinels";

    @Resource
    private Environment environment;

    @Profile(value = {"online", "beta", "gamma"})
    @Bean(name = {"redisService", "redisClient"}, initMethod = "init")
    public RedisClient redisService() {
        RedisClientImpl bean = new RedisClientImpl();
        bean.setPassword(environment.getProperty(PASSWORD));
        bean.setSentinels(environment.getProperty(SENTINELS));
        bean.setMasters(environment.getProperty(MASTERS));
        return bean;
    }

    @Profile(value = {"dev", "test"})
    @Bean(name = {"redisService", "redisClient"}, initMethod = "init")
    public RedisClient mockRedisService() {
        return new RedisClient() {
            @Override
            public Set<Tuple> zrange(String key, int begin, int end) {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public Long zadd(String key, String value, double score) {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public long sremove(String setKey, String... memebers) {
                // TODO Auto-generated method stub
                return 0;
            }
            
            @Override
            public Long setnx(String key, String value) {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public boolean setWithExpireTime(String key, String value, int seconds) {
                // TODO Auto-generated method stub
                return false;
            }
            
            @Override
            public boolean set(String key, String value) {
                // TODO Auto-generated method stub
                return false;
            }
            
            @Override
            public Long sadd(String key, String value) {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public void returnResource(AssembledShardedJedis jedis) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void returnBrokenResource(AssembledShardedJedis jedis) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public boolean lrem(String key, int count, String value) {
                // TODO Auto-generated method stub
                return false;
            }
            
            @Override
            public List<String> lrange(String key, long start, long end) {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public Long lpush(String key, String... value) {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public List<String> getList(String setName, int limit) {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public AssembledShardedJedis getJedis() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public String get(String key) {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public boolean del(String key) {
                // TODO Auto-generated method stub
                return false;
            }
        };
    }
}
