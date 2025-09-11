package org.kafka_lecture.basic

import org.kafka_lecture.model.OrderEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class OrderEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, OrderEvent>,
    @Value("\${kafka.topics.orders}") private val ordersTopic: String,
) {
    private val logger = LoggerFactory.getLogger(OrderEventPublisher::class.java)
    fun publishOrderEvent(orderEvent: OrderEvent) {
        try {
            // hash(key) % 토픽의 파티션 수
            kafkaTemplate.send(ordersTopic, orderEvent.orderId,orderEvent)
                .whenComplete { _, ex ->
                    if (ex != null) {
                        logger.error("Error when publishing order event", ex)
                    }
                }
        } catch (ex : Exception) {
            logger.error("Error publishing order event", ex)
        }
    }
}