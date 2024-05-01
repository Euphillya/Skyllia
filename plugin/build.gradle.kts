dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT") { isTransitive = false }
    implementation(project(":api"))
    implementation(project(":nms:v1_19_R2"))
    implementation(project(":nms:v1_19_R3"))
    implementation(project(":nms:v1_20_R1"))
    implementation(project(":nms:v1_20_R2"))
    implementation(project(":nms:v1_20_R3"))
    implementation(project(":nms:v1_20_R4"))
    compileOnly("net.kyori:adventure-text-minimessage:4.15.0")
    compileOnly("com.electronwill.night-config:toml:3.6.7")
    compileOnly("com.google.guava:guava:33.0.0-jre")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.9") { isTransitive = false }
    compileOnly("com.sk89q.worldedit:worldedit-core:7.2.0-SNAPSHOT") { isTransitive = false }
    compileOnly("com.github.Euphillya:Energie:1.2.0")
    compileOnly("com.github.Euphillya:SGBD-MariaDB:3827fafa25")
    compileOnly("org.apache.logging.log4j:log4j-core:2.23.1")
    compileOnly("org.apache.logging.log4j:log4j-api:2.23.1")
    compileOnly("net.md-5:bungeecord-api:1.20-R0.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}