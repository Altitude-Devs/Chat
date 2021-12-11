plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

allprojects {
    group = "com.alttd.chat"
    version = "1.0.0-SNAPSHOT"
    description = "All in one minecraft chat plugin"
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
    }
}

dependencies {
//    implementation(project(":api"))
    implementation(project(":galaxy"))
    implementation(project(":velocity"))
    implementation("net.kyori", "adventure-text-minimessage", "4.1.0-SNAPSHOT")
    implementation("org.spongepowered", "configurate-yaml", "4.1.2")
}

tasks {

    shadowJar {
        archiveFileName.set("${project.name}-${project.version}.jar")
        listOf(
            "net.kyori.adventure.text.minimessage",
            "org.spongepowered.configurate"
        ).forEach { relocate(it, "${rootProject.group}.lib.$it") }
    }

    build {
        dependsOn(shadowJar)
    }

}