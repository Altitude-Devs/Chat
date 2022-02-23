rootProject.name = "Chat"

include(":api")
include(":galaxy")
include(":velocity")

dependencyResolutionManagement {
    repositories {
//        mavenLocal()
        mavenCentral()
        maven("https://repo.destro.xyz/snapshots") // Altitude - Galaxy
//        maven("https://oss.sonatype.org/content/repositories/snapshots/") // Minimessage
        maven("https://nexus.velocitypowered.com/repository/") // Velocity
        maven("https://nexus.velocitypowered.com/repository/maven-public/") // Velocity
        maven("https://repo.spongepowered.org/maven") // Configurate
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // Papi
        maven("https://jitpack.io")
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
