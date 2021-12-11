plugins {
    `maven-publish`
}

dependencies {
    implementation(project(":api")) // API
    compileOnly("com.velocitypowered:velocity-api:3.0.0") // Velocity
    annotationProcessor("com.velocitypowered:velocity-api:3.0.0")
    implementation("mysql:mysql-connector-java:8.0.23") // mysql
    compileOnly("net.kyori:adventure-text-minimessage:4.1.0-SNAPSHOT") // Minimessage
}