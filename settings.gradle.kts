@file:Suppress("MissingPackageDeclaration", "UnstableApiUsage")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal() // maven("https://plugins.gradle.org/m2")

        maven("https://repo.spring.io/milestone")

        // Snapshots von Spring Framework, Spring Boot, ...
        // maven("https://repo.spring.io/snapshot") { mavenContent { snapshotsOnly() } }
        // maven("https://repo.spring.io/plugins-release")
    }
}

// buildCache {
//    local {
//        directory = "C:/Z/caches"
//    }
// }

rootProject.name = "kunde"
