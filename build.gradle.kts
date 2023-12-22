plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.5.10" // Check for new versions at https://plugins.gradle.org/plugin/io.papermc.paperweight.userdev
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "fr.euphyllia";
version = "0.1-SNAPSHOT";
description = "Plugin Skyblock pour Folia";

val paperRepo = "https://repo.papermc.io/repository/maven-public/";
val sonatypeRepo = "https://oss.sonatype.org/content/groups/public/";
val engineHubRepo = "https://maven.enginehub.org/repo/";
val essentialsRepo = "https://repo.essentialsx.net/releases"

repositories {
    mavenCentral()
    maven(paperRepo)
    maven(sonatypeRepo)
    maven(engineHubRepo)
    maven(essentialsRepo)
}

dependencies {
    paperweight.foliaDevBundle("1.20.2-R0.1-SNAPSHOT") // Car j'ai besoin du code de Folia
    implementation("com.electronwill.night-config:toml:3.6.6")
    implementation("com.google.guava:guava:33.0.0-jre")
    implementation("org.apache.logging.log4j:log4j-api:2.22.0")
    implementation("org.apache.logging.log4j:log4j-core:2.22.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.2")
    implementation("com.zaxxer:HikariCP:5.0.1")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.9") { isTransitive = false }
    compileOnly("com.sk89q.worldedit:worldedit-core:7.2.0-SNAPSHOT") { isTransitive = false }
    compileOnly("net.essentialsx:EssentialsXSpawn:2.19.7") { isTransitive = false }
    compileOnly("net.essentialsx:EssentialsX:2.19.7") { isTransitive = false }

    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
    compileJava {
        options.encoding = "UTF-8"
    }
}

