package com.studygrind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StudyGrindApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyGrindApplication.class, args);
    }
}