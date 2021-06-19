plugins {
  kotlin("jvm") version "1.4.10"
}

group = "ml.ffactory"
version = "1.0-SNAPSHOT"

repositories {
  jcenter()
  mavenCentral()
}

val ktorVersion: String by project
val logbackVersion: String by project

dependencies {
  // Ktor
  implementation("io.ktor:ktor-server-netty:$ktorVersion")
  implementation("io.ktor:ktor-websockets:$ktorVersion")
  implementation("ch.qos.logback:logback-classic:$logbackVersion") // Logging
  // Serialization
  implementation("io.ktor:ktor-serialization:$ktorVersion")
  implementation("io.ktor:ktor-jackson:$ktorVersion") // Json serialization
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.3") // Serialize complex built-in datatypes

  implementation("io.github.cdimascio:dotenv-kotlin:6.2.2") // Read .env file
}