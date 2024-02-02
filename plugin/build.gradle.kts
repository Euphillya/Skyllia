dependencies {
    compileOnly("dev.folia:folia-api:1.20.2-R0.1-SNAPSHOT")
    implementation(project(":api"))
    implementation(project(":nms:v1_19_R3"))
    implementation(project(":nms:v1_20_R1"))
    implementation(project(":nms:v1_20_R2"))
    implementation(project(":nms:v1_20_R3"))
    implementation("net.kyori:adventure-text-minimessage:4.15.0")
    implementation("com.electronwill.night-config:toml:3.6.7")
    implementation("com.google.guava:guava:33.0.0-jre")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.9") { isTransitive = false }
    compileOnly("com.sk89q.worldedit:worldedit-core:7.2.0-SNAPSHOT") { isTransitive = false }
    compileOnly("net.essentialsx:EssentialsXSpawn:2.19.7") { isTransitive = false }
    compileOnly("net.essentialsx:EssentialsX:2.19.7") { isTransitive = false }
}