import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	val kotlinVersion = "1.6.21"
	kotlin("jvm") version kotlinVersion
	// kotlin("plugin.spring") version kotlinVersion
	kotlin("plugin.serialization") version kotlinVersion

	// id("org.springframework.boot") version "2.7.0"
	// id("io.spring.dependency-management") version "1.0.11.RELEASE"
	id("net.mamoe.mirai-console") version "2.11.1"
}

group = "my.ktbot"
version = "1.0.0"
description = "我的 QQBot"

repositories {
	mavenLocal()
	maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
	mavenCentral()
}

dependencies {
	implementation("org.xerial:sqlite-jdbc:3.36.0.3")
	implementation("org.ktorm:ktorm-core:3.4.1")
	implementation("org.ktorm:ktorm-support-sqlite:3.4.1")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("io.ktor", "ktor-client-serialization-jvm", "1.6.8")
	compileOnly("org.jetbrains:annotations:23.0.0")
	// implementation("net.mamoe:mirai-logging-slf4j-logback:2.10.3")
	// implementation("org.fusesource.jansi:jansi:2.4.0")
	// implementation("org.apache.logging.log4j:log4j-api:2.17.1")
	// implementation("org.apache.logging.log4j:log4j-core:2.17.1")
	// api("net.mamoe:mirai-logging-log4j2:2.9.2")
	// implementation("org.reflections:reflections:0.10.2")

	// implementation("net.mamoe:mirai-core-all:2.11.1")
	// implementation("net.mamoe:mirai-console:2.11.1")
	// implementation("net.mamoe:mirai-console-terminal:2.11.1")
	// implementation("org.springframework.boot:spring-boot-starter") {
	// 	exclude("")
	// }
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

mirai {
	noCoreApi = false
	noTestCore = false
	noConsole = false
	dontConfigureKotlinJvmDefault = false
	publishingEnabled = false
	jvmTarget = JavaVersion.VERSION_17
	configureShadow {
		dependencyFilter.include {
			println("include: ${it.name}")
			it.moduleGroup == "io.ktor"
		}
	}
}

tasks.withType<AbstractCompile> {
	sourceCompatibility = JavaVersion.VERSION_17.toString()
	targetCompatibility = JavaVersion.VERSION_17.toString()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		jvmTarget = JavaVersion.VERSION_17.toString()
		freeCompilerArgs = listOf(
			"-Xjsr305=strict",
			"-opt-in=kotlin.RequiresOptIn",
			// "-Xcontext-receivers",
		)
	}
}

tasks.create("build2Jar") {
	group = "mirai"
	dependsOn += "buildPlugin"
	doLast {
		val pluginPath = "${rootDir}/plugins/"
		File(pluginPath).listFiles()?.forEach {
			if (it.isFile) {
				println("Delete File: ${it.name}")
				if (!delete(it)) {
					println("Cannot Delete File:${it.name}")
				}
			}
		}
		copy {
			from("${buildDir}/mirai/")
			into(pluginPath)
			eachFile { println("Copy File: ${name}") }
		}
	}
}
