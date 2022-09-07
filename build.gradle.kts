import org.gradle.api.JavaVersion.VERSION_17

plugins {
	val kotlinVersion = "1.7.10"
	kotlin("jvm").version(kotlinVersion)
	kotlin("plugin.serialization").version(kotlinVersion)

	id("net.mamoe.mirai-console").version("2.12.1")
	id("com.google.devtools.ksp").version("1.7.10-1.0.6")
}

group = "my.ktbot"
version = "1.0.0"
description = "我的 QQBot"

repositories {
	mavenLocal()
	maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
	mavenCentral()
	maven("https://maven.google.com")
}

dependencies {
	// kotlin
	implementation(kotlin("stdlib"))
	implementation(kotlin("reflect"))
	compileOnly("org.jetbrains:annotations:23.0.0")
	// sqlite
	implementation("org.xerial:sqlite-jdbc:3.39.2.1")
	implementation("org.ktorm:ktorm-core:3.5.0")
	implementation("org.ktorm:ktorm-support-sqlite:3.5.0")
	// // sql
	// val exposedVersion = "0.39.2"
	// implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
	// implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
	// implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
	// implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
	// ktorm-ksp
	implementation("org.ktorm:ktorm-ksp-api:1.0.0-RC2")
	ksp("org.ktorm:ktorm-ksp-compiler:1.0.0-RC2")
	// ktor
	val ktorVersion = "2.1.0"
	implementation("io.ktor:ktor-http:$ktorVersion")
	implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
	implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
	implementation("io.ktor:ktor-serialization-kotlinx:$ktorVersion")
	implementation("io.ktor:ktor-client-logging:$ktorVersion")
	// mirai插件注解
	api("net.mamoe:mirai-console-compiler-annotations-jvm:2.12.2")
	// implementation("net.mamoe:mirai-core-utils:2.12.1")
	// 系统状况读取
	implementation("com.github.oshi:oshi-core-java11:6.2.2")
	// 分词器
	implementation("com.huaban:jieba-analysis:1.0.2")
	// hutool全家桶:https://hutool.cn/docs/#/
	implementation("cn.hutool:hutool-core:5.8.6")
	// 词云生成器
	testImplementation("com.kennycason:kumo-core:1.28")
	testImplementation("com.kennycason:kumo-tokenizers:1.28")
	// skiko
	val skikoVersion = "0.7.20"
	api("org.jetbrains.skiko:skiko-awt:$skikoVersion")
	//implementation("org.jetbrains.skiko:skiko-awt-runtime-windows-x64:$skikoVersion")
}
// ksp 加入编译
kotlin {
	sourceSets {
		main { kotlin.srcDir("build/generated/ksp/main/kotlin") }
		test { kotlin.srcDir("build/generated/ksp/test/kotlin") }
	}
}

mirai {
	noCoreApi = false
	noTestCore = false
	noConsole = false
	dontConfigureKotlinJvmDefault = false
	publishingEnabled = false
	jvmTarget = VERSION_17
	configureShadow {
		dependencyFilter.include {
			println("include: ${it.name}")
			it.moduleGroup == "io.ktor"
		}
	}
}

java {
	sourceCompatibility = VERSION_17
	targetCompatibility = VERSION_17
}

tasks {
	compileJava {
		sourceCompatibility = VERSION_17.toString()
		targetCompatibility = VERSION_17.toString()
	}
	compileKotlin {
		kotlinOptions {
			verbose = true
			jvmTarget = VERSION_17.toString()
			freeCompilerArgs = listOf(
				"-Xjsr305=strict",
				"-opt-in=kotlin.RequiresOptIn",
				"-opt-in=net.mamoe.mirai.utils.MiraiExperimentalApi",
				"-Xcontext-receivers",
			)
		}
	}
	create("build2Jar") {
		group = "mirai"
		dependsOn += "buildPlugin"
		doLast {
			val pluginPath = "${rootDir}/plugins/"
			(File(pluginPath).listFiles() ?: emptyArray()).filter f@{
				val name = it.name
				if (it.isFile && name.startsWith(rootProject.name)) {
					if (name.endsWith(".bak")) {
						println("Delete backup File: $name")
						if (!it.delete()) error("Cannot Delete File: $pluginPath$name")
						return@f false
					}
					return@f true
				}
				return@f false
			}.forEach {
				val name = it.name
				println("Backup File: $name to $name.bak")
				it.renameTo(File("$pluginPath$name.bak"))
			}
			(File("${buildDir}/mirai/").listFiles() ?: emptyArray()).forEach {
				val name = it.name
				println("Copy File: $name to plugins/$name ")
				it.copyTo(File(pluginPath + name))
			}
		}
	}
}
