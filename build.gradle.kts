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

repositories {
    mavenCentral()
    maven(paperRepo)
    maven(sonatypeRepo)
}

dependencies {
    paperweight.foliaDevBundle("1.20.2-R0.1-SNAPSHOT") // Car j'ai besoin du code de Folia
    implementation("com.electronwill.night-config:toml:3.6.6")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("org.apache.logging.log4j:log4j-api:2.22.0")
    implementation("org.apache.logging.log4j:log4j-core:2.22.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.2")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation(platform("com.intellectualsites.bom:bom-newest:1.39")) // Ref: https://github.com/IntellectualSites/bom
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core:2.5.2")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.5.2") { isTransitive = false }

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

