import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

group = "xyz.pokecord"
version = "2.0.0"

val graalJsVersion = "21.0.0.2"
val jdaKtxVersion = "985db81"
val jdaUtilitiesVersion = "3.0.5"
val jdaVersion = "4.2.1_269"
val kmongoVersion = "4.2.4"
val kotlinCoroutinesVersion = "1.4.3"
val kotlinxSerializationJsonVersion = "1.1.0"
val ktorVersion = "1.5.4"
val logbackVersion = "1.2.3"
val redissonVersion = "3.15.5"
val sentryLogbackVersion = "5.0.1"
val snakeYamlVersion = "1.28"
val trove4jVersion = "3.0.3"

plugins {
  id("com.github.johnrengelman.shadow") version "6.1.0"
  java
  kotlin("jvm") version "1.4.31"
  kotlin("plugin.serialization") version "1.4.31"
}

repositories {
  mavenCentral()
  jcenter()
  maven("https://jitpack.io/")
  maven("https://m2.dv8tion.net/releases")
  maven("https://zihadmahiuddin.github.io/remotesharding-kt")
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation(kotlin("reflect"))

  implementation("ch.qos.logback:logback-classic:$logbackVersion")
  implementation("io.sentry:sentry-logback:$sentryLogbackVersion")

  implementation("com.github.minndevelopment:jda-ktx:${jdaKtxVersion}")

  implementation("dev.zihad:remotesharding:1.0.1")

  implementation("net.dv8tion:JDA:$jdaVersion") {
//  implementation("com.github.dv8fromtheworld:jda:development") { // JitPack
    exclude(module = "opus-java")
  }

  implementation("net.sf.trove4j:trove4j:$trove4jVersion")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
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
