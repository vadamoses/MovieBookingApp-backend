package com.moviebookingapp.kafka.consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumer.class);
	private static final Marker MARKER = MarkerFactory.getMarker(LOGGER.getName());

    @KafkaListener(topics = "${kafka.topic-1}", groupId = "${spring.kafka.consumer.group-id}")
    public void topic1(String message) {
        LOGGER.info(MARKER, "Received message from number_of_tickets_booked_for_movie_topic: {}", message);
    }
    
    @KafkaListener(topics = "${kafka.topic-2}", groupId = "${spring.kafka.consumer.group-id}")
    public void topic2(String message) {
        LOGGER.info(MARKER, "Received message from ticket_status_for_movie_topic: {}", message);
    }
}