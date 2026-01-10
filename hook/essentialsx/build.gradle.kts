plugins {
    id("java")
}
group = "fr.euphyllia.skyllia.hook.essentialsx"
version = "2.3"


repositories {
    mavenCentral()
    maven {
        name = "EssentialsX"
        url = uri("https://repo.essentialsx.net/releases/")
    }
}
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT") { isTransitive = false }
    compileOnly("net.kyori:adventure-text-minimessage:4.25.0")
    compileOnly(project(":api"))

    compileOnly("net.essentialsx:EssentialsX:2.21.2") { isTransitive = false }
    compileOnly("net.essentialsx:EssentialsXSpawn:2.21.2") { isTransitive = false }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
