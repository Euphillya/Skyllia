import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
    id("java")
    id("maven-publish")
    id("io.github.goooler.shadow") version "8.1.7"
}

group = "fr.euphyllia";
version = "1.0-RC7-" + System.getenv("GITHUB_RUN_NUMBER");
description = "Plugin Skyblock pour Folia / PaperMC";

val paperRepo = "https://repo.papermc.io/repository/maven-public/";
val sonatypeRepo = "https://oss.sonatype.org/content/groups/public/";
val engineHubRepo = "https://maven.enginehub.org/repo/";
val jitpack = "https://jitpack.io";

dependencies {
    implementation(project(":nms:v1_19_R2", "reobf"))
    implementation(project(":nms:v1_19_R3", "reobf"))
    implementation(project(":nms:v1_19_R3", "reobf"))
    implementation(project(":nms:v1_20_R1", "reobf"))
    implementation(project(":nms:v1_20_R2", "reobf"))
    implementation(project(":nms:v1_20_R3", "reobf"))
    implementation(project(":nms:v1_20_R4", "reobf")) // 1.20.5-6
    implementation(project(":plugin"))

    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "io.github.goooler.shadow")
    apply(plugin = "maven-publish")

    repositories {
        mavenCentral()
        maven(paperRepo)
        maven(sonatypeRepo)
        maven(engineHubRepo)
        maven(jitpack)
    }


    tasks.withType<ShadowJar> {
        relocate("fr.euphyllia.energie", "fr.euphyllia.skyllia.dependency.energie")
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
