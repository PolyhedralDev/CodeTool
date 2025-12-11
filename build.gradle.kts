import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("org.jetbrains.intellij.platform") version "2.6.0"
    kotlin("plugin.serialization") version "2.0.0"
}

group = "com.dfsek.terra.codetool"
version = "1.0.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        create("IC", "2025.1.2")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        bundledPlugin("org.jetbrains.plugins.yaml")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("com.charleskorn.kaml:kaml:0.61.0")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242"
        }

        changeNotes = """
            1.0.0 - Initial release with <code>.tesf</code> support   
        """.trimIndent()
    }
}

tasks.register<UpdateRegistryDataTask>("updateRegistryData") {
    mcVersion.set("1.21.11")
    serverJarUrl.set("https://piston-data.mojang.com/v1/objects/205a55e13ce3104298dd84d1fa55bc524fb3ca51/server.jar")
}

tasks.register<Copy>("copyApiDocumentation") {
    from("terraDocs/docs/config/documentation/addons")
    include("*.yml")
    into("src/main/resources/documentation/addons")
}

sourceSets {
    main {
        java.srcDir("src/main/gen")
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }
}
