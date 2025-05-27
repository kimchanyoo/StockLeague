package com.stockleague.backend.kafka.producer;

import com.stockleague.backend.notification.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationEvent> notificationKafkaTemplate;

    private static final String TOPIC = "user-notification";

    public void send(NotificationEvent notificationEvent) {
        notificationKafkaTemplate.send(TOPIC, notificationEvent);
    }
}
