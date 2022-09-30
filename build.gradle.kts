import org.gradle.api.JavaVersion.VERSION_17

plugins {
	kotlin("jvm") version "1.7.20" apply false
	kotlin("plugin.serialization") version "1.7.20" apply false
	id("com.google.devtools.ksp") version "1.7.20-1.0.6" apply false
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
					// "-Xuse-k2"
				)
			}
		}
		withType<Test> {
			maxParallelForks = Runtime.getRuntime().availableProcessors()
		}
	}

}
