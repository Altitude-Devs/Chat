plugins {
    `maven-publish`
    id("com.github.johnrengelman.shadow")
}

dependencies {
    implementation(project(":api")) // API
    compileOnly("com.alttd:Galaxy-API:1.18.1-R0.1-SNAPSHOT") // Galaxy
    compileOnly("com.gitlab.ruany:LiteBansAPI:0.3.5")
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