plugins {
    `maven-publish`
    id("io.github.goooler.shadow")
}

dependencies {
    implementation(project(":api")) // API
    compileOnly("com.alttd:Galaxy-API:1.21-R0.1-SNAPSHOT") // Galaxy
    compileOnly("com.gitlab.ruany:LiteBansAPI:0.3.5") // move to proxy
    compileOnly("org.apache.commons:commons-lang3:3.12.0") // needs an alternative, already removed from upstream api and will be removed in server
    compileOnly("net.luckperms:api:5.3") // Luckperms
}

tasks {

    shadowJar {
        archiveFileName.set("${rootProject.name}-${project.name}-${project.version}.jar")
    }

    build {
        dependsOn(shadowJar)
    }

}