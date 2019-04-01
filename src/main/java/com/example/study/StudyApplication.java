package com.example.study;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.concurrent.Executors;

@SpringBootApplication
@EnableAsync
@Slf4j
public class StudyApplication {

    @RestController
    public static class MyController {

        @GetMapping("/emitter")
        public ResponseBodyEmitter emitter() {
            ResponseBodyEmitter emitter = new ResponseBodyEmitter();

            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    for (int i = 0; i < 50; i++) {
                        emitter.send("<p>Stream " + i + "</p>");
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                }
            });

            return emitter;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(StudyApplication.class, args);
    }
}
