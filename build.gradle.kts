import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
	val kotlinVersion = "1.6.10"
	kotlin("jvm") version kotlinVersion
	kotlin("plugin.serialization") version kotlinVersion

	id("net.mamoe.mirai-console") version "2.9.2"
}

group = "my.ktbot.plugin.binbot"
version = "0.1"

repositories {
	mavenLocal()
	maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
	mavenCentral()
}

dependencies {
	implementation("org.xerial:sqlite-jdbc:3.36.0.3")
	implementation("org.ktorm:ktorm-core:3.4.1")
	implementation("org.ktorm:ktorm-support-sqlite:3.4.1")
	implementation("io.ktor:ktor-client-serialization-jvm:1.6.7")
	implementation("io.ktor:ktor-client-json:1.6.7")
	implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.0")
	implementation("org.jetbrains:annotations:23.0.0")
//	implementation("org.slf4j:slf4j-api:1.7.35")
//	api("net.mamoe:mirai-logging-log4j2:2.9.2")
//	implementation("org.slf4j:slf4j-simple:1.7.35")
}
mirai {
	noCoreApi = false
	noTestCore = false
	noConsole = false
	dontConfigureKotlinJvmDefault = false
	publishingEnabled = false
	jvmTarget = JavaVersion.VERSION_17
	configureShadow {
		dependencies {
			include(dependency("io.ktor:ktor-client-serialization-jvm:1.6.7"))
			include(dependency("io.ktor:ktor-client-json:1.6.7"))
		}
	}
}
tasks {
	withType(KotlinJvmCompile::class.java) {
		kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
	}
	create("build2Jar") {
		val pluginPath = "${rootDir}/plugins/"
		doFirst {
			File(pluginPath).listFiles()?.forEach {
				if (it.isFile) {
					println("Delete File:${it.name}")
					it.delete()
				}
			}
		}
		group = "mirai"
		dependsOn += "buildPlugin"
		doLast {
			copy {
				println("Copy File:${pluginPath}")
				from("${buildDir}/mirai")
				into(pluginPath)
			}
		}
	}
}