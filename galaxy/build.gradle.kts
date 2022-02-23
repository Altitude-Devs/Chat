plugins {
    `maven-publish`
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":api")) // API
    compileOnly("com.alttd:Galaxy-API:1.18.1-R0.1-SNAPSHOT") {
        exclude("net.kyori.adventure.text.minimessag")
    } // Galaxy
    compileOnly("net.kyori", "adventure-text-minimessage", "4.10.0-20220122.015731-43") { // Minimessage
        exclude("net.kyori")
        exclude("net.kyori.examination")
    }
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