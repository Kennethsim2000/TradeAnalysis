package com.example.Demo.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Demo.model.Greeting;

import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/publish")
@Slf4j
public class MessageController {
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    KafkaTemplate<String, Greeting> greetingKafkaTemplate;


    private static final String TOPIC = "tweets";

    @GetMapping
    public String publishMessage(@RequestParam String message)
    {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, message);
        String res = "";
        future.whenComplete((result, ex)-> {
            if(ex == null) {
                log.info("Message successfully published");
            } else {
                log.error(ex.getMessage());
            }
        });
        return "Message published";
    }

    @PostMapping
    public String publishGreeting(@RequestBody Greeting greeting)
    {
        CompletableFuture<SendResult<String, Greeting>> future = greetingKafkaTemplate.send(TOPIC, greeting);
        String res = "";
        future.whenComplete((result, ex)-> {
            if(ex == null) {
                log.info("Message successfully published");
            } else {
                log.error(ex.getMessage());
            }
        });
        return "Message published";
    }
}
