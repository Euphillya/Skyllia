plugins {
    id("java")
    id("maven-publish")
}
group = "fr.euphyllia.skyllia"
version = "2.1"

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT") { isTransitive = false }
    compileOnly(project(":api"))
    compileOnly(project(":database"))
    compileOnly(project(":nms:v1_20_R1"))
    compileOnly(project(":nms:v1_20_R2"))
    compileOnly(project(":nms:v1_20_R3"))
    compileOnly(project(":nms:v1_20_R4"))
    compileOnly(project(":nms:v1_21_R1"))
    compileOnly(project(":nms:v1_21_R2"))
    compileOnly(project(":nms:v1_21_R3"))
    compileOnly(project(":nms:v1_21_R4"))

    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.12-SNAPSHOT") { isTransitive = false }
    compileOnly("com.sk89q.worldedit:worldedit-core:7.3.12-SNAPSHOT") { isTransitive = false }
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
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Euphillya/Skyllia")
            credentials {
                username = System.getenv("GITHUB_USERNAME") ?: ""
                password = System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
        maven {
            name = "excalia-repo"
            val isSnapshot = version.toString().endsWith("SNAPSHOT")

            url = uri(
                if (isSnapshot)
                    "http://172.29.17.1:8081/repository/maven-snapshots/"
                else
                    "http://172.29.17.1:8081/repository/maven-releases/"
            )
            isAllowInsecureProtocol = true
            credentials {
                username = System.getenv("NEXUS_USERNAME") ?: error("Variable NEXUS_USERNAME introuvable !")
                password = System.getenv("NEXUS_PASSWORD") ?: error("Variable NEXUS_PASSWORD introuvable !")
            }
        }
    }
}