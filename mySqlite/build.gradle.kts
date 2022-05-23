plugins {
	kotlin("jvm")
	kotlin("plugin.serialization")
}

description = "自定 sqlite"

dependencies {
	implementation("org.ktorm:ktorm-core:3.4.1")
	implementation("org.ktorm:ktorm-support-sqlite:3.4.1")
}
