plugins {
    id("java")
    id("maven-publish")
}

group = "fr.euphyllia.skyllia"
version = "3.0.0-SNAPSHOT"

dependencies {
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
            url = uri("http://172.29.17.1:8081/repository/maven-releases/")
            isAllowInsecureProtocol = true
            credentials {
                username = System.getenv("NEXUS_USERNAME") ?: ""
                password = System.getenv("NEXUS_PASSWORD") ?: ""
            }
        }
    }
}