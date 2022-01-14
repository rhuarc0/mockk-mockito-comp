import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.6.0"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.6.0"
	kotlin("plugin.spring") version "1.6.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}


java.sourceSets["main"].java {
	srcDir("src/main/kotlin")
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	testImplementation("io.mockk:mockk:1.12.0")
	testImplementation("org.assertj:assertj-core:3.21.0")
	testImplementation("org.mockito:mockito-core:4.1.0")
	testImplementation("org.mockito:mockito-junit-jupiter:4.1.0")
	testImplementation("org.mockito:mockito-inline:4.1.0")
	testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
	testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")


}

java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
