import java.io.ByteArrayOutputStream

plugins {
    id("java-library")
    id("java")
    id("maven-publish")
    id("io.github.goooler.shadow") version "8.1.8"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18" apply false
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

val paperRepo = "https://repo.papermc.io/repository/maven-public/";
val sonatypeRepo = "https://oss.sonatype.org/content/groups/public/";
val engineHubRepo = "https://maven.enginehub.org/repo/";
val jitpack = "https://jitpack.io"
val mojang = "https://libraries.minecraft.net";

dependencies {
    implementation(project(":database"))
    implementation(project(":api"))
    implementation(project(":plugin"))
    implementation(project(":nms:v1_20_R1"))
    implementation(project(":nms:v1_20_R2"))
    implementation(project(":nms:v1_20_R3"))
    implementation(project(":nms:v1_20_R4"))
    implementation(project(":nms:v1_21_R1"))
    implementation(project(":nms:v1_21_R2"))
    implementation(project(":nms:v1_21_R3"))
    implementation(project(":nms:v1_21_R4"))
    implementation(project(":nms:v1_21_R5"))
    implementation(project(":nms:v1_21_R6"))
}

allprojects {
    group = "fr.euphyllia";
    version = "2.1-" + (System.getenv("GITHUB_RUN_NUMBER") ?: getGitCommitHash())
    description = "First Skyblock plugin on Folia. If you want features, join us on our Discord.";

    apply(plugin = "java-library")
    apply(plugin = "io.github.goooler.shadow")
    apply(plugin = "maven-publish")

    repositories {
        mavenLocal()
        mavenCentral()
        maven(paperRepo)
        maven(sonatypeRepo)
        maven(engineHubRepo)
        maven(mojang)
        maven(jitpack)
    }

    dependencies {
        compileOnly("org.apache.maven.resolver:maven-resolver-api:2.0.11")
        compileOnly("org.apache.logging.log4j:log4j-api:2.25.1")
        compileOnly("org.apache.logging.log4j:log4j-core:2.25.1")
        compileOnly("org.mariadb.jdbc:mariadb-java-client:3.5.6")
        compileOnly("com.zaxxer:HikariCP:7.0.2")
        compileOnly("org.xerial:sqlite-jdbc:3.50.3.0")
        compileOnly("org.jetbrains:annotations:26.0.2-1")
        compileOnly("com.electronwill.night-config:toml:3.8.3")
        compileOnly("com.github.ben-manes.caffeine:caffeine:3.2.2")
        compileOnly("net.md-5:bungeecord-api:1.20-R0.2")
        compileOnly("com.mojang:brigadier:1.0.18")
        compileOnly("org.mongodb:mongodb-driver-sync:5.4.0")
        compileOnly("org.mongodb:bson:5.4.0")
    }

    tasks {

        compileJava {
            options.encoding = "UTF-8"
        }
        processResources {
            filesMatching("**/paper-plugin.yml") {
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

runPaper.folia.registerTask()

tasks {
    runServer {
        minecraftVersion("1.21.4")
    }
}