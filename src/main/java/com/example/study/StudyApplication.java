package com.example.study;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;

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
        // asynchronous 지원
        AsyncRestTemplate rt = new AsyncRestTemplate();

        /*
         CPU는 놀고 있다. 하는 일이라고는 외부의 다른 서버에 요청하고 대기하고 있는 상태.
         대기할 때는 CPU가 많이 필요하지 않다.
         이 문제를 해결하기 위해서는 API를 호출하는 이 작업을 비동기적으로 바꿔야 한다. 이 작업이 끝나기 전에
         바로 리턴을 하고, 이 API가 대기하는 동안 썼던 그 스레드를 다음 요청을 처리하도록 바로 이용한다.
         (실제 결과를 받고 클라이언트에 리턴하기 위해서는 새로운 스레드를 할당 받아야 겠지만, 외부 API 호출동안은
         스레드 자원을 낭비하고 싶지 않다가 목적)

         스프링 3.x대에는 이 문제를 간단히 해결하기 어려웠다.
         AsyncRestTemplate을 사용할 때 ListenableFuture를 바로 리턴하면 된다.
         스프링은 컨트롤러가 ListenableFuture를 리턴하면 해당 스레드는 즉시 반납한다. 그리고 결과가 후에 오면
         콜백은 스프링 MVC가 알아서 콜백을 등록하고 컨트롤러가 던지려했던 응답 형태로 처리한다.

         실행 후 스레드를 살펴보면, tomcat 스레드는 그대로 1개 이다. 그러나 비동기 작업을 처리하기 위해서
         백그라운드에 100개의 스레드 새로 생성한다.
         */
        @GetMapping("/rest")
        public ListenableFuture<ResponseEntity<String>> rest(int idx) {
            return rt.getForEntity("http://localhost:8081/service?req={req}",
                    String.class, "hello" + idx);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(StudyApplication.class, args);
    }
}
