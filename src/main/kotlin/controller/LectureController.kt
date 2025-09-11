package org.kafka_lecture.controller

import org.kafka_lecture.basic.OrderEventPublisher
import org.kafka_lecture.model.CreateOrderRequest
import org.kafka_lecture.model.OrderEvent
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/lecture")
class LectureController(
    private val orderEventPublisher: OrderEventPublisher,
) {

    @PostMapping
    fun createOrder(@RequestBody request : CreateOrderRequest): ResponseEntity<String> {
        val orderEvent = OrderEvent(
            orderId = UUID.randomUUID().toString(),
            customerId = request.customerId,
            quantity = request.quantity,
            price = request.price,
        )
        orderEventPublisher.publishOrderEvent(orderEvent)
        return ResponseEntity.ok("Order created")
    }
}