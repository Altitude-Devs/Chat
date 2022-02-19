plugins {
    `maven-publish`
}

dependencies {
    compileOnly("com.alttd:Galaxy-API:1.18.1-R0.1-SNAPSHOT") {
        exclude("net.kyori.adventure.text.minimessage")
    }
    compileOnly("net.kyori:adventure-text-minimessage:4.10.0-SNAPSHOT") // Minimessage
    compileOnly("org.spongepowered:configurate-yaml:4.1.2") // Configurate
    compileOnly("net.luckperms:api:5.3") // Luckperms
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories{
        maven {
            name = "maven"
            url = uri("https://repo.destro.xyz/snapshots")
            credentials(PasswordCredentials::class)
        }
    }
}