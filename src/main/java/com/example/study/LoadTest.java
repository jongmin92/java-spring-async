package com.example.study;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// 100개의 스레드가 만들어지면서 순차적으로 요청을 하게되는게 문제
// 동시에 100개를 실행하려고 한다면? 동기화가 필요하다. CyclicBarrier를 사용한다. 어느 순간에 경계같은 것읆 만들어 사용

@Slf4j
public class LoadTest {
    static AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        ExecutorService es = Executors.newFixedThreadPool(100);

        RestTemplate rt = new RestTemplate();
        String url = "http://localhost:8080/rest?idx={idx}";

        CyclicBarrier barrier = new CyclicBarrier(101);

        for (int i = 0; i < 100; i++) {
            es.submit(() -> {
                int idx = counter.addAndGet(1);
                log.info("Thread {}", idx);

                // await을 만난 스레드가 100번째가 될 때 모든 스레드들도 await에서 풀려난다.
                // submit이 받는 callable은 return을 가질 수 있으며, exception도 던질 수 있다.
                barrier.await();

                StopWatch sw = new StopWatch();
                sw.start();

                String res = rt.getForObject(url, String.class, idx);

                sw.stop();
                log.info("Elapsed: {} {} / {}", idx, sw.getTotalTimeSeconds(), res);
                // IDE가 callable임을 인식할 수 있도록 return
                return null;
            });
        }

        barrier.await();
        StopWatch main = new StopWatch();
        main.start();

        es.shutdown();
        // 지정된 시간이 타임아웃 걸리기 전이라면 대기작업이 진행될 때까지 기다린다.
        // (100초안에 작업이 끝날때까지 기다리거나, 100초가 초과되면 종료)
        es.awaitTermination(100, TimeUnit.SECONDS);
        main.stop();
        log.info("Total: {}", main.getTotalTimeSeconds());
    }
}
