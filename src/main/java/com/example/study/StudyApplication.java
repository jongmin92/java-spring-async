package com.example.study;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

/*
 앞단에 서블릿 요청을 받아서 서블릿 스레드를 할당받고 하는것을 비동기로 효율적으로 처리하도록 만들었는데,
 문제는 뒷단의 작업들. 아주 빠르게 뭔가를 계산하고 리턴하는 경우라면 굳이 비동기 MVC를 걸지않고 바로 작업을 처리하고
 리턴해도 상관없다. 그렇지만 그게 아니라 백단의 어떤 별개의 서비스들을 호출하는게 많이 있는 경우, 문제는 단순히
 비동기를 서블릿을 사용하는 것만으로 해결할 수 없는 경우가 많이 있다.

 Thread Pool Hell
 스레드 풀이 순간 요청이 급격하게 몰려오면 풀이 가득차게 된다. 추가적인 요청이 들어오면 대기 상태에 빠져 요청에 대한
 응답이 느려지게 된다.

 최근 서비스들은 하나의 요청을 처리함에 있어 다른 서버로의 요청(Network I/O)이 많아졌다. 해당 요청을 처리하는
 하나의 스레드는 그 시간동안 대기상태에 빠지게 된다. 스레드 풀이 꽉 차버리는 현상이 발생하게 된다.
 */

@SpringBootApplication
@Slf4j
public class StudyApplication {

    @RestController
    public static class MyController {
        // asynchronous + netty non-blocking
        AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

        /*
         tomcat 스레드 1개, netty가 필요로 하는 약간의 스레드가 추가된 것 말고는 스레드 수가 크게 증가하지 않음
         */
        @GetMapping("/rest")
        public DeferredResult<String> rest(int idx) {
            // 오브젝트를 만들어서 컨트롤러에서 리턴하면 언제가 될지 모르지만 언제인가 DeferredResult에 값을 써주면
            // 그 값을 응답으로 사용
            DeferredResult<String> dr = new DeferredResult<>();

            ListenableFuture<ResponseEntity<String>> f1 = rt.getForEntity("http://localhost:8081/service?req={req}",
                    String.class, "hello" + idx);
            f1.addCallback(s -> {
                dr.setResult(s.getBody() + "/work");
            }, e -> {
                dr.setErrorResult(e.getMessage());
            });

            return dr;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(StudyApplication.class, args);
    }
}
