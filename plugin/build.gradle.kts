plugins {
    id("java")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT") { isTransitive = false }
    implementation(project(":api"))
    implementation(project(":nms:v1_19_R2", "reobf"))
    implementation(project(":nms:v1_19_R3", "reobf"))
    implementation(project(":nms:v1_20_R1", "reobf"))
    implementation(project(":nms:v1_20_R2", "reobf"))
    implementation(project(":nms:v1_20_R3", "reobf"))
    implementation(project(":nms:v1_20_R4", "reobf"))

    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.9") { isTransitive = false }
    compileOnly("com.sk89q.worldedit:worldedit-core:7.2.0-SNAPSHOT") { isTransitive = false }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}