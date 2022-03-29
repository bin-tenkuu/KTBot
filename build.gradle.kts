plugins {
	val kotlinVersion = "1.6.10"
	kotlin("jvm") version kotlinVersion
	kotlin("plugin.serialization") version kotlinVersion

	id("net.mamoe.mirai-console") version "2.10.1"
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
	implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.0")
	implementation("io.ktor:ktor-client-serialization-jvm:1.6.8")
	implementation("io.ktor:ktor-client-json:1.6.8")
	compileOnly("org.jetbrains:annotations:23.0.0")
	// implementation("org.reflections:reflections:0.10.2")
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
		dependencyFilter.include {
			println(it.name)
			it.moduleGroup == "io.ktor" && it.moduleName in arrayOf(
				"ktor-client-serialization-jvm",
				"ktor-client-json"
			)
		}
	}
}
kotlin {
	sourceSets {
		all {
			languageSettings.optIn("kotlin.RequiresOptIn")
		}
	}
}
tasks.create("build2Jar") {
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