
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9

/**
 * Convention plugin for use in kotlin only modules
 */
plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    explicitApi = ExplicitApiMode.Warning
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        languageVersion = KOTLIN_1_9
        apiVersion = KOTLIN_1_9
        freeCompilerArgs.addAll(
            "-Xjvm-default=all",
            "-opt-in=com.squareup.anvil.annotations.ExperimentalAnvilApi",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi",
        )
    }
}
