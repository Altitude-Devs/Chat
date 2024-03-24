plugins {
    `maven-publish`
}

dependencies {
    compileOnly("com.alttd:Galaxy-API:1.19.2-R0.1-SNAPSHOT") {
//        exclude("net.kyori")
    }
    compileOnly("org.spongepowered:configurate-yaml:4.1.2") // Configurate
    compileOnly("net.luckperms:api:5.3") // Luckperms
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation("com.alttd:Galaxy-API:1.19.2-R0.1-SNAPSHOT")
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

tasks.test {
    useJUnitPlatform()
}