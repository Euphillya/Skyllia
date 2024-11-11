plugins {
    id("java")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT") { isTransitive = false }
    compileOnly(project(":api"))
    compileOnly(project(":nms:v1_20_R1", "reobf"))
    compileOnly(project(":nms:v1_20_R2", "reobf"))
    compileOnly(project(":nms:v1_20_R3", "reobf"))
    compileOnly(project(":nms:v1_20_R4", "reobf"))
    compileOnly(project(":nms:v1_21_R1", "reobf"))

    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.9") { isTransitive = false }
    compileOnly("com.sk89q.worldedit:worldedit-core:7.2.0-SNAPSHOT") { isTransitive = false }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}