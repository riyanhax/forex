import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    groovy
    id("org.springframework.boot") version "2.0.4.RELEASE"
}

apply(plugin = "io.spring.dependency-management")

repositories {
    mavenCentral()
}

val groovyVersion = "2.4.8"
val guavaVersion = "24.0-jre"
val oandaVersion = "3.0.21"
val slf4jVersion = "1.7.25"
val spockVersion = "1.1-groovy-2.4-rc-3"
val springVersion = "4.3.3.RELEASE"

dependencies {
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.google.guava:guava:$guavaVersion")
    implementation("com.oanda.v20:v20:$oandaVersion")
    implementation("org.codehaus.groovy:groovy-all:$groovyVersion")
    implementation("org.flywaydb:flyway-core:5.1.4")

    runtime("mysql:mysql-connector-java:5.1.47")

    testImplementation("org.springframework:spring-test")
    testImplementation("org.springframework.boot:spring-boot-test")
    testImplementation("org.spockframework:spock-spring:$spockVersion") {
        exclude(group = "org.codehaus.groovy")
    }
    testImplementation("com.h2database:h2:1.4.197")
}

tasks.getByName<BootJar>("bootJar") {
    launchScript()
}

tasks.getByName<Jar>("jar") {
    version = ""
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.withType<Test> {
    maxHeapSize = "4096m"
}

val stopService = tasks.register<Exec>("stopService") {
    commandLine("sudo", "service", "forex", "stop")
}

val copyJar = tasks.register<DefaultTask>("copyJar") {
    dependsOn(tasks.getByName("assemble"), stopService)

    val archive = tasks.getByName<Jar>("jar").archivePath

    doLast {
        exec {
            val targetDir = file(project.property("targetDir")!!)

            logger.lifecycle("Releasing $archive to $targetDir")

            commandLine("sudo", "cp", archive, targetDir)
        }
    }
}

val startService = tasks.register<Exec>("startService") {
    dependsOn(copyJar)
    commandLine("sudo", "service", "forex", "start")
}

val release = tasks.register<DefaultTask>("release") {
    dependsOn(copyJar, startService)
}