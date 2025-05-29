import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.5"
	id("io.spring.dependency-management") version "1.1.4"
	kotlin("jvm") version "1.9.23"
	kotlin("plugin.spring") version "1.9.23"
	kotlin("plugin.jpa") version "1.9.23"
}

group = "com.persons.finder"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	// Database
	implementation("org.postgresql:postgresql")
	implementation("com.h2database:h2") // For testing

	// spatial
	implementation("org.hibernate.orm:hibernate-spatial")

	implementation("org.n52.jackson:jackson-datatype-jts:1.2.10")

	// api Docs
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

	// for validation: missing locations or malformed input etc.
	implementation ("org.springframework.boot:spring-boot-starter-validation")

	// testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17" // Ensure JVM target is 17
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}