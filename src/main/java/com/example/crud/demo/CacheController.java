package com.example.crud.demo;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cache")
public class CacheController {
    
    private final CacheStatsService cacheStatsService;

    public CacheController(CacheStatsService cacheStatsService)
    {
        this.cacheStatsService = cacheStatsService;
    }

    @GetMapping("/stats")
    public Map<String, Object> getCacheStatistics() {
        return cacheStatsService.getCacheStat();
    }
}
