package com.example.study;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 DeferredResult: Spring 3.2 부터 사용 가능하다. 지연된 결과. 외부의 이벤트 혹은 클라이언트 요청에 의해서
 지연되어 있는 HTTP 요청에 대한 응답을 나중에 써줄 수 있는 기술
 워커 스레드를 별도로 만들어 대기하지 않고도 나중에 응답을 받을 수 있다.
 */

@SpringBootApplication
@EnableAsync
@Slf4j
public class StudyApplication {

    @RestController
    public static class MyController {
        Queue<DeferredResult<String>> results = new ConcurrentLinkedQueue<>();

        @GetMapping("/dr")
        public DeferredResult<String> dr() {
            log.info("dr");
            DeferredResult<String> dr = new DeferredResult<>();
            results.add(dr);
            return dr;
        }

        @GetMapping("/dr/count")
        public String drCount() {
            return String.valueOf(results.size());
        }

        @GetMapping("/dr/event")
        public String drEvent(String msg) {
            for (DeferredResult<String> dr : results) {
                dr.setResult("Hello " + msg);
                results.remove(dr);
            }
            return "OK";
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(StudyApplication.class, args);
    }
}
