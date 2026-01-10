plugins {
    id("java")
    id("maven-publish")
}
group = "fr.euphyllia.skyllia"

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT") { isTransitive = false }
    compileOnly("net.kyori:adventure-text-minimessage:4.25.0")
    compileOnly(project(":api"))
    compileOnly(project(":database"))
    compileOnly(project(":hook:worldedit"))
    compileOnly(project(":hook:fastasyncworldedit"))
    compileOnly(project(":hook:internalworld"))
    compileOnly(project(":hook:canvas"))
    compileOnly(project(":hook:luminol"))
    compileOnly(project(":hook:essentialsx"))

    // NMS Version
    compileOnly(project(":nms:v1_20_R1"))
    compileOnly(project(":nms:v1_20_R2"))
    compileOnly(project(":nms:v1_20_R3"))
    compileOnly(project(":nms:v1_20_R4"))
    compileOnly(project(":nms:v1_21_R1"))
    compileOnly(project(":nms:v1_21_R2"))
    compileOnly(project(":nms:v1_21_R3"))
    compileOnly(project(":nms:v1_21_R4"))
    compileOnly(project(":nms:v1_21_R5"))
    compileOnly(project(":nms:v1_21_R6"))
    compileOnly(project(":nms:v1_21_R7"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "Euphyllia-Repo"
            url = uri("https://repo.euphyllia.moe/repository/maven-releases/")
            credentials {
                username = System.getenv("NEXUS_USERNAME") ?: ""
                password = System.getenv("NEXUS_PASSWORD") ?: ""
            }
        }
    }
}