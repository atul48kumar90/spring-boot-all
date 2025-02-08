package com.example.crud.demo;

import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;


import reactor.core.publisher.Mono;

public class ConcurrentEmployeeInsterter {
    
    private static final int total_thread = 10;
    private static final int total_req = 1000;
    private static final String url = "http://localhost:8080/api/employee/employees";
    private static final WebClient webClient = WebClient.create();

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(total_req);

        IntStream.range(0, total_req).forEach(id -> {
            createEmployee(id).doFinally(signal -> countDownLatch.countDown()).subscribe();
        });

        countDownLatch.await();
        System.out.println("All request completed");
    }

    public static Mono<String> createEmployee(int id)
    {
        String jsonPayload = String.format("""
            {
                "name": "Employee %d",
                "role": "Developer",
                "salary": 50000
            }
            """, id);

            return webClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(jsonPayload)
            .retrieve()
            .bodyToMono(String.class)
            .doOnNext(response -> System.out.println("Response for Employee " + id + ": " + response));
    }
}
