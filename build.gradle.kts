plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.spring") version "2.1.10"
}

group = "org.kafka_lecture"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
        isAllowInsecureProtocol = true
    }
}

dependencies {
    // 웹 애플리케이션 개발을 위한 스타터 패키지
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-web
    implementation("org.springframework.boot:spring-boot-starter-web")
    // JPA == ORM
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-jpa    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Kafka를 활용하기 위한 라이브러리
    // https://mvnrepository.com/artifact/org.springframework.kafka/spring-kafka
    implementation("org.springframework.kafka:spring-kafka")
    // Kafka의 메세지 및 스트림 처리
    // https://mvnrepository.com/artifact/org.apache.kafka/kafka-streams
    implementation("org.apache.kafka:kafka-streams")

    // 데이터 직렬화를 위한 Avro 라이브러리
    // https://mvnrepository.com/artifact/org.apache.avro/avro
    implementation("org.apache.avro:avro:1.11.3")
    // Avro 데이터를 다루기 위한 역/직렬화
    // https://mvnrepository.com/artifact/io.confluent/kafka-avro-serializer
    implementation("io.confluent:kafka-avro-serializer:7.5.0")
    // SerDe 지원을 위한 라이브러리
    // https://mvnrepository.com/artifact/io.confluent/kafka-streams-avro-serde
    implementation("io.confluent:kafka-streams-avro-serde:7.5.0")
    // 스키마를 저장하는 저장소
    // https://mvnrepository.com/artifact/io.confluent/kafka-schema-registry-client
    implementation("io.confluent:kafka-schema-registry-client:7.5.0")

    // 기본적인 역/직렬화
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Spring, Java 언어를 Kotlin으로 맵핑해주는 라이브러리
    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-reflect
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib-jdk8
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.2.10")

    // 애플리케이션 모니터링 라이브러리
    // https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // https://mvnrepository.com/artifact/com.h2database/h2
    runtimeOnly("com.h2database:h2")
    // https://mvnrepository.com/artifact/org.postgresql/postgresql
    runtimeOnly("org.postgresql:postgresql")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}