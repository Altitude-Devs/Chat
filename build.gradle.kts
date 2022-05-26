plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

allprojects {
    group = "com.alttd.chat"
    version = "1.0.0-SNAPSHOT"
    description = "All in one minecraft chat plugin"

//    repositories {
//        mavenCentral()
//        maven("https://repo.destro.xyz/snapshots") // Altitude - Galaxy
//        maven("https://oss.sonatype.org/content/groups/public/") // Adventure
//        maven("https://oss.sonatype.org/content/repositories/snapshots/") // Minimessage
//        maven("https://oss.sonatype.org/content/repositories/") // Minimessage
//        maven("https://nexus.velocitypowered.com/repository/") // Velocity
//        maven("https://nexus.velocitypowered.com/repository/maven-public/") // Velocity
//        maven("https://repo.spongepowered.org/maven") // Configurate
//        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // Papi
//        maven("https://jitpack.io")
//    }
}

subprojects {
    apply<JavaLibraryPlugin>()

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = Charsets.UTF_8.name()
        }

        withType<Javadoc> {
            options.encoding = Charsets.UTF_8.name()
        }

        withType<JavaCompile> {
            options.isDeprecation = true
        }
    }
}

dependencies {
//    implementation(project(":api"))
    implementation(project(":galaxy"))
    implementation(project(":velocity"))
//    implementation("net.kyori", "adventure-text-minimessage", "4.10.0-SNAPSHOT") {
//        exclude("net.kyori")
//        exclude("net.kyori.examination")
//    }
//    implementation("org.spongepowered", "configurate-yaml", "4.1.2")
//    implementation("mysql:mysql-connector-java:8.0.27") // mysql
}

tasks {

    shadowJar {
        archiveFileName.set("${project.name}-${project.version}.jar")
        minimize() {
            exclude { it.moduleName == "galaxy" }
            exclude { it.moduleName == "velocity" }
        }
        listOf(
//            "net.kyori.adventure.text.minimessage",
            "org.spongepowered.configurate"
//        ).forEach { relocate(it, "${rootProject.group}.lib.$it") }
        ).forEach { relocate(it, "${rootProject.group}.lib.${it.substringAfterLast(".")}") }
    }

    build {
        dependsOn(shadowJar)
    }

}