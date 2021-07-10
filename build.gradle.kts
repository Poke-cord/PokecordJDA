import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

group = "xyz.pokecord"
version = "2.0.0"

val discordWebhooksVersion = "0.5.7"
val graalJsVersion = "21.0.0.2"
val jdaKtxVersion = "985db81"
val jdaUtilitiesVersion = "3.0.5"
val jdaVersion = "4.3.0_285"
val kmongoVersion = "4.2.4"
val kotlinxCoroutinesVersion = "1.5.0"
val kotlinxSerializationJsonVersion = "1.1.0"
val ktorVersion = "1.6.0"
val logbackVersion = "1.2.3"
val redissonVersion = "3.15.5"
val remoteShardingKtVersion = "68d53da93b"
val sentryLogbackVersion = "5.0.1"
val snakeYamlVersion = "1.28"
val trove4jVersion = "3.0.3"

plugins {
  id("com.github.johnrengelman.shadow") version "6.1.0"
  java
  kotlin("jvm") version "1.5.10"
  kotlin("plugin.serialization") version "1.5.10"
}

repositories {
  mavenCentral()
  jcenter()
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
  implementation("io.ktor:ktor-client-websockets:$ktorVersion")

  implementation("io.ktor:ktor-server-core:$ktorVersion")
  implementation("io.ktor:ktor-server-jetty:$ktorVersion")

  implementation("io.ktor:ktor-serialization:$ktorVersion")
}

tasks {
  named<ShadowJar>("shadowJar") {
    archiveBaseName.set("pokecord")
    mergeServiceFiles()
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
      jvmTarget = JavaVersion.VERSION_11.toString()
    }
  }

  compileTestKotlin {
    kotlinOptions {
      jvmTarget = JavaVersion.VERSION_11.toString()
    }
  }
}
