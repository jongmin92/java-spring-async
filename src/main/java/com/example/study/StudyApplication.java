package com.example.study;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.CompletableFuture;

/*
 이번에는 자바8에 나온 CompletableFuture라는 새로운 비동기 자바 프로그래밍 기술에 대해서 알아보고, 지난 3회 정도 동안
 다루어 왔던 자바 서블릿, 스프링의 비동기 기술 발전의 내용을 자바 8을 기준으로 다시 재작성 한다.
 */
@SpringBootApplication
@EnableAsync
@Slf4j
public class StudyApplication {

    @RestController
    public static class MyController {
        /*
         Spring 4.0에 들어간 AsyncRestTemplate이 return하는 것은 CompletableFuture가 아니다.
         Spring 4까지는 자바 6~8을 지원하기 때문에 CompletableFuture로 return을 만들지 못했다.
         계속 ListenableFuture를 유지하고 있다.
         ListenableFuture를 CompletableFuture로 wrapping하면 된다.
         */
        AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

        @Autowired
        MyService myService;

        static final String URL1 = "http://localhost:8081/service?req={req}";
        static final String URL2 = "http://localhost:8081/service2?req={req}";

        @GetMapping("/rest")
        public DeferredResult<String> rest(int idx) {
            DeferredResult<String> dr = new DeferredResult<>();

            toCF(rt.getForEntity("http://localhost:8081/service?req={req}", String.class, "hello" + idx))
                    .thenCompose(s -> toCF(rt.getForEntity("http://localhost:8081/service2?req={req}", String.class, s.getBody())))
                    .thenCompose(s -> toCF(myService.work(s.getBody())))
                    .thenAccept(s -> dr.setResult(s))
                    .exceptionally(e -> {
                        dr.setErrorResult(e.getMessage());
                        return null;
                    });

//            f1.addCallback(s -> {
//                ListenableFuture<ResponseEntity<String>> f2 = rt.getForEntity("http://localhost:8081/service2?req={req}", String.class, s.getBody());
//                f2.addCallback(s2 -> {
//                    ListenableFuture<String> f3 = myService.work(s2.getBody());
//                    f3.addCallback(s3 -> {
//                        dr.setResult(s3);
//                    }, e -> {
//                        dr.setErrorResult(e.getMessage());
//                    });
//                }, e -> {
//                    dr.setErrorResult(e.getMessage());
//                });
//            }, e -> {
//                dr.setErrorResult(e.getMessage());
//            });

            return dr;
        }

        <T> CompletableFuture<T> toCF(ListenableFuture<T> lf) {
            CompletableFuture<T> cf = new CompletableFuture<>();
            lf.addCallback(s -> cf.complete(s), e -> cf.completeExceptionally(e));
            return cf;
        }
    }

    @Service
    public static class MyService {
        @Async
        public ListenableFuture<String> work(String req) {
            return new AsyncResult<>(req + "/asyncwork");
        }
    }

    @Bean
    public ThreadPoolTaskExecutor myThreadPool() {
        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
        te.setCorePoolSize(1);
        te.setMaxPoolSize(1);
        te.initialize();
        return te;
    }

    public static void main(String[] args) {
        SpringApplication.run(StudyApplication.class, args);
    }
}
