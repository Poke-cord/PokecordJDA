import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

group = "xyz.pokecord"
version = "2.1.1"

val logbackVersion = "1.2.10"
val discord4jVersion = "3.2.6"
val discord4jConnectVersion = "db9ce67f5e"

plugins {
  id("com.github.johnrengelman.shadow") version "8.1.1"
  java
  kotlin("jvm") version "1.9.10"
}

repositories {
  mavenCentral()
  maven("https://jitpack.io")
}

dependencies {
  implementation(kotlin("stdlib"))

  implementation("ch.qos.logback:logback-classic:$logbackVersion")

  implementation("com.discord4j:discord4j-rest:$discord4jVersion")
  implementation("com.github.Discord4J:connect:$discord4jConnectVersion")
}

tasks {
  named<ShadowJar>("shadowJar") {
    archiveBaseName.set("pokecord-discord4j-connect-rsocket-servers")
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
