import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm")
	kotlin("plugin.serialization")

	id("net.mamoe.mirai-console") version "2.14.0"
	id("com.google.devtools.ksp")
}

dependencies {
	// kotlin
	val kotlinVersion = "1.8.0"
	implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
	implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
	implementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.5.0")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
	compileOnly("org.jetbrains:annotations:24.0.1")
	// clikt
	testImplementation("com.github.ajalt.clikt:clikt:3.5.2")
	// sqlite
	implementation("org.xerial:sqlite-jdbc:3.40.1.0")
	implementation("org.ktorm:ktorm-core:3.6.0")
	implementation("org.ktorm:ktorm-support-sqlite:3.6.0")
	implementation("org.ktorm:ktorm-ksp-api:1.0.0-RC3")
	ksp("org.ktorm:ktorm-ksp-compiler:1.0.0-RC3")
	// ktor-client
	val ktorVersion = "2.2.4"
	implementation("io.ktor:ktor-client-auth-jvm:$ktorVersion")
	implementation("io.ktor:ktor-http-jvm:$ktorVersion")
	implementation("io.ktor:ktor-client-okhttp-jvm:$ktorVersion")
	implementation("io.ktor:ktor-client-websockets-jvm:$ktorVersion")
	implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
	implementation("io.ktor:ktor-serialization-kotlinx:$ktorVersion")
	implementation("io.ktor:ktor-client-encoding-jvm:$ktorVersion")
	implementation("io.ktor:ktor-client-logging-jvm:$ktorVersion")
	// ktor-server
	implementation("io.ktor:ktor-server-jvm:$ktorVersion")
	implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
	implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
	implementation("io.ktor:ktor-server-resources:$ktorVersion")
	implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
	implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
	implementation("io.ktor:ktor-server-compression:$ktorVersion")
	implementation("io.ktor:ktor-server-data-conversion:$ktorVersion")
	implementation("io.ktor:ktor-server-websockets:$ktorVersion")
	// mirai插件注解
	api("net.mamoe:mirai-console-compiler-annotations-jvm:2.14.0")
	// implementation("net.mamoe:mirai-core-utils:2.12.1")
	// 系统状况读取
	implementation("com.github.oshi:oshi-core-java11:6.4.0")
	// 分词器
	implementation("com.huaban:jieba-analysis:1.0.2")
	// hutool全家桶: https://hutool.cn/docs/#/
	implementation("cn.hutool:hutool-core:5.8.15")
	// openAI: https://chat.openai.com/chat
	testImplementation("com.theokanning.openai-gpt3-java:client:0.11.1")
	// html 库
	implementation("org.jsoup:jsoup:1.15.4")
}
// ksp 加入编译
kotlin {
	jvmToolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
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

tasks {
	withType<KotlinCompile> {
		kotlinOptions.freeCompilerArgs += "-opt-in=net.mamoe.mirai.utils.MiraiExperimentalApi"
	}
	create("build2Jar") {
		group = "mirai"
		dependsOn += "buildPlugin"
		doLast {
			fun File.copyAndBakeup(name: String) {
				val bakFile = File("$name.bak")
				if (bakFile.isFile) {
					println("Delete backup File: $name.bak")
					if (!bakFile.delete()) error("Cannot Delete File: $name")
				}
				val file = File(name)
				if (file.isFile) {
					println("Backup File: $name to $name.bak")
					file.renameTo(bakFile)
				}
				println("Copy File: $absolutePath to $name")
				copyTo(file)
			}

			val pluginPath = "${rootDir}/plugins/"
			for (it in File("${buildDir}/mirai/").listFiles() ?: emptyArray()) {
				it.copyAndBakeup(pluginPath + it.name)
			}
		}
	}
}
