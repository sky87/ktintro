import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
}

group = "ktintro"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += listOf("-Xuse-experimental=kotlin.Experimental")
}

fun configureSourceSet(ss: SourceSet, dir: String) {
    ss.java.setSrcDirs(listOf(dir))
    ss.resources.setSrcDirs(listOf(dir))
    ss.withConvention(KotlinSourceSet::class) {
        kotlin.setSrcDirs(listOf(dir))
    }
}

// Can't delete the test source set otherwise intellij complains
configureSourceSet(sourceSets["test"], ".gradle/test")
configureSourceSet(sourceSets["main"], "src")

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.1")

    // Serialization
    val jacksonVersion = "2.9.8"
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    compile("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")


    // Logging
    val slf4jVersion = "1.7.25"
    compile("org.slf4j:slf4j-api:$slf4jVersion")
    compile("org.slf4j:slf4j-log4j12:$slf4jVersion")
    compile("log4j:log4j:1.2.17")

    // Jetty
    val jettyVersion = "9.4.15.v20190215"
    compile("org.eclipse.jetty:jetty-server:$jettyVersion")
}
