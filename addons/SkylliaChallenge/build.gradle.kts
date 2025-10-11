plugins {
    id("java")
}

group = "fr.euphyllia.skyllia";
version = "2.0";

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")

    compileOnly(project(":api"))
    compileOnly(project(":plugin"))
    compileOnly(project(":database"))
    compileOnly(project(":addons:SkylliaBank"))

    compileOnly("dev.triumphteam:triumph-gui:3.1.13")
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
}