package org.example.taskeventconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class TaskEventConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskEventConsumerApplication.class, args);
    }
}