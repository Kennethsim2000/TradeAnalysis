package com.example.Demo.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.Demo.model.Greeting;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class Listener {
    // to do this, requires an @EnableKafka annotation on your @Configuration classes and a listener container factory.
    //By default, a bean with name kafkaListenerContainerFactory is expected.
    // The listener container factory is used to configure the underlying ConcurrentMessageListenerContainer. This is a
    //spring managed container that allows concurrent processing of messages from kafka topics by managing multiple kafka
    //consumer threads.

    //@KafkaListener is used to designate a bean method as a listener for a listener container
    @KafkaListener(topics = "tweets", groupId = "group1", containerFactory = "kafkaListenerContainerFactory")
    public void listen(String data) {
        log.info(data + " received");
    }

    @KafkaListener(topics = "tweets", groupId = "group1", containerFactory = "greetingConcurrentKafkaListenerContainerFactory")
    public void greetingListener(Greeting greeting) {
        log.info("greeting " + greeting);
    }
}

//two types of MessageListenerContainer: KafkaMessageListenerContainer and ConcurrentMessageListenerContainer.
// KafkaMessageListenerContainer receives all messages from all topics or partitions on a single thread.
// ConcurrentMessageListenerContainer delegates to one or more KafkaMessageListenerContainer instances to provide
//multi-threaded consumption.
