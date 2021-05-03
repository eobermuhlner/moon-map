plugins {
    java
    application
    kotlin("jvm") version "1.4.32"
    id("org.openjfx.javafxplugin") version "0.0.9"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

javafx {
    version = "12"
    modules("javafx.controls")
}

application {
    mainClass.set("ch.obermuhlner.moonmap.MoonMap")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}