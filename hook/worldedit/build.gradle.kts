plugins {
    id("java")
}
group = "fr.euphyllia.skyllia"
version = "2.3"

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT") { isTransitive = false }
    compileOnly("net.kyori:adventure-text-minimessage:4.25.0")
    compileOnly(project(":api"))
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.12-SNAPSHOT") { isTransitive = false }
    compileOnly("com.sk89q.worldedit:worldedit-core:7.3.12-SNAPSHOT") { isTransitive = false }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
