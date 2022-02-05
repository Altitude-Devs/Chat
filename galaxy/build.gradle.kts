plugins {
    `maven-publish`
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":api")) // API
    compileOnly("com.alttd:Galaxy-API:1.18.1-R0.1-SNAPSHOT") // Galaxy
    compileOnly("net.kyori:adventure-text-minimessage:4.10.0-SNAPSHOT") // Minimessage
    compileOnly("com.gitlab.ruany:LiteBansAPI:0.3.5") // move to proxy
}

tasks {

    shadowJar {
        archiveFileName.set("${project.name}-${project.version}.jar")
//        minimize()
    }

    build {
        dependsOn(shadowJar)
    }

}