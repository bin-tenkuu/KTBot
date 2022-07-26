import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	val kotlinVersion = "1.7.10"
	kotlin("jvm").version(kotlinVersion)
	kotlin("plugin.serialization").version(kotlinVersion)

	id("net.mamoe.mirai-console").version("2.12.0")
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
	val ktorVersion = "1.6.8"
	kotlin.run {
		implementation("io.ktor:ktor-client-okhttp:${ktorVersion}")
		implementation("io.ktor:ktor-client-json:${ktorVersion}")
		implementation("io.ktor:ktor-client-serialization:${ktorVersion}")
	}
	// @Suppress("GradlePackageUpdate")
	// implementation("io.ktor:ktor-client-okhttp:${ktorVersion}")
	// @Suppress("GradlePackageUpdate")
	// implementation("io.ktor:ktor-client-json:${ktorVersion}")
	// @Suppress("GradlePackageUpdate")
	// implementation("io.ktor:ktor-client-serialization:${ktorVersion}")
	// val ktorVersion = "2.0.3"
	// implementation("io.ktor:ktor-client-core:${ktorVersion}")
	// implementation("io.ktor:ktor-client-okhttp:${ktorVersion}")
	// implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
	// implementation("io.ktor:ktor-client-json:${ktorVersion}")
	// implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
	// ??
	api("net.mamoe:mirai-console-compiler-annotations-jvm:2.11.1")
	// 系统状况读取
	implementation("com.github.oshi:oshi-core-java11:6.2.1")
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
