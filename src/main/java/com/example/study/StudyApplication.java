package com.example.study;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@SpringBootApplication
@EnableAsync
@Slf4j
public class StudyApplication {

    @Service
    public static class MyService {
        /*
         내부적으로 AOP를 이용해 복잡한 로직이 실행된다.
         비동기 작업은 return값으로 바로 결과를 줄 수 없다. (Future 혹은 Callback을 이용해야 한다.)
         */
        @Async
        public ListenableFuture<String> hello() throws InterruptedException {
            log.info("hello()");
            Thread.sleep(1000);
            return new AsyncResult<>("Hello");
        }
    }

    public static void main(String[] args) {
        // try with resource 블록을 이용해 빈이 다 준비된 후 종료되도록 설정
        try (ConfigurableApplicationContext c = SpringApplication.run(StudyApplication.class, args)) {
        }
    }

    @Autowired
    MyService myService;

    // 모든 빈이 다 준비된 후 실행됨 (현재는 일종의 컨트롤러라고 생각)
    @Bean
    ApplicationRunner run() {
        return args -> {
            log.info("run()");
            ListenableFuture<String> f = myService.hello();
            f.addCallback(s -> log.info(s), e-> log.info(e.getMessage()));
            log.info("exit");

            Thread.sleep(2000);
        };
    }
}
