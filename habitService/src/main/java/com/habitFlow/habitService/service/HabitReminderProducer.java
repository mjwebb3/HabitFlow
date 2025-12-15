package com.habitFlow.habitService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka Producer component responsible for sending messages to various Kafka topics.
 * This generic producer is used by other services (like {@link HabitReminderScheduler}
 * and {@link HabitService}) to dispatch events, such as reminders and general notifications.
 */
@Component
@RequiredArgsConstructor
public class HabitReminderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Sends a generic event object to a specified Kafka topic.
     * The key is left null, allowing Kafka to determine partitioning based on other factors.
     *
     * @param topic The name of the Kafka topic to send the event to.
     * @param event The event object (e.g., {@link com.habitFlow.Kafka.HabitReminderEvent},
     * {@link com.habitFlow.Kafka.NotificationEvent}) to be serialized and sent.
     */
    public void send(String topic, Object event) {
        kafkaTemplate.send(topic, event);
        System.out.printf("[KafkaProducer] 📤 Sent event to topic '%s': %s%n", topic, event);
    }
}