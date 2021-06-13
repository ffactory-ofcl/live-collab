plugins {
  kotlin("jvm") version "1.4.10"
}

group = "ml.ffactory"
version = "1.0-SNAPSHOT"

repositories {
  jcenter()
  mavenCentral()
}

val ktor_version: String by project
val logback_version: String by project

dependencies {
  implementation("io.ktor:ktor-server-netty:$ktor_version")
  implementation("io.ktor:ktor-websockets:$ktor_version")
  implementation("ch.qos.logback:logback-classic:$logback_version")

  implementation("io.ktor:ktor-jackson:$ktor_version")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.3")
}