plugins {
    `maven-publish`
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":api")) // API
    compileOnly("com.velocitypowered:velocity-api:3.0.0") // Velocity
    annotationProcessor("com.velocitypowered:velocity-api:3.0.0")
    implementation("mysql:mysql-connector-java:8.0.27") // mysql
    implementation("org.spongepowered", "configurate-yaml", "4.1.2")
    implementation("net.kyori", "adventure-text-minimessage", "4.10.0-SNAPSHOT") {
        exclude("net.kyori")
        exclude("net.kyori.examination")
    }
    compileOnly("com.gitlab.ruany:LiteBansAPI:0.3.5")
}

tasks {

    shadowJar {
        archiveFileName.set("${project.name}-${project.version}.jar")
//        minimize()
        listOf(
            "net.kyori.adventure.text.minimessage",
            "org.spongepowered.configurate"
        ).forEach { relocate(it, "${rootProject.group}.lib.${it.substringAfterLast(".")}") }
    }

    build {
        dependsOn(shadowJar)
    }

}