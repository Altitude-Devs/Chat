plugins {
    `maven-publish`
    id("io.github.goooler.shadow")
}

dependencies {
    implementation(project(":api")) // API
    compileOnly("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")
    implementation("mysql:mysql-connector-java:8.0.27") // mysql
    implementation("org.spongepowered", "configurate-yaml", "4.1.2")
    compileOnly("net.kyori:adventure-text-minimessage:4.10.1")
    compileOnly("com.gitlab.ruany:LiteBansAPI:0.3.5")
    compileOnly("com.alttd.proxydiscordlink:ProxyDiscordLink:1.0.0-BETA-SNAPSHOT")
}

tasks {

    shadowJar {
        archiveFileName.set("${rootProject.name}-${project.name}-${project.version}.jar")
//        minimize()
        listOf(
//            "net.kyori.adventure.text.minimessage",
            "org.spongepowered.configurate"
        ).forEach { relocate(it, "${rootProject.group}.lib.${it.substringAfterLast(".")}") }
    }

    build {
//        setBuildDir("${rootProject.buildDir}")
        dependsOn(shadowJar)
    }

}