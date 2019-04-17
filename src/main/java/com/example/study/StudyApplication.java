package com.example.study;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
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

import java.util.function.Consumer;

/*
 이번에는 콜백핼을 어떻게 개선할지에 대해서 이야기한다.
 ListenableFuture를 Wrapping 하는 클래스를 만들어, chainable하게 사용할 수 있는 방식으로 코드를 만들어본다.
 콜백핼의 문제로는 에러를 처리하는 코드가 중복이 된다는 것도 있다. 이 부분도 해결해보자.
 */
@SpringBootApplication
@EnableAsync
@Slf4j
public class StudyApplication {

    @RestController
    public static class MyController {
        AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

        @Autowired
        MyService myService;

        static final String URL1 = "http://localhost:8081/service?req={req}";
        static final String URL2 = "http://localhost:8081/service2?req={req}";

        @GetMapping("/rest")
        public DeferredResult<String> rest(int idx) {
            DeferredResult<String> dr = new DeferredResult<>();

            Completion
                    .from(rt.getForEntity(URL1, String.class, "hello" + idx))
                    .andAccept(s -> dr.setResult(s.getBody()));

//            ListenableFuture<ResponseEntity<String>> f1 = rt.getForEntity("http://localhost:8081/service?req={req}", String.class, "hello" + idx);
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
    }

    public static class Completion {

        Consumer<ResponseEntity<String>> con;

        Completion next;

        public Completion() {
        }

        public Completion(Consumer<ResponseEntity<String>> con) {
            this.con = con;
        }

        public static Completion from(ListenableFuture<ResponseEntity<String>> lf) {
            Completion c = new Completion();
            lf.addCallback(s -> {
                c.complete(s);
            }, e -> {
                c.error(e);
            });
            return c;
        }

        public void andAccept(Consumer<ResponseEntity<String>> con) {
            Completion c = new Completion(con);
            this.next = c;
        }

        void complete(ResponseEntity<String> s) {
            if (next != null) next.run(s);
        }

        private void run(ResponseEntity<String> value) {
            if (con != null) con.accept(value);
        }

        private void error(Throwable e) {
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
