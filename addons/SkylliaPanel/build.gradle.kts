plugins {
    id("java")
}

group = "fr.euphyllia.skylliapanel"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly(project(":api"))

    compileOnly("dev.triumphteam:triumph-gui-paper:3.1.13") // "dev.triumphteam:triumph-gui:3.1.13"

    compileOnly("com.electronwill.night-config:toml:3.6.7")
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
