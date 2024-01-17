plugins {
    id("java-library")
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "fr.euphyllia";
version = "1.0-alpha.1";
description = "Plugin Skyblock pour Folia / PaperMC";

val paperRepo = "https://repo.papermc.io/repository/maven-public/";
val sonatypeRepo = "https://oss.sonatype.org/content/groups/public/";
val engineHubRepo = "https://maven.enginehub.org/repo/";
val essentialsRepo = "https://repo.essentialsx.net/releases"


dependencies {
    implementation(project(":nms:v1_19_R3", "reobf"))
    implementation(project(":nms:v1_20_R1", "reobf"))
    implementation(project(":nms:v1_20_R2", "reobf"))
    implementation(project(":plugin"))

    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenCentral()
        maven(paperRepo)
        maven(sonatypeRepo)
        maven(engineHubRepo)
        maven(essentialsRepo)
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

