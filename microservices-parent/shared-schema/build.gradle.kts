plugins {
    id("java-library")
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
}

group = "ca.gbc.comp3095"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven")
    }
}

dependencies {
    implementation("org.apache.avro:avro:1.12.0")
}

avro {
    isCreateSetters = true
    fieldVisibility = "PRIVATE"
    isEnableDecimalLogicalType = true
}

sourceSets {
    main {
        java.srcDir(layout.buildDirectory.dir("generated-main-avro-java"))
    }
}

tasks.named<com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask>("generateAvroJava") {
    source(fileTree("src/main/avro"))
    setOutputDir(layout.buildDirectory.dir("generated-main-avro-java").get().asFile)
}
