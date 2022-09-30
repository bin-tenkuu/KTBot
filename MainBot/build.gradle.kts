import org.gradle.api.JavaVersion.VERSION_17

plugins {
	kotlin("jvm")
	kotlin("plugin.serialization") version "1.7.10"

	id("net.mamoe.mirai-console") version "2.12.1"
	id("com.google.devtools.ksp") version "1.7.10-1.0.6"
}

dependencies {
	// kotlin
	implementation(kotlin("stdlib"))
	implementation(kotlin("reflect"))
	compileOnly("org.jetbrains:annotations:23.0.0")
	// kotlin script
	// none
	// sqlite
	implementation("org.xerial:sqlite-jdbc:3.39.3.0")
	implementation("org.ktorm:ktorm-core:3.5.0")
	implementation("org.ktorm:ktorm-support-sqlite:3.5.0")
	// ktorm-ksp
	implementation("org.ktorm:ktorm-ksp-api:1.0.0-RC2")
	ksp("org.ktorm:ktorm-ksp-compiler:1.0.0-RC2")
	// ktor-client
	val ktorVersion = "2.1.1"
	implementation("io.ktor:ktor-http:$ktorVersion")
	implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
	implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
	implementation("io.ktor:ktor-serialization-kotlinx:$ktorVersion")
	implementation("io.ktor:ktor-client-logging:$ktorVersion")
	// ktor-server
	implementation("io.ktor:ktor-server:$ktorVersion")
	implementation("io.ktor:ktor-server-core:$ktorVersion")
	implementation("io.ktor:ktor-server-netty:$ktorVersion")
	implementation("io.ktor:ktor-server-resources:$ktorVersion")
	implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
	implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
	implementation("io.ktor:ktor-server-compression:$ktorVersion")
	implementation("io.ktor:ktor-server-data-conversion:$ktorVersion")
	// mirai插件注解
	api("net.mamoe:mirai-console-compiler-annotations-jvm:2.12.2")
	// implementation("net.mamoe:mirai-core-utils:2.12.1")
	// 系统状况读取
	implementation("com.github.oshi:oshi-core-java11:6.2.2")
	// 分词器
	implementation("com.huaban:jieba-analysis:1.0.2")
	// hutool全家桶:https://hutool.cn/docs/#/
	implementation("cn.hutool:hutool-core:5.8.7")
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
	// jvmToolchain(17) // 1.7.20
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
	withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
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
