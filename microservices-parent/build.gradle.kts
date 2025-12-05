plugins {
	java
	id("org.springframework.boot") version "3.5.6" apply false

	id("io.spring.dependency-management") version "1.1.7" apply false
}

group = "ca.gbc.comp3095"
version = "0.0.1-SNAPSHOT"
description = "microservice-parent"

subprojects{
	apply(plugin = "java")

	repositories{
		mavenCentral()
	}
	java {
		toolchain{
			languageVersion.set(JavaLanguageVersion.of(17))
		}
	}
	tasks.register("prepareKotlinBuildScriptModel") { }
}
