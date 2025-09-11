package org.kafka_lecture.basic

import org.kafka_lecture.model.OrderEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class OrderEventConsumer {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["\${kafka.topics.orders}"],
        groupId = "order-processing-group",
        concurrency = "3",
        containerFactory = "orderEventKafkaListenerContainerFactory"
        )
    fun processOrder(
        @Payload orderEvent: OrderEvent,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: String,
        @Header(KafkaHeaders.OFFSET) offset: Long
    ) {

        try {
            processLogic()
            logger.info("Received order event: {}", orderEvent)
        } catch (ex: Exception) {
            logger.error("Error while receiving order event: {}", ex.message, ex)
        }
    }

    private fun processLogic() {
        Thread.sleep(100)
    }
}

@Component
class OrderAnalyticsConsumer {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["\${kafka.topics.orders}"],
        groupId = "order-analytics-group",
        concurrency = "2",
        containerFactory = "orderEventKafkaListenerContainerFactory"
    )
    fun processOrder(
        @Payload orderEvent: OrderEvent,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: String
    ) {
        logger.info("Collecting analytics for order {} form partition {}", orderEvent.orderId, partition)
        try {
            updateCustomerStatistics(orderEvent)
            logger.info("Received order event: {}", orderEvent)
        } catch (ex: Exception) {
            logger.error("Error while receiving order event: {}", ex.message, ex)
        }
    }

    private fun updateCustomerStatistics(orderEvent: OrderEvent) {
        logger.debug("Updated customer statistics for {}", orderEvent.customerId)
    }
}


@Component
class OrderNotificationsConsumer {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["\${kafka.topics.orders}"],
        groupId = "order-notification-group",
        concurrency = "1",
        containerFactory = "orderEventKafkaListenerContainerFactory"
    )
    fun processOrder(
        @Payload orderEvent: OrderEvent,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: String
    ) {
        logger.info("CSending notifications for order {} form partition {}", orderEvent.orderId, partition)
        try {
            if (isHighValueOrder(orderEvent)) {
                sendHighValueOrderSms(orderEvent)
            }
        } catch (ex: Exception) {
            logger.error("Failed to send notifications for order {}: {}", orderEvent.orderId, ex.message)
        }
    }

    private fun sendHighValueOrderSms(orderEvent: OrderEvent) {
        logger.info("SMS sent high-value order {}", orderEvent.orderId)
    }

    private fun isHighValueOrder(orderEvent: OrderEvent): Boolean {
        return orderEvent.price >= BigDecimal("1000")
    }
}