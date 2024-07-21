plugins {
    `java-library`
    id("io.github.goooler.shadow") version "8.1.8"
}

allprojects {
    group = "com.alttd.chat"
    version = "2.0.0-SNAPSHOT"
    description = "All in one minecraft chat plugin"
}

subprojects {
    apply<JavaLibraryPlugin>()

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
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
        archiveFileName.set("${project.name}.jar")
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

    jar {
        enabled = false
    }
}