import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.2.41"
    id("io.spring.dependency-management") version "1.0.5.RELEASE"
    application
    id ("com.github.johnrengelman.shadow") version "2.0.4"
}

application {
    mainClassName = "demo.ApplicationKt"
}

dependencies {
    val fuVersion = "1.0.0.BUILD-SNAPSHOT" 
    implementation("org.springframework.fu:spring-fu:$fuVersion")
    implementation("org.springframework.fu:spring-fu-logging:$fuVersion")
    implementation("org.springframework.fu:spring-fu-jackson:$fuVersion")
    implementation("org.springframework.fu:spring-fu-mongodb:$fuVersion")
    implementation("org.springframework.fu:spring-fu-mustache:$fuVersion")
    implementation("org.springframework.fu:spring-fu-webflux-netty:$fuVersion")

    testImplementation("org.springframework.fu:spring-fu-test:$fuVersion")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.spring.io/libs-release")
    maven("https://repo.spring.io/snapshot")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}
tasks.withType<Test> {
    useJUnitPlatform()
}
dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:2.0.2.RELEASE") {
            bomProperty("spring.version", "5.1.0.BUILD-SNAPSHOT")
            bomProperty("reactor-bom.version", "Californium-BUILD-SNAPSHOT")
        }
    }
    dependencies {
        val coroutinesVersion = "0.22.5"
        dependency("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
        dependency("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
    }
}
