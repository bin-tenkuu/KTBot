import org.gradle.api.JavaVersion.VERSION_17

plugins {
	val kotlinVersion = "1.7.10"
	kotlin("jvm") version kotlinVersion apply false
}

buildscript {
}

allprojects {
	group = "my.ktbot"
	version = "1.0.0"
}

description = "我的 QQBot"

subprojects {
	repositories {
		mavenLocal()
		mavenCentral()
		maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
		maven("https://maven.google.com")
	}
	tasks {
		withType<JavaCompile> {
			options.encoding = "UTF-8"
			sourceCompatibility = VERSION_17.toString()
			targetCompatibility = VERSION_17.toString()
		}
		withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
			kotlinOptions {
				verbose = true
				jvmTarget = VERSION_17.toString()
				// allWarningsAsErrors = true
				freeCompilerArgs = freeCompilerArgs + mutableListOf(
					// "-Xexplicit-api=strict",
					"-Xjsr305=strict",
					"-opt-in=kotlin.RequiresOptIn",
					"-Xcontext-receivers",
				)
			}
		}
		withType<Test> {
			maxParallelForks = Runtime.getRuntime().availableProcessors()
		}
	}

}
