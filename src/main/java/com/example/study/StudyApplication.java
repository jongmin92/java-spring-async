package com.example.study;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;

/*
 Servlet 3.0: 비동기 서블릿
  - HTTP connection은 이미 논블록킹 IO
  - 서블릿 요청 읽기, 응답 쓰기는 블록킹
  - 비동기 작업 시작 즉시 서블릿 쓰레드 반납
  - 비동기 작업이 완료되면 서블릿 쓰레드 재할당
  - 비동기 서블릿 컨텍스트 이용 (AsyncContext)
 Servlet 3.1: 논블록킹 IO
  - 논블록킹 서블릿 요청, 응답 처리
  - Callback

  쓰레드가 블록되는 상황은 CPU와 메모리 자원을 많이 먹는다. 컨텍스트 스위칭이 일어나기 때문.
  기본적으로 블록이 되면 wating 상태로 빠지고 추후 running 상태로 변경되면서 컨텍스트 스위칭이 2번 일어나면서 불필요하게
  CPU 자원이 소모된다.
  Java InputStream과 OutputStream은 블록킹 방식이다. RequestHttpServletRequest, RequestHttpServletResponse는
  InputSream과 OutputStream을 사용하기 때문에 서블릿은 기본적으로 블로킹 IO 방식이다.
 */

@SpringBootApplication
@EnableAsync
@Slf4j
public class StudyApplication {

    @RestController
    public static class MyController {
        @GetMapping("/callable")
        public Callable<String> callable() {
            log.info("callable");
            return () -> {
                log.info("async");
                Thread.sleep(2000);
                return "hello";
            };
        }

//        public String callable() throws InterruptedException {
//            log.info("async");
//            Thread.sleep(2000);
//            return "hello";
//        }
    }

    @Bean
    ThreadPoolTaskExecutor tp() {
        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
        te.setCorePoolSize(100);
        te.setQueueCapacity(50);
        te.setMaxPoolSize(200);
        te.setThreadNamePrefix("workThread");
        return te;
    }

    public static void main(String[] args) {
        SpringApplication.run(StudyApplication.class, args);
    }
}
