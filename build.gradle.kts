import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val javaVersion = "1.8"
val mcVersion = "1.8.8"

group = "de.hglabor"
version = "${mcVersion}_v1"

description = "Minecraft Hunger Games in $mcVersion"

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.0"

    `java-library`
    id("com.github.johnrengelman.shadow") version "7.0.0" // Used for building the plugin
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1" // Generates plugin.yml
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    implementation(kotlin("stdlib"))
    // Spigot
    compileOnly("org.spigotmc", "spigot-api", "$mcVersion-R0.1-SNAPSHOT")

    // KSpigot
    implementation(files("/libs/KSpigot-1.8.0.jar"))

    // KMONGO
    implementation("org.litote.kmongo", "kmongo", "4.4.0")
    implementation("org.litote.kmongo", "kmongo-serialization-mapping", "4.4.0")

    // Kotlin
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.6.0-native-mt")
}

tasks {
    shadowJar {
        fun reloc(pkg: String) = relocate(pkg, "de.hglabor.dependency.$pkg")
        reloc("net.axay")
        //reloc("de.hglabor")
    }
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaVersion
            freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
    }
}

// Configure plugin.yml generation
bukkit {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "de.hglabor.plugins.hungergames.HungerGames"
    apiVersion = "1.8"
    softDepend = listOf("WorldEdit")
    authors = listOf("BestAuto")
    commands {
        register("start") {
            description = "Start the next gamephase"
        }
        register("kit") {
            description = "Choose a kit"
        }
        register("feast") {
            description = "Point your compass towards the feast"
        }
    }
}