package com.example.study;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*
callable은 runnable하고 다르게 값을 return 수 있다. 또한 예외가 발생했을 때, 예외가 발생했을 때 다 처리하지 않고 밖으로 던질 수 있다.
Future를 통해서 비동기 결과의 값을 가져올 수 있지만 get 메서드를 호출하게 되면 비동기 작업이 완료될 때까지 해당 스레드가 blocking된다.
 */

@Slf4j
public class FutureEx {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newCachedThreadPool();

        Future<String> f = es.submit(() -> {
            Thread.sleep(2000);
            log.info("Async");
            return "Hello";
        });

        log.info(f.get());
        log.info("Exit");
    }
}
