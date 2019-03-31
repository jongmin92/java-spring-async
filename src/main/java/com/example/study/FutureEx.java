package com.example.study;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
자바의 `Future`를 잘 알아야 한다. 자바에서는 Future가 가장 기본이 되는 interface이다. 자바 1.5에서 등장했다.
문서를 참고하면 Future라는건 비동기적인 작업을 수행하고난 결과를 나타내는 것이다. 비동기적인 무엇인가 연산 혹은 작업을 수행하고 그 결과를 갖고 있는 것이다. (비동기적인 작업을 수행한다는 것은 현재 내가 진행하고 있는 스레드가 아닌 별도의 스레드에서 작업을 수행하는 것이다. 같은 스레드에서는 메서드를 호출할 때는 리턴값을 받으면 되지만 비동기적으로 작업을 수행할 때는 결과값을 전달받을 수 있는 무언가의 interface가 필요하다.)
 */

@Slf4j
public class FutureEx {
    public static void main(String[] args) {
        ExecutorService es = Executors.newCachedThreadPool();

        es.execute(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {}
            log.info("Async");
        });

        log.info("Exit");
    }
}
