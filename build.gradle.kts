import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
    id("java")
    id("maven-publish")
    id("io.github.goooler.shadow") version "8.1.7"
    id("io.papermc.paperweight.userdev") version "1.7.1" apply false
}

val paperRepo = "https://repo.papermc.io/repository/maven-public/";
val sonatypeRepo = "https://oss.sonatype.org/content/groups/public/";
val engineHubRepo = "https://maven.enginehub.org/repo/";
val jitpack = "https://jitpack.io";

dependencies {
    implementation(project(":api"))
    implementation(project(":plugin"))
    implementation(project(":nms:v1_20_R1", "reobf"))
    implementation(project(":nms:v1_20_R2", "reobf"))
    implementation(project(":nms:v1_20_R3", "reobf"))
    implementation(project(":nms:v1_20_R4", "reobf"))
    implementation(project(":nms:v1_21_R1", "reobf"))
}

allprojects {
    group = "fr.euphyllia";
    version = "1.0-" + System.getenv("GITHUB_RUN_NUMBER");
    description = "Plugin Skyblock pour Folia / PaperMC";

    apply(plugin = "java-library")
    apply(plugin = "io.github.goooler.shadow")
    apply(plugin = "maven-publish")

    repositories {
        mavenLocal()
        mavenCentral()
        maven(paperRepo)
        maven(sonatypeRepo)
        maven(engineHubRepo)
        maven(jitpack)
    }

    dependencies {
        compileOnly("com.github.Euphillya:SGBD-MariaDB:1.3")

        compileOnly("org.apache.logging.log4j:log4j-api:2.23.1")
        compileOnly("org.apache.logging.log4j:log4j-core:2.23.1")
        compileOnly("org.mariadb.jdbc:mariadb-java-client:3.4.0")
        compileOnly("com.zaxxer:HikariCP:5.1.0")
        compileOnly("net.kyori:adventure-text-minimessage:4.15.0")
        compileOnly("com.electronwill.night-config:toml:3.8.0")
        compileOnly("com.google.guava:guava:33.2.1-jre")
        compileOnly("net.md-5:bungeecord-api:1.20-R0.2")
    }

    tasks.withType<ShadowJar> {
        relocate("fr.euphyllia.sgbd", "fr.euphyllia.skyllia.dependency.sgbd")
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }
        processResources {
            filesMatching("**/plugin.yml") {
                expand(rootProject.project.properties)
            }

            // Always re-run this task
            outputs.upToDateWhen { false }
        }

    }
}

tasks.test {
    useJUnitPlatform()
}


java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
