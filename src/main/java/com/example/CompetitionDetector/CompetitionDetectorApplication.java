package com.example.CompetitionDetector;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
public class CompetitionDetectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompetitionDetectorApplication.class, args);
    }
}

@Component
@RequiredArgsConstructor
class CommandLineRunnerImpl implements CommandLineRunner {

    private static final int TIME_TO_WAIT_IN_MILLISECONDS = 20000;

    private final Service service;

    @Override
    public void run(String... args) throws Exception {

        service.initCompetitionInfo();
        while (true) {
            service.sendEmail();
            Thread.sleep(TIME_TO_WAIT_IN_MILLISECONDS);
        }
    }
}
