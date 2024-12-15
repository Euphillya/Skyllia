import java.io.ByteArrayOutputStream

plugins {
    id("java-library")
    id("java")
    id("maven-publish")
    id("io.github.goooler.shadow") version "8.1.8"
    id("io.papermc.paperweight.userdev") version "1.7.7" apply false
}

val paperRepo = "https://repo.papermc.io/repository/maven-public/";
val sonatypeRepo = "https://oss.sonatype.org/content/groups/public/";
val engineHubRepo = "https://maven.enginehub.org/repo/";
val jitpack = "https://jitpack.io";

dependencies {
    implementation(project(":database"))
    implementation(project(":api"))
    implementation(project(":plugin"))
    implementation(project(":nms:v1_20_R1", "reobf"))
    implementation(project(":nms:v1_20_R2", "reobf"))
    implementation(project(":nms:v1_20_R3", "reobf"))
    implementation(project(":nms:v1_20_R4", "reobf"))
    implementation(project(":nms:v1_21_R1", "reobf"))
    implementation(project(":nms:v1_21_R2", "reobf"))
    implementation(project(":nms:v1_21_R3", "reobf"))
}

allprojects {
    group = "fr.euphyllia";
    version = "1.0-" + (System.getenv("GITHUB_RUN_NUMBER") ?: getGitCommitHash())
    description = "First Skyblock Plugin for Folia";

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
        compileOnly("org.apache.logging.log4j:log4j-api:2.24.2")
        compileOnly("org.apache.logging.log4j:log4j-core:2.24.2")
        compileOnly("org.mariadb.jdbc:mariadb-java-client:3.5.1")
        compileOnly("com.zaxxer:HikariCP:6.2.1")
        compileOnly("net.kyori:adventure-text-minimessage:4.17.0")
        compileOnly("com.electronwill.night-config:toml:3.8.1")
        compileOnly("com.google.guava:guava:33.3.1-jre")
        compileOnly("net.md-5:bungeecord-api:1.20-R0.2")
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

fun getGitCommitHash(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "--short", "HEAD") // "--short" retourne les premières lettres du commit
        standardOutput = stdout
    }
    return stdout.toString().trim()
}