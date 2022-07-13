import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	val kotlinVersion = "1.7.10"
	kotlin("jvm").version(kotlinVersion)
	kotlin("plugin.serialization").version(kotlinVersion)

	id("net.mamoe.mirai-console").version("2.12.0")
	id("com.google.devtools.ksp").version("1.7.0-RC2-1.0.5")
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
	implementation("org.jetbrains.kotlin:kotlin-stdlib:${getKotlinPluginVersion()}")
	implementation("org.jetbrains.kotlin:kotlin-reflect:${getKotlinPluginVersion()}")
	compileOnly("org.jetbrains:annotations:23.0.0")
	// sqlite
	implementation("org.xerial:sqlite-jdbc:3.36.0.3")
	implementation("org.ktorm:ktorm-core:3.5.0")
	implementation("org.ktorm:ktorm-support-sqlite:3.5.0")
	// ktorm-ksp
	implementation("org.ktorm:ktorm-ksp-api:1.0.0-RC2")
	ksp("org.ktorm:ktorm-ksp-compiler:1.0.0-RC2")
	// ktor
	@Suppress("GradlePackageUpdate")
	implementation("io.ktor:ktor-client-serialization-jvm:1.6.8")
	// ??
	api("net.mamoe:mirai-console-compiler-annotations-jvm:2.11.1")
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
					error("Cannot Delete File:${it.name}")
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
