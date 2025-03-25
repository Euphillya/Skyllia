import java.io.ByteArrayOutputStream

plugins {
    id("java-library")
    id("java")
    id("maven-publish")
    id("io.github.goooler.shadow") version "8.1.8"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16" apply false
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
}

allprojects {
    group = "fr.euphyllia";
    version = "2.0-" + (System.getenv("GITHUB_RUN_NUMBER") ?: getGitCommitHash())
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
        compileOnly("org.apache.maven.resolver:maven-resolver-api:2.0.7")
        compileOnly("org.apache.logging.log4j:log4j-api:2.24.3")
        compileOnly("org.apache.logging.log4j:log4j-core:2.24.3")
        compileOnly("org.mariadb.jdbc:mariadb-java-client:3.5.2")
        compileOnly("com.zaxxer:HikariCP:6.2.1")
        compileOnly("org.xerial:sqlite-jdbc:3.49.1.0")
        compileOnly("net.kyori:adventure-text-minimessage:4.19.0")
        compileOnly("com.electronwill.night-config:toml:3.8.1")
        compileOnly("com.github.ben-manes.caffeine:caffeine:3.1.6")
        compileOnly("net.md-5:bungeecord-api:1.20-R0.2")
        compileOnly("com.mojang:brigadier:1.0.18")
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