plugins {
    id("java")
}
group = "fr.euphyllia.skyllia"
version = "2.3"

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-text-minimessage:4.25.0")
    compileOnly(project(":api"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
