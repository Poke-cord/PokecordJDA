import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

group = "xyz.pokecord"
version = "2.1.1"

val discordWebhooksVersion = "0.7.4"
val graalJsVersion = "21.2.0"
val jdaKtxVersion = "985db81"
val jdaUtilitiesVersion = "3.0.5"
val jdaVersion = "4.4.0_352"
val kmongoVersion = "4.4.0"
val kotestVersion = "5.0.2"
val kotlinxCoroutinesVersion = "1.6.0"
val kotlinxSerializationJsonVersion = "1.3.2"
val ktorVersion = "1.6.7"
val logbackVersion = "1.2.10"
val prometheusVersion = "0.14.1"
val redissonVersion = "3.16.7"
val remoteShardingKtVersion = "adc8d9220e"
val sentryLogbackVersion = "5.5.0"
val snakeYamlVersion = "1.29"
val trove4jVersion = "3.0.3"

plugins {
  id("com.github.johnrengelman.shadow") version "8.1.1"
  java
  kotlin("jvm") version "1.9.10"
  kotlin("plugin.serialization") version "1.9.10"
}

repositories {
  mavenCentral()
  maven("https://jitpack.io/")
  maven("https://m2.dv8tion.net/releases")
}

dependencies {
  implementation(kotlin("scripting-jsr223"))
  implementation(kotlin("stdlib"))
  implementation(kotlin("reflect"))

  implementation("ch.qos.logback:logback-classic:$logbackVersion")
  implementation("io.sentry:sentry-logback:$sentryLogbackVersion")

  implementation("com.github.minndevelopment:jda-ktx:${jdaKtxVersion}")

  implementation("com.github.zihadmahiuddin:remotesharding-kt:$remoteShardingKtVersion")

  implementation("net.dv8tion:JDA:$jdaVersion") {
    exclude(module = "opus-java")
  }

  implementation("club.minnced:discord-webhooks:$discordWebhooksVersion")

  implementation("net.sf.trove4j:trove4j:$trove4jVersion")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJsonVersion")

  implementation("org.redisson:redisson:$redissonVersion")

  implementation("org.graalvm.js:js:$graalJsVersion")
  implementation("org.graalvm.js:js-scriptengine:$graalJsVersion")

  implementation("org.litote.kmongo:kmongo:$kmongoVersion")
  implementation("org.litote.kmongo:kmongo-id-serialization:$kmongoVersion")
  implementation("org.litote.kmongo:kmongo-coroutine-serialization:$kmongoVersion")

  implementation("org.yaml:snakeyaml:$snakeYamlVersion")

  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-java:$ktorVersion")
  implementation("io.ktor:ktor-client-serialization:$ktorVersion")

  implementation("io.ktor:ktor-server-core:$ktorVersion")
  implementation("io.ktor:ktor-server-jetty:$ktorVersion")
  implementation("io.ktor:ktor-auth:$ktorVersion")

  implementation("io.ktor:ktor-serialization:$ktorVersion")

  implementation("io.prometheus:simpleclient:$prometheusVersion")
  implementation("io.prometheus:simpleclient_pushgateway:$prometheusVersion")

  testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
}

tasks {
  named<ShadowJar>("shadowJar") {
    archiveBaseName.set("pokecord")
    mergeServiceFiles()
    isZip64 = true
  }

  withType<Test> {
    useJUnitPlatform()
  }

  jar {
    manifest {
      attributes(mapOf("Main-Class" to "xyz.pokecord.App"))
    }
  }

  build {
    dependsOn(shadowJar)
  }

  compileKotlin {
    kotlinOptions {
      jvmTarget = JavaVersion.VERSION_20.toString()
    }
  }

  compileTestKotlin {
    kotlinOptions {
      jvmTarget = JavaVersion.VERSION_20.toString()
    }
  }
}
