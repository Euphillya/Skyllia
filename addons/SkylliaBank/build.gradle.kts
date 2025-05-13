plugins {
    id("java")
}

group = "fr.euphyllia.skyllia";
version = "2.0";

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    compileOnly(project(":api"))
    compileOnly(project(":plugin"))
    compileOnly(project(":database"))
    compileOnly(project(":addons:SkylliaOre"))
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