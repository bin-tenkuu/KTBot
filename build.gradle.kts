import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	val kotlinVersion = "1.8.0"
	kotlin("jvm") version kotlinVersion apply false
	kotlin("plugin.serialization") version kotlinVersion apply false
	id("com.google.devtools.ksp") version "1.8.0-1.0.8" apply false
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
		maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
		mavenCentral()
		maven("https://maven.google.com")
	}
	tasks {
		withType<JavaCompile> {
			options.apply {
				isVerbose = true
				encoding = "UTF-8"
			}
			sourceCompatibility = VERSION_17.toString()
			targetCompatibility = VERSION_17.toString()
		}
		withType<KotlinCompile> {
			kotlinOptions {
				verbose = true
				jvmTarget = VERSION_17.toString()
				// allWarningsAsErrors = true
				freeCompilerArgs = freeCompilerArgs + mutableListOf(
					// "-Xexplicit-api=strict",
					"-Xjsr305=strict",
					"-opt-in=kotlin.RequiresOptIn",
					"-Xcontext-receivers",
					// "-Xuse-k2"
				)
			}
		}
		withType<Test> {
			maxParallelForks = Runtime.getRuntime().availableProcessors()
		}
	}
}
