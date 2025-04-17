plugins {
    id("java")
    id("maven-publish")
}

group = "fr.euphyllia.skyllia"
version = "2.0"

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly(project(":database"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withJavadocJar()
    withSourcesJar()
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
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