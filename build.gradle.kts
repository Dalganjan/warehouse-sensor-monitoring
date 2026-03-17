plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "com.example.warehouse"
version = "1.0.0"

repositories {
    mavenCentral()
}

val kotestVersion = "5.9.1"
val coroutinesVersion = "1.9.0"
val slf4jVersion = "2.0.16"
val logbackVersion = "1.5.12"

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
}

application {
    mainClass.set("com.example.warehouse.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}
