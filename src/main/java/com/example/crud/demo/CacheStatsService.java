package com.example.crud.demo;

import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.connection.RedisServerCommands;


@Service
public class CacheStatsService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisConnectionFactory redisConnectionFactory;

    public CacheStatsService(RedisTemplate<String, Object> redisTemplate, RedisConnectionFactory redisConnectionFactory)
    {
        this.redisTemplate = redisTemplate;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    public Map<String, Object> getCacheStat()
    {
        RedisServerCommands serverCommands = redisConnectionFactory.getConnection().serverCommands();
        long totalKeys = redisTemplate.keys("EMPLOYEE_*").size();

        Map<Object, Object>  memoryInfo = serverCommands.info("memory");

        Set<String> cacheKeys = redisTemplate.keys("EMPLOYEE_*");

        return Map.of(
            "Total Cached Employees", totalKeys,
            "Current Cache Keys", cacheKeys,
            "Memory Usage", memoryInfo.get("used_memory_human"),
            "Cache Hits", EmployeeService.CACHE_HITS,
            "Cache Misses", EmployeeService.CACHE_MISS
        );
    }
}
