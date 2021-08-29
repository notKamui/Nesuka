plugins {
    application
    kotlin("jvm") version "1.5.30"
}

group = "com.notkamui.nesuka"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.notkamui.nesuka.MainKt")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.5.21")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "com.notkamui.nesuka.MainKt"
    }
    configurations["compileClasspath"].forEach { file ->
        from(zipTree(file.absoluteFile))
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}