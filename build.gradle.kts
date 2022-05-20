import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	val kotlinVersion = "1.6.21"
	kotlin("jvm") version kotlinVersion
	kotlin("plugin.serialization") version kotlinVersion
}

group = "my.ktbot"
version = "1.0.0"
description = "我的 QQBot 项目 - 根项目"

allprojects {
	repositories {
		mavenLocal()
		maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
		mavenCentral()
	}

	tasks.withType(AbstractCompile::class.java) {
		sourceCompatibility = JavaVersion.VERSION_17.toString()
		targetCompatibility = JavaVersion.VERSION_17.toString()
	}

	tasks.withType(KotlinCompile::class.java) {
		kotlinOptions {
			jvmTarget = JavaVersion.VERSION_17.toString()
			freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"// + "-Xcontext-receivers"
		}
	}
}

subprojects {
	apply {
		plugin("kotlin")
	}

	group = "my.ktbot"
	version = "1.0.0"
}
