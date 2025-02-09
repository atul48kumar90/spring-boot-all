package com.example.crud.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String EMPLOYEE_CACHE_KEY = "EMPLOYEE_";
    private static final String EMPLOYEE_KEYS_SET = "EMPLOYEE_KEYS_SET";
    private static final int MAX_CACHE_SIZE = 5;
    static int CACHE_HITS = 0;
    static int CACHE_MISS = 0;

    @CachePut(value = "employee", key="#employee.id")
    public Employee createEmployee(Employee employee) {
        Employee savedEmployee =  employeeRepository.save(employee);
        cachedEmployee(savedEmployee);        
        return savedEmployee;
    }

    public Map<String, Integer> getCachePerformanceStats() {
        return Map.of("Cache Hits", CACHE_HITS, "Cache Misses", CACHE_MISS);
    }


    @Cacheable(value = "employee", key="#id", unless = "#result == null")
    public Optional<Employee> getEmployeeById(long id) {
        Employee cachedEmployee = (Employee)redisTemplate.opsForValue().get(EMPLOYEE_CACHE_KEY + id);
        if(cachedEmployee != null)
        {
            CACHE_HITS++;
            return Optional.of(cachedEmployee);
        }
        CACHE_MISS++;
        Optional<Employee> empl = employeeRepository.findById(id);
        empl.ifPresent(this::cachedEmployee);        
        return empl;
    }

    public List<Employee> getAllEmployee(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit); // Page 0 with `limit` records
        Page<Employee> page = employeeRepository.findAll(pageRequest); // Use built-in method
        return page.getContent();
    }

    public void deleteEmployeeById(long id) {
        employeeRepository.deleteById(id);
        redisTemplate.delete(EMPLOYEE_CACHE_KEY + id);
    }

    @Async
    public void saveCSVFile(MultipartFile file)
    {
        try(CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))){
            List<String[]> rows = reader.readAll();
            rows.forEach(row -> {
                Employee emp = new Employee(row[0], row[1], row[2]);
                createEmployee(emp);
            });
        } catch(Exception e)
        {
            System.out.println("got some issue");
        }
    }

    private void cachedEmployee(Employee employee)
    {
        Long size = redisTemplate.opsForZSet().size(EMPLOYEE_CACHE_KEY);
        if(size != null && size >= MAX_CACHE_SIZE)
        {
            Set<Object> lruKeys = redisTemplate.opsForZSet().range(EMPLOYEE_CACHE_KEY, 0, 0);
            if(lruKeys != null && !lruKeys.isEmpty())
            {
                String lruKey = (String) lruKeys.iterator().next();
                redisTemplate.delete(lruKey);
                redisTemplate.opsForZSet().remove(EMPLOYEE_KEYS_SET, lruKey);
            }
        }

        redisTemplate.opsForValue().set(EMPLOYEE_CACHE_KEY + employee.getId(), employee, 10, TimeUnit.MINUTES);
        redisTemplate.opsForZSet().add(EMPLOYEE_KEYS_SET, EMPLOYEE_CACHE_KEY + employee.getId(), System.currentTimeMillis());
    }
}
