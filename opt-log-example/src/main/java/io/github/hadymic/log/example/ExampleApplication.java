package io.github.hadymic.log.example;

import io.github.hadymic.log.annotation.EnableOptLog;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Hadymic
 */
@SpringBootApplication
@EnableOptLog
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
}
