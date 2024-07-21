import java.io.FileOutputStream
import java.net.URL

plugins {
    `maven-publish`
    id("io.github.goooler.shadow")
    id("xyz.jpenilla.run-paper") version "1.0.6"
}

dependencies {
    implementation(project(":api")) // API
    compileOnly("com.alttd:Galaxy-API:1.20.4-R0.1-SNAPSHOT") // Galaxy
    compileOnly("com.gitlab.ruany:LiteBansAPI:0.3.5") // move to proxy
    compileOnly("org.apache.commons:commons-lang3:3.12.0") // needs an alternative, already removed from upstream api and will be removed in server
    compileOnly("net.luckperms:api:5.3") // Luckperms
    compileOnly(files("../libs/CMI.jar"))
}

tasks {

    shadowJar {
        archiveFileName.set("${rootProject.name}-${project.name}-${project.version}.jar")
//        minimize()
    }

    build {
//        setBuildDir("${rootProject.buildDir}")
        dependsOn(shadowJar)
    }

    runServer {
        val dir = File(System.getProperty("user.home") + "/share/devserver/");
        if (!dir.parentFile.exists()) {
            dir.parentFile.mkdirs()
        }
        runDirectory.set(dir)

        val fileName = "/galaxy.jar"
        var file = File(dir.path + fileName)

        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        if (!file.exists()) {
            download("https://repo.destro.xyz/snapshots/com/alttd/Galaxy-Server/Galaxy-paperclip-1.19.2-R0.1-SNAPSHOT-reobf.jar", file)
        }
        serverJar(file)
        minecraftVersion("1.19.2")
    }
}

fun download(link: String, path: File) {
    URL(link).openStream().use { input ->
        FileOutputStream(path).use { output ->
            input.copyTo(output)
        }
    }
}