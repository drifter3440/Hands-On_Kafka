package org.kafka_lecture.config



import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StringDeserializer
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.kafka_lecture.model.OrderEvent
import org.springframework.beans.factory.annotation.Configurable
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configurable
class KafkaConfig {
    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    companion object {
        const val SCHEMA_REGISTRY_URL: String = "http://localhost:8081"
    }

    // listener

    @Bean
    fun orderEventConsumerFactory(): ConsumerFactory<String, OrderEvent> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            // 역직렬화 실패시 애플리케이션 중단을 방지
            ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS to StringDeserializer::class.java,
            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS to JsonDeserializer::class.java,
            // 강타입 orderEvent
            JsonDeserializer.VALUE_DEFAULT_TYPE to OrderEvent::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "*",
            JsonDeserializer.USE_TYPE_INFO_HEADERS to false
        )
        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun orderEventKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, OrderEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, OrderEvent>()
        factory.consumerFactory = orderEventConsumerFactory()
        return factory
    }

    @Bean
    fun genericConsumerFactory(): ConsumerFactory<String, Any> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS to StringDeserializer::class.java,
            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS to JsonDeserializer::class.java,
            JsonDeserializer.TRUSTED_PACKAGES to "*",
            JsonDeserializer.USE_TYPE_INFO_HEADERS to true
        )
        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun genericKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
        factory.consumerFactory = genericConsumerFactory()
        return factory
    }

    @Bean
    fun cdcConsumerFactory(): ConsumerFactory<String, String> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "real-order-cdc-processor",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to true,
            ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG to 1000
        )
        return DefaultKafkaConsumerFactory(props)
    }
    @Bean
    fun cdcKafkaListenerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = cdcConsumerFactory()
        return factory
    }

    @Bean
    fun avroConsumerFactory(): ConsumerFactory<String, GenericRecord> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaAvroDeserializer::class.java,
            AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to SCHEMA_REGISTRY_URL,
            KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to false,
            AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS to true
        )
        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun avroKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, GenericRecord> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, GenericRecord>()
        factory.consumerFactory = avroConsumerFactory()
        return factory
    }

    // producer
    @Bean
    fun orderEventProducerFactory(): ProducerFactory<String, OrderEvent> {
        val props = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            JsonSerializer.ADD_TYPE_INFO_HEADERS to true
        )
        return DefaultKafkaProducerFactory(props)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, OrderEvent> {
        return KafkaTemplate(orderEventProducerFactory())
    }

    @Bean
    fun avroProducerFactory(): ProducerFactory<String, GenericRecord> {
        val props = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java,
            AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to SCHEMA_REGISTRY_URL,
            AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS to true,
            ProducerConfig.ACKS_CONFIG to "1", // "0", "1", "all"
            ProducerConfig.RETRIES_CONFIG to 3,
            ProducerConfig.BATCH_SIZE_CONFIG to 16384,
            ProducerConfig.LINGER_MS_CONFIG to 10,
            ProducerConfig.COMPRESSION_TYPE_CONFIG to "snappy" // none, lz4, gzip, zstd
        )
        return DefaultKafkaProducerFactory(props)
    }

    @Bean
    fun avroKafkaTemplate(): KafkaTemplate<String, OrderEvent> {
        return KafkaTemplate(orderEventProducerFactory())
    }
}